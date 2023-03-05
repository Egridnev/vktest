package com.vktest.analogclockview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


class AnalogClockView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private var clockCircleColor = 0
    private var minuteCircleColor = 0
    private var hourlyMarksColor = 0
    private var minuteMarksColor = 0
    private var numberTextColor = Color.BLACK
    private var hourlyHandColor = 0
    private var minuteHandColor = 0
    private var secondsHandColor = 0
    private var centerCircleColor = 0

    private var minuteCircleWidth = 2f
    private var hourlyMarksWidth = 3f
    private var minuteMarksWidth = 1f
    private var numberTextSize = 18f
    private var hourlyHandWidth = 5f
    private var minuteHandWidth = 3f
    private var secondsHandWidth = 1f
    private var centerCircleRadius = 0f

    private var showHourlyMarks = true
    private var showMinuteMarks = true
    private var showSecondsHand = true

    private val minWidth = dipToPx(MIN_WIDTH_DP).toInt()
    private val minHeight = dipToPx(MIN_HEIGHT_DP).toInt()

    private val paintRect: Rect = Rect()

    private var refreshRectLeft = 0
    private var refreshRectTop = 0
    private var refreshRectRight = 0
    private var refreshRectBottom = 0

    private var hour = 0f
    private var minute = 0f
    private var second = 0f
    private var milliSecond = 0

    private val clockCirclePaint: Paint = Paint()
    private val minuteCirclePaint: Paint = Paint()
    private val hourlyMarksPaint: Paint = Paint()
    private val minuteMarksPaint: Paint = Paint()
    private val numberPaint = TextPaint()
    private val hourlyHandPaint: Paint = Paint()
    private val minuteHandPaint: Paint = Paint()
    private val secondsHandPaint: Paint = Paint()
    private val centerCirclePaint: Paint = Paint()

    private var calendar: Calendar? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        initAttributes(context, attrs)
        initPaint()
        initAnimation()
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val attr: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.AnalogClockView)

        clockCircleColor = attr.getColor(R.styleable.AnalogClockView_clockCircleColor, DEFAULT_CLOCK_CIRCLE_COLOR)
        minuteCircleColor = attr.getColor(R.styleable.AnalogClockView_minuteCircleColor, DEFAULT_MINUTE_CIRCLE_COLOR)
        hourlyMarksColor = attr.getColor(R.styleable.AnalogClockView_hourlyMarksColor, DEFAULT_HOURLY_MARKS_COLOR)
        minuteMarksColor = attr.getColor(R.styleable.AnalogClockView_minuteMarksColor, DEFAULT_MINUTE_MARKS_COLOR)
        hourlyHandColor = attr.getColor(R.styleable.AnalogClockView_hourlyHandColor, DEFAULT_HOURLY_HAND_COLOR)
        minuteHandColor = attr.getColor(R.styleable.AnalogClockView_minuteHandColor, DEFAULT_MINUTE_HAND_COLOR)
        secondsHandColor = attr.getColor(R.styleable.AnalogClockView_secondsHandColor, DEFAULT_SECONDS_HAND_COLOR)
        centerCircleColor = attr.getColor(R.styleable.AnalogClockView_centerCircleColor, DEFAULT_CENTER_CIRCLE_COLOR)

        centerCircleRadius = attr.getDimension(
            R.styleable.AnalogClockView_centerCircleRadius,
            DEFAULT_CENTER_CIRCLE_RADIUS
        )

        showHourlyMarks = attr.getBoolean(R.styleable.AnalogClockView_showHourlyMarks, showHourlyMarks)
        showMinuteMarks = attr.getBoolean(R.styleable.AnalogClockView_showMinuteMarks, showMinuteMarks)
        showSecondsHand = attr.getBoolean(R.styleable.AnalogClockView_showSecondsHand, showSecondsHand)

        attr.recycle()
    }

    private fun initPaint() {
        clockCirclePaint.isAntiAlias = true
        clockCirclePaint.color = clockCircleColor
        clockCirclePaint.style = Paint.Style.FILL

        minuteCirclePaint.isAntiAlias = true
        minuteCirclePaint.color = minuteCircleColor
        minuteCirclePaint.style = Paint.Style.STROKE
        minuteCirclePaint.strokeWidth = minuteCircleWidth

        hourlyMarksPaint.isAntiAlias = true
        hourlyMarksPaint.color = hourlyMarksColor
        hourlyMarksPaint.style = Paint.Style.STROKE
        hourlyMarksPaint.strokeWidth = hourlyMarksWidth

        minuteMarksPaint.isAntiAlias = true
        minuteMarksPaint.color = minuteMarksColor
        minuteMarksPaint.style = Paint.Style.STROKE
        minuteMarksPaint.strokeWidth = minuteMarksWidth

        numberPaint.isAntiAlias = true
        numberPaint.color = numberTextColor
        numberPaint.textSize = numberTextSize
        numberPaint.textAlign = Paint.Align.CENTER

        hourlyHandPaint.isAntiAlias = true
        hourlyHandPaint.color = hourlyHandColor
        hourlyHandPaint.style = Paint.Style.STROKE
        hourlyHandPaint.strokeWidth = hourlyHandWidth

        minuteHandPaint.isAntiAlias = true
        minuteHandPaint.color = minuteHandColor
        minuteHandPaint.style = Paint.Style.STROKE
        minuteHandPaint.strokeWidth = minuteHandWidth

        secondsHandPaint.isAntiAlias = true
        secondsHandPaint.color = secondsHandColor
        secondsHandPaint.style = Paint.Style.STROKE
        secondsHandPaint.strokeWidth = secondsHandWidth

        centerCirclePaint.isAntiAlias = true
        centerCirclePaint.color = centerCircleColor
        centerCirclePaint.style = Paint.Style.FILL
    }

    private fun initAnimation() {
        calendar = Calendar.getInstance()
        val hour: Float = calendar?.get(Calendar.HOUR)!!.toFloat()
        val minute: Float = calendar?.get(Calendar.MINUTE)!!.toFloat()
        val second: Float = calendar?.get(Calendar.SECOND)!! + (DEFAULT_ANIMATION_DURATION_MILLI + DEFAULT_ANIM_START_DELAY_MILLI) / 1000.0f
        val milliSecond = (1000 * second + calendar?.get(Calendar.MILLISECOND)!!).toInt()
        val hourAnimator = ValueAnimator.ofFloat(0f, hour)
        val minuteAnimator = ValueAnimator.ofFloat(0f, minute)
        val secondAnimator = ValueAnimator.ofFloat(0f, second)
        val milliSecondAnimator = ValueAnimator.ofInt(0, milliSecond)
        hourAnimator.addUpdateListener { animation ->
            this.hour = animation.animatedValue as Float
        }
        minuteAnimator.addUpdateListener { animation ->
            this.minute = animation.animatedValue as Float
        }
        secondAnimator.addUpdateListener { animation ->
            this.second = animation.animatedValue as Float
        }
        milliSecondAnimator.addUpdateListener { animation ->
            this.milliSecond = animation.animatedValue as Int
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int = if (widthMode == MeasureSpec.EXACTLY) { widthSize } else { minWidth }
        val height: Int = if (heightMode == MeasureSpec.EXACTLY) { heightSize } else { minHeight }
        val centerX = width / 2
        val centerY = height / 2
        val rectSize = min(width, height)

        paintRect.set(
            centerX - rectSize / 2,
            centerY - rectSize / 2,
            centerX + rectSize / 2,
            centerY + rectSize / 2
        )

        setMeasuredDimension(width, height)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(paintRect.centerX().toFloat(), paintRect.centerY().toFloat())
        drawClockFace(canvas)
        drawMinuteCircle(canvas)
        if (showHourlyMarks) {
            drawHourlyMarks(canvas)
        }
        if (showMinuteMarks) {
            drawMinuteMarks(canvas)
        }

        calendar = Calendar.getInstance()
        hour = calendar?.get(Calendar.HOUR)!!.toFloat()
        minute = calendar?.get(Calendar.MINUTE)!!.toFloat()
        second = calendar?.get(Calendar.SECOND)!!.toFloat()
        milliSecond = (1000 * second + calendar?.get(Calendar.MILLISECOND)!!).toInt()

        drawHourHand(canvas, hour, minute, second)
        drawMinuteHand(canvas, minute, second)

        if (showSecondsHand) {
            drawSecondsHand(canvas, milliSecond)
        }

        canvas.drawCircle(0f, 0f, centerCircleRadius, centerCirclePaint)

        postInvalidate(
            refreshRectLeft + paintRect.centerX() - hourlyMarksWidth.toInt(),
            refreshRectTop + paintRect.centerY() - hourlyMarksWidth.toInt(),
            refreshRectRight + paintRect.centerX() + hourlyMarksWidth.toInt(),
            refreshRectBottom + paintRect.centerY() + hourlyMarksWidth.toInt()
        )

        refreshRectBottom = 0
        refreshRectTop = refreshRectBottom
        refreshRectRight = refreshRectTop
        refreshRectLeft = refreshRectRight

    }

    private fun drawClockFace(canvas: Canvas) {
        canvas.drawCircle(0f, 0f, (paintRect.width() / 2).toFloat(), clockCirclePaint)
    }

    private fun drawHourlyMarks(canvas: Canvas) {
        val radius: Int = paintRect.width() / 2
        var degree = 0

        while (degree < 360) {
            val radian = degree * Math.PI / 180
            canvas.drawLine(
                radius * cos(radian).toFloat(),
                radius * sin(radian).toFloat(),
                (radius - DEFAULT_HOURLY_MARKS_LENGTH) * cos(radian).toFloat(),
                (radius - DEFAULT_HOURLY_MARKS_LENGTH) * sin(radian).toFloat(),
                hourlyMarksPaint
            )
            degree += 30
        }
    }

    private fun drawMinuteMarks(canvas: Canvas) {
        val radius: Int = paintRect.width() / 2
        var degree = 0

        while (degree < 360) {
            if (degree % 30 == 0) {
                degree += 6
                continue
            }
            val radian = degree * Math.PI / 180
            canvas.drawLine(
                radius * cos(radian).toFloat(),
                radius * sin(radian).toFloat(),
                (radius - DEFAULT_MINUTE_MARKS_LENGTH) * cos(radian).toFloat(),
                (radius - DEFAULT_MINUTE_MARKS_LENGTH) * sin(radian).toFloat(),
                minuteMarksPaint
            )
            degree += 6
        }
    }

    private fun drawMinuteCircle(canvas: Canvas) {
        val radius: Int = paintRect.width() / 2
        canvas.drawCircle(0f, 0f, radius - DEFAULT_MINUTE_MARKS_LENGTH, minuteCirclePaint)
    }

    private fun drawHourHand(canvas: Canvas, hour: Float, minute: Float, second: Float) {
        val fm: Paint.FontMetrics = numberPaint.fontMetrics
        val numberHeight: Float = -fm.ascent + fm.descent
        val radius = (paintRect.width() / 2 - DEFAULT_HOURLY_MARKS_LENGTH - numberHeight - fm.bottom - dipToPx(5f)).toInt()
        val radian = (hour - 3) * Math.PI / 6 + minute * Math.PI / 360 + second * Math.PI / 21600
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()

        canvas.drawLine(0f, 0f, stopX, stopY, hourlyHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun drawMinuteHand(canvas: Canvas, minute: Float, second: Float) {
        val radius = (paintRect.width() / 2 - DEFAULT_MINUTE_MARKS_LENGTH - dipToPx(5f)).toInt()
        val radian = (minute - 15) * Math.PI / 30 + second * Math.PI / 1800
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()

        canvas.drawLine(0f, 0f, stopX, stopY, minuteHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun drawSecondsHand(canvas: Canvas, milliSecond: Int) {
        val radius: Int = paintRect.width() / 2
        val radian = (milliSecond - 15000) * Math.PI / 30000
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()

        canvas.drawLine(0f, 0f, stopX, stopY, secondsHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun setRefreshRectCoordinates(x: Int, y: Int) {
        refreshRectLeft = min(refreshRectLeft, x)
        refreshRectTop = min(refreshRectTop, y)
        refreshRectRight = max(refreshRectRight, x)
        refreshRectBottom = max(refreshRectBottom, y)
    }

    companion object {
        private const val MIN_WIDTH_DP = 50f
        private const val MIN_HEIGHT_DP = 50f

        private val DEFAULT_CLOCK_CIRCLE_COLOR: Int = Color.parseColor("#A9ADB0")
        private val DEFAULT_MINUTE_CIRCLE_COLOR: Int = Color.BLACK
        private val DEFAULT_HOURLY_MARKS_COLOR: Int = Color.BLACK
        private val DEFAULT_MINUTE_MARKS_COLOR: Int = Color.BLACK
        private val DEFAULT_HOURLY_HAND_COLOR: Int = Color.BLACK
        private val DEFAULT_MINUTE_HAND_COLOR: Int = Color.BLACK
        private val DEFAULT_SECONDS_HAND_COLOR: Int = Color.BLACK
        private val DEFAULT_CENTER_CIRCLE_COLOR: Int = Color.BLACK

        private const val DEFAULT_ANIMATION_DURATION_MILLI = 1200
        private const val DEFAULT_ANIM_START_DELAY_MILLI = 500

        private val DEFAULT_HOURLY_MARKS_LENGTH = dipToPx(20f)
        private val DEFAULT_MINUTE_MARKS_LENGTH = dipToPx(10f)
        private val DEFAULT_CENTER_CIRCLE_RADIUS = dipToPx(5f)

        private fun dipToPx(dipValue: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                Resources.getSystem().displayMetrics
            )
        }
    }
}