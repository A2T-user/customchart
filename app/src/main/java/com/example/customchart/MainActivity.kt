package com.example.customchart

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var button: ImageView
    private lateinit var ringChartView: RingChartView
    private lateinit var tvTotal: TextView
    private lateinit var colors: List<Int>
    private lateinit var icons: List<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.edit_text)
        val btnClear = findViewById<ImageView>(R.id.clear)
        button = findViewById(R.id.button)
        ringChartView = findViewById(R.id.ring_chart)
        tvTotal = findViewById(R.id.total)

        colors = listOf(                // Список цветов
            ContextCompat.getColor(this,R.color.color1),
            ContextCompat.getColor(this,R.color.color2),
            ContextCompat.getColor(this,R.color.color3),
            ContextCompat.getColor(this,R.color.color4),
            ContextCompat.getColor(this,R.color.color5),
            ContextCompat.getColor(this,R.color.color6),
            ContextCompat.getColor(this,R.color.color7),
            ContextCompat.getColor(this,R.color.color8)
        )
        icons = listOf(                 // Список иконок
            ContextCompat.getDrawable(this, R.drawable.ic_food)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_cloth)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_home)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_repair)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_main_cat_cafe)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_main_cat_gift)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_main_cat_education)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_main_cat_groceries)!!
        )

        ringChartView.setParameters(40f, 0.1f)

        button.setOnClickListener {
            ringChartView.setData(getListData())
        }

        btnClear.setOnClickListener {
            editText.setText("")
            ringChartView.setData(getListData())
        }
    }
    private fun getListData (): List<ChartItem> {
        val result = mutableListOf<ChartItem>()
        val inputString = editText.text.toString()
        val floatList: List<Float> = inputString
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toFloat() }
        val amounts = sortData(floatList)
        outputSum(amounts.sum())
        for (index in amounts.indices) {
            result.add(ChartItem(amounts[index], icons[index], colors[index]))
        }
        return result
    }

    // Максимальное число секторов диаграммы 8. Если число элементов в списке больше 8,
    // то элементы с 8 и до конца списка суммируются и показываются одним сектором - прочие
    private fun sortData(newData: List<Float>): List<Float> {
        val sortedList = newData.sortedDescending()
        return if (sortedList.size <= 8) {
            sortedList
        } else {
            val firstSeven = sortedList.take(7)
            val sumRest = sortedList.drop(7).sum()
            firstSeven + sumRest
        }
    }

    fun outputSum (total: Float) {
        tvTotal.text = total.toString()
    }
}