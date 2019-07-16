package com.rodentia6.view_diy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class AnalogPadView : View {
    companion object {
        private const val MIN_WIDTH = 120f //dp
        private const val C_SEAL = 0.70f
        private const val C_BALL = 0.48f
        private const val C_PLAY = 0.52f // 遊び
        private const val Q_PI: Float = (Math.PI / 4).toFloat()
        private val SNAP_RANGE = Q_PI
        private val SNAP_AXIS_8: FloatArray =
            floatArrayOf(0f, Q_PI, Q_PI * 2, Q_PI * 3, Q_PI * 4, -Q_PI * 3, -Q_PI * 2, -Q_PI)
        private val SNAP_AXIS_4: FloatArray = floatArrayOf(0f, Q_PI * 2, Q_PI * 4, -Q_PI * 2)
        private fun snapFour(rad: Float): Float {
            for (axis in SNAP_AXIS_4) {
                if (Math.abs(axis - rad) <= SNAP_RANGE) {
                    return axis
                }
            }
            return rad
        }
    }

    private val color: Int
    private val dencity: Float
    private val black: Paint
    private val ball: Paint
    private val ballHighlight: Paint
    private val centerX: Float
        get() = width.toFloat() / 2
    private val centerY: Float
        get() = height.toFloat() / 2
    private var cursorX: Float = centerX
    private var cursorY: Float = centerY


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val tArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AnalogPadView
        )
        color = tArray.getInteger(R.styleable.AnalogPadView_ball_color, Color.BLUE)
        black = Paint().apply {
            style = Paint.Style.FILL
            color = 0xff000000.toInt()
        }
        ball = Paint().apply {
            style = Paint.Style.FILL
            color = this@AnalogPadView.color
        }
        ballHighlight = Paint().apply {
            style = Paint.Style.FILL
            color = 0xffffffff.toInt()
        }

        dencity = context.resources.displayMetrics.density
        tArray.recycle()

    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val r = Math.min(centerX, centerY)
        val circleSize = r * C_SEAL
        val ballSize = r * C_BALL
        canvas.drawCircle(centerX, centerY, circleSize, black)
        val length = Math.hypot(cursorX.toDouble() - centerX.toDouble(), cursorY.toDouble() - centerY.toDouble())
        val (x, y) = if (length < r * C_PLAY) {
            cursorX to cursorY
        } else {
            val a2 = atan2(cursorY - centerY, cursorX - centerX)
            val snapped = snapFour(a2)
            //4方向スナップ
            val x2 = centerX + r * C_PLAY * cos(snapped)
            val y2 = centerY + r * C_PLAY * sin(snapped)
            x2 to y2
        }
        canvas.drawCircle(x, y, ballSize, ball)
        canvas.drawCircle(x - r * 0.22f, y - r * 0.22f, r * 0.07f, ballHighlight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mw = this.paddingLeft + this.paddingRight + MIN_WIDTH * dencity
        val mh = this.paddingTop + this.paddingBottom + MIN_WIDTH * dencity
        this.cursorX = mw
        this.cursorY = mh

        setMeasuredDimension(
            resolveSize(mw.toInt(), widthMeasureSpec),
            resolveSize(mh.toInt(), heightMeasureSpec)
        )
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                cursorX = event.x
                cursorY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                cursorX = centerX
                cursorY = centerY
                invalidate()
            }
        }
        android.util.Log.d("rod6", "${getValue()}")
        return true
    }

    fun getValue(): Pair<Float, Float> {
        return (cursorX - centerX) / centerX to (cursorY - centerY) / centerY
    }
}