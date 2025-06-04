package com.example.customchart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class RingChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Параметры диаграммы
    private var textSize = 40f                              // Размер шрифта %
    private var thickness = 0.1f                            // Отношение толщины к наруж.диаметру

    private var data = mutableListOf<ChartItem>()
    private val paint = Paint()
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRingChart(canvas)
    }

    @SuppressLint("DefaultLocale")
    private fun drawRingChart(canvas: Canvas) {
        var total = 0f
        if (data.size != 0) total = data.toList().sumOf { it.sum.toDouble() }.toFloat()
        val textFormat = "%.1f%%"                                               // Формат вывода %
        val grandColor = getColorFromTheme(context, R.attr.grandColor)
        val textColor = getColorFromTheme(context, R.attr.textColor)

        val minSize = if (width <= height) width else height                    // Р-р наименьшей стороны объекта
        val hundredPercent = String.format(textFormat, 100f)                    // Строка "100%"
        paint.textSize = textSize
        val textLength = paint.measureText(hundredPercent)                      // Макс.ширина поля %
        val outerDiameter = minSize - 2.6f * textLength                         // Наружный диаметр диаграммы
        val thicknessSize = outerDiameter * thickness                           // Толщина диаграммы
        val rectExternal = createInnerRectF(rect, 1.3f * textLength)      // Прямоугольник, в который вписывается диаграмма
        val rectInternal = createInnerRectF(rectExternal, thicknessSize)        // Прямоугольник, в который вписывается внутренний круг диаграммы
        val cx = width * 0.5f                                                   // Координата X центра диаграммы
        val cy = height * 0.5f                                                  // Координата Y центра диаграммы
        val radiusOfRounding = thicknessSize * 0.5f                             // Радиус скругления
        val distance = (outerDiameter - thicknessSize) / 2                      // Радиус средней линии кольца
        val iconSize = 1.4 * radiusOfRounding                                   // Размер иконки
        val distanceText = 0.5 * outerDiameter + 0.65f * textLength             // Радиус центров текста

        var startAngle = 0f                             // Начальный угол
        if (total != 0f) {
            for (i in data.indices) {
                val sweepAngle = (data[i].sum / total) * 360    // Вычисление угла сегмента
                paint.color = data[i].color
                canvas.drawArc(rectExternal, startAngle, sweepAngle, true, paint) // Рисуем сегмент
                startAngle += sweepAngle                    // Обновляем начальный угол для следующего сегмента
            }
        } else {
            paint.color = Color.LTGRAY
            canvas.drawArc(rectExternal, 0f, 360f, true, paint)
        }

        // Рисуем отверстие в центре
        paint.color = ContextCompat.getColor(context, grandColor)
        paint.style = Paint.Style.FILL
        canvas.drawArc(rectInternal, 0f, 360f, true, paint)

        // Рисуем скругление, иконку и выводим текст
        startAngle = 360f
        for (i in data.size -1 downTo 0) {
            val sweepAngle = (data[i].sum / total) * 360f   // Вычисление угла сегмента
            val sweepAngleRadians = Math.toRadians(startAngle.toDouble())

            var x = (distance * cos(sweepAngleRadians)).toFloat() + cx
            var y = (distance * sin(sweepAngleRadians)).toFloat() + cy

            paint.color = data[i].color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, radiusOfRounding, paint)

            // Рисуем иконку
            data[i].icon.setBounds((x - iconSize / 2).toInt(), (y - iconSize / 2).toInt(), (x + iconSize / 2).toInt(), (y + iconSize / 2).toInt())
            data[i].icon.draw(canvas)

            // Вычисляем процент и рисуем текст
            val percentage = String.format(textFormat, (data[i].sum / total) * 100)
            paint.color = ContextCompat.getColor(context,textColor)     // Цвет текста
            paint.textSize = textSize                                   // Размер текста
            x = (distanceText * cos(sweepAngleRadians)).toFloat() + cx  // Координатs центра текста X
            y = (distanceText * sin(sweepAngleRadians)).toFloat() + cy  // ------------//---------- Y
            val textWidth = paint.measureText(percentage)
            // Угол поворота канвы
            val rotationAngle = if (startAngle >= 90 && startAngle < 270) startAngle + 180 else  startAngle
            canvas.save()                           // Сохраняем текущее состояние канвы
            canvas.rotate(rotationAngle, x, y)      // Поворачиваем канву

            val textX = x - textWidth / 2           // Координаты верхнего левого угла текста Х
            val textY = y + 10                      // -------------------------//----------- Y
            canvas.drawText(percentage, textX, textY, paint) // Рисуем текст

            canvas.restore()                // Восстанавливаем состояние канвы

            startAngle -= sweepAngle        // Обновляем начальный угол для следующего сегмента
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        when {
            w > h -> {
                val offset = (w - h) / 2
                rect.set(offset.toFloat(), 0f, (w - offset).toFloat(), h.toFloat())
            }
            w < h -> {
                val offset = (h - w) / 2
                rect.set(0f, offset.toFloat(), w.toFloat(), (h - offset).toFloat())
            }
            else -> rect.set(0f, 0f, w.toFloat(), h.toFloat())
        }

    }

    private fun createInnerRectF(outerRect: RectF, offset: Float): RectF {
        // Вычисление координат нового RectF с учетом отступов
        val left = outerRect.left + offset
        val top = outerRect.top + offset
        val right = outerRect.right - offset
        val bottom = outerRect.bottom - offset
        return RectF(left, top, right, bottom)
    }

    fun setData(newData: List<ChartItem>) {
        data.clear()            // Очистить старые данные
        data.addAll(newData)    // Добавить новые данные
        invalidate()            // Перерисовать View
    }

    fun setParameters(newTextSize: Float, newThickness: Float) {
        textSize = newTextSize
        thickness = newThickness
    }

    private fun getColorFromTheme(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return if (typedValue.resourceId != 0) {
            typedValue.resourceId // Возвращаем идентификатор ресурса
        } else {
            // Если это не ссылка на ресурс, возвращаем цвет по умолчанию
            R.color.white
        }
    }
}