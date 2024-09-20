package com.pantyetta.android_acceleration

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pantyetta.android_acceleration.ui.theme.Android_accelerationTheme
import java.time.LocalTime

class MainActivity : ComponentActivity(), SensorEventListener {
    lateinit var sensorManager: SensorManager
    var AccSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_accelerationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        AccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, AccSensor, SensorManager.SENSOR_DELAY_UI)
        clockStart = System.nanoTime()
    }

    override fun onPause() {
        super.onPause()
        //リスナーを解除しないとバックグラウンドにいるとき常にコールバックされ続ける
        sensorManager.unregisterListener(this)
    }
    var clockStart: Long = 0
    var clockLatest: Long? = null

    var SensorX: MutableList<Float> = mutableListOf()
    var SensorY: MutableList<Float> = mutableListOf()
    var SensorZ: MutableList<Float> = mutableListOf()

    var SensorTimer: MutableList<Float> = mutableListOf()


    fun distance(){
        var previousVelocity = listOf(0.0, 0.0, 0.0)
        var totalDistance = listOf(0.0, 0.0, 0.0)

        for (i in SensorX.indices) {

            // 現在の速度を計算
            val currentVelocity = listOf(previousVelocity[0] + SensorX[i] * SensorTimer[i], previousVelocity[1] + SensorY[i] * SensorTimer[i], previousVelocity[2] + SensorZ[i] * SensorTimer[i])

            // 現在の距離を計算
            val distance = previousVelocity.zip(currentVelocity) { a, b -> (a + b) / 2 * SensorTimer[i] }
            totalDistance = totalDistance.zip(distance) {a,b -> a+b}

            // 次のイテレーションのために速度を更新Ï
            previousVelocity = currentVelocity
        }

        // 結果を出力
        println("\n\n移動距離: ${totalDistance[0]}  ${totalDistance[1]}  ${totalDistance[2]}\n\n\n")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Remove the gravity contribution with the high-pass filter.
        if (event?.sensor?.type === Sensor.TYPE_LINEAR_ACCELERATION) {

            val now = System.nanoTime()
            if(now > clockStart + (1000 * 1000 * 1000).toLong() * 5){
                sensorManager.unregisterListener(this)
                distance()

                clockLatest = null

                SensorX= mutableListOf()
                SensorY = mutableListOf()
                SensorZ= mutableListOf()
                SensorTimer = mutableListOf()
//
                sensorManager.registerListener(this, AccSensor, SensorManager.SENSOR_DELAY_UI)
                clockStart = System.nanoTime()
                return
            }
            SensorX.add(event.values[0])
            SensorY.add(event.values[1])
            SensorZ.add(event.values[2])
            SensorTimer.add((now - (clockLatest?: clockStart)) / (1000 * 1000 * 1000).toFloat())
            clockLatest = now

//            println("${SensorTimer.lastOrNull()} ${SensorX.lastOrNull()} ${SensorY.lastOrNull()} ${SensorZ.lastOrNull()}")

            // TODO
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {

    Text(
        text = "Hello!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_accelerationTheme {
        Greeting()
    }
}