package rygel.cn.utils.wheeldemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAnim).setOnClickListener {
            findViewById<WheelController>(R.id.wheelController).run {
                setCurValueWithAnim((120 * Math.random()).toInt())
            }
        }
        findViewById<Button>(R.id.btnSkipCallback).setOnClickListener {
            findViewById<WheelController>(R.id.wheelController).run {
                skipCallbackOnce()
            }
        }
        findViewById<WheelController>(R.id.wheelController).setOnValueChangeListener(object : WheelController.OnValueChangeLister{

            override fun onValueSelected(value: Int) {
                Toast.makeText(this@MainActivity, "select : $value", Toast.LENGTH_SHORT).show()
            }

            override fun onValueChanged(value: Int) {
                findViewById<TextView>(R.id.tvValue).text = value.toString()
            }
        })
    }
}
