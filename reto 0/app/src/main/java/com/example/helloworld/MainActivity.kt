package com.example.helloworld

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var rootView: View
    private lateinit var greetingText: TextView
    private lateinit var languageLabel: TextView
    private lateinit var hintText: TextView

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val typewriterHandler = Handler(Looper.getMainLooper())
    private var typewriterRunnable: Runnable? = null

    private var currentIndex = 0
    private var currentBackgroundColor = Color.BLACK
    private var lastShakeTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.main)
        greetingText = findViewById(R.id.greetingText)
        languageLabel = findViewById(R.id.languageLabel)
        hintText = findViewById(R.id.hintText)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val (initialGreeting, initialIndex) = Greetings.all.first() to 0
        currentBackgroundColor = initialGreeting.backgroundColor
        rootView.setBackgroundColor(currentBackgroundColor)
        displayGreeting(initialGreeting, initialIndex, animateBackground = false)
        playEntranceAnimation()

        rootView.setOnClickListener {
            val (nextGreeting, nextIndex) = Greetings.next(currentIndex)
            displayGreeting(nextGreeting, nextIndex, animateBackground = true)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        typewriterHandler.removeCallbacksAndMessages(null)
    }

    private fun displayGreeting(greeting: Greeting, index: Int, animateBackground: Boolean) {
        currentIndex = index
        val textColor = contrastColorFor(greeting.backgroundColor)
        greetingText.setTextColor(textColor)
        languageLabel.setTextColor(textColor)
        hintText.setTextColor(textColor)
        languageLabel.text = greeting.languageLabel

        typewrite(greetingText, greeting.text)

        if (animateBackground) {
            animateBackgroundTo(greeting.backgroundColor)
        } else {
            currentBackgroundColor = greeting.backgroundColor
            rootView.setBackgroundColor(currentBackgroundColor)
        }
    }

    private fun typewrite(view: TextView, fullText: String, charDelayMs: Long = 45L) {
        typewriterRunnable?.let { typewriterHandler.removeCallbacks(it) }
        view.text = ""
        var charIndex = 0
        val runnable = object : Runnable {
            override fun run() {
                charIndex++
                view.text = fullText.substring(0, charIndex)
                if (charIndex < fullText.length) {
                    typewriterHandler.postDelayed(this, charDelayMs)
                }
            }
        }
        typewriterRunnable = runnable
        typewriterHandler.post(runnable)
    }

    private fun animateBackgroundTo(newColor: Int) {
        ValueAnimator.ofArgb(currentBackgroundColor, newColor).apply {
            duration = 400
            addUpdateListener { rootView.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
        currentBackgroundColor = newColor
    }

    private fun playEntranceAnimation() {
        greetingText.alpha = 0f
        greetingText.scaleX = 0.85f
        greetingText.scaleY = 0.85f
        val fadeIn = ObjectAnimator.ofFloat(greetingText, View.ALPHA, 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(greetingText, View.SCALE_X, 0.85f, 1f)
        val scaleY = ObjectAnimator.ofFloat(greetingText, View.SCALE_Y, 0.85f, 1f)
        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 450
            interpolator = OvershootInterpolator()
            start()
        }

        languageLabel.alpha = 0f
        hintText.alpha = 0f
        ObjectAnimator.ofFloat(languageLabel, View.ALPHA, 0f, 1f).apply {
            startDelay = 300
            duration = 400
            start()
        }
        ObjectAnimator.ofFloat(hintText, View.ALPHA, 0f, 1f).apply {
            startDelay = 500
            duration = 400
            start()
        }
    }

    private fun contrastColorFor(backgroundColor: Int): Int {
        val luminance = (0.299 * Color.red(backgroundColor) +
            0.587 * Color.green(backgroundColor) +
            0.114 * Color.blue(backgroundColor)) / 255
        return if (luminance < 0.5) Color.WHITE else Color.BLACK
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > SHAKE_SLOP_TIME_MS) {
                lastShakeTimestamp = now
                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun onShakeDetected() {
        vibrate()
        val (randomGreeting, randomIndex) = Greetings.random(currentIndex)
        displayGreeting(randomGreeting, randomIndex, animateBackground = true)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    private companion object {
        const val SHAKE_THRESHOLD_GRAVITY = 2.5f
        const val SHAKE_SLOP_TIME_MS = 500L
    }
}
