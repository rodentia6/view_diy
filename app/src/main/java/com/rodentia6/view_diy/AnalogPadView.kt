package com.rodentia6.view_diy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*


class AnalogPadView : View {
    companion object {
        private const val MIN_WIDTH = 120f //dp
        private const val C_SEAL = 0.70f
        private const val C_BALL = 0.48f
        private const val C_PLAY = 0.52f // 遊び
        private const val Q_PI: Float = (Math.PI / 4).toFloat()
        private val SNAP_RANGE = Q_PI / 2
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
    private val radius: Float
        get() = Math.min(centerX, centerY)
    private var valueX: Float = 0f
    private var valueY: Float = 0f

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

        val circleSize = radius * C_SEAL
        val ballSize = radius * C_BALL
        canvas.drawCircle(centerX, centerY, circleSize, black)
        canvas.drawCircle(centerX + radius * C_PLAY * valueX, centerY + radius * C_PLAY * valueY, ballSize, ball)
        canvas.drawCircle(
            centerX + radius * C_PLAY * valueX - radius * 0.22f,
            centerY + radius * C_PLAY * valueY - radius * 0.22f,
            radius * 0.07f,
            ballHighlight
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mw = this.paddingLeft + this.paddingRight + MIN_WIDTH * dencity
        val mh = this.paddingTop + this.paddingBottom + MIN_WIDTH * dencity
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
                val p = snapCursorFour(event.x to event.y)
                valueX = p.first
                valueY = p.second
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                valueX = 0f
                valueY = 0f
                invalidate()
            }
        }
        //android.util.Log.d("rod6", "${getValue()}")
        return true
    }

    private fun snapCursorFour(cursor: Pair<Float, Float>): Pair<Float, Float> {
        val playRad = radius * C_PLAY
        val (cx, cy) = cursor
        val vox = (cx - centerX) / playRad
        val voy = (cy - centerY) / playRad
        val length = hypot(vox.toDouble(), voy.toDouble())
        if (length < 1) {
            return vox to voy
        }

        val a2 = atan2(voy, vox)

        //4方向スナップ
        when {
            abs(a2) < SNAP_RANGE -> return 1f to 0f
            abs(a2 - Q_PI * 2) < SNAP_RANGE -> return 0f to 1f
            abs(a2 - Q_PI * 4) < SNAP_RANGE ||
                    abs(a2 + Q_PI * 4) < SNAP_RANGE -> return -1f to 0f
            abs(a2 + Q_PI * 2) < SNAP_RANGE -> return 0f to -1f
        }
        val x2 = cos(a2)
        val y2 = sin(a2)
        return x2 to y2
    }

    fun getValue(): Pair<Float, Float> {
        return valueX to valueY
    }
}