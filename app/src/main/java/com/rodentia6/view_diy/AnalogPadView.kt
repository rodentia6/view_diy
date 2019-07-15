package com.rodentia6.view_diy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.FloatRange
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class AnalogPadView : View {
    companion object {
        private const val MIN_WIDTH = 120f //dp
        private const val C_SEAL = 0.75f
        private const val C_BALL = 0.52f
        private const val C_PLAY = 0.63f // 遊び
        private const val C_SNAP = 0.70f // 遊びを超えたときのスナップ先
    }

    private val color: Int
    private val dencity: Float
    private val black: Paint
    private val maru: Paint
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
        color = tArray.getInteger(R.styleable.AnalogPadView_fuga_color, Color.BLUE)
        black = Paint().apply {
            style = Paint.Style.FILL
            color = 0xff000000.toInt()
        }
        maru = Paint().apply {
            style = Paint.Style.FILL
            color = this@AnalogPadView.color
        }

        dencity = context.resources.displayMetrics.density
        //android.util.Log.d("rod6", "dencity: $dencity")
        tArray.recycle()

    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        //canvas.drawCircle(MIN_WIDTH/2, MIN_WIDTH/2, MIN_WIDTH/2, black)
        val r = Math.min(centerX, centerY)
        val circleSize = r * C_SEAL
        val ballSize = r * C_BALL
        canvas.drawCircle(centerX, centerY, circleSize, black)
        val length = Math.hypot(cursorX.toDouble() - centerX.toDouble(), cursorY.toDouble() - centerY.toDouble())
        if (length < r * C_PLAY) {
            canvas.drawCircle(cursorX, cursorY, ballSize, maru)
        } else {
            val a2 = atan2(cursorY - centerY, cursorX - centerX)
            //4方向スナップ
            val (x, y) = when {
                Math.abs(a2) <= Math.PI / 4 -> 1 to 0
                Math.PI * 3 / 4 <= Math.abs(a2) -> -1 to 0
                (Math.PI / 4 <= a2 && a2 < Math.PI * 3 / 4) -> 0 to 1
                else -> 0 to -1
            }
            canvas.drawCircle(centerX + r * x * C_SNAP, centerY + r * y * C_SNAP, ballSize, maru)
            //android.util.Log.d("rod6", "a2: $a2")
//            val x = centerX + r * C_SNAP * cos(a2)
//            val y = centerY + r * C_SNAP * sin(a2)
//            canvas.drawCircle(x, y, ballSize, maru)
        }
        android.util.Log.d("rod6", "$length, ${r * C_PLAY}")
    }


    /**
     * ビューの大きさの調整
     * @param widthMeasureSpec 幅の注文
     * @param heightMeasureSpec 高さの注文
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // コントロールの表示に必要な大きさを計算する。
        // このカスタムビューにはコンテンツの決まった大きさはないので、最小限 30dp必要ということで計算。
        val mw = this.paddingLeft + this.paddingRight + MIN_WIDTH * dencity
        val mh = this.paddingTop + this.paddingBottom + MIN_WIDTH * dencity
        this.cursorX = mw/2
        this.cursorY = mh/2

        // resolveSizeに渡すとよきに計らってくれる。
        setMeasuredDimension(
            resolveSize(mw.toInt(), widthMeasureSpec),
            resolveSize(mh.toInt(), heightMeasureSpec)
        )
        invalidate()
        //android.util.Log.d("rod6", "w,h : $width, $height")
    }

//    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        event ?: return false
//        val touchX = event.x.toDouble()
//        val touchY = event.y.toDouble()
//        val r = width.toDouble() / 2
//
//        val length = Math.hypot(touchX - r, touchY - r)
//
//        return if (length <= r) super.dispatchTouchEvent(event) else false
//    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        //android.util.Log.d("rod6", "ontouch() event: $event")
        //android.util.Log.d("rod6", "w,h : $width, $height")
        //android.util.Log.d("rod6", "ontouch() event: ${event.action}")
        if (event.edgeFlags != 0x00) {
            cursorX = centerX
            cursorY = centerY
            invalidate()
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                //android.util.Log.d("rod6", "ontouch() event: $event")
                cursorX = event.x
                cursorY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                //android.util.Log.d("rod6", "ontouch() event: $event")
                cursorX = centerX
                cursorY = centerY
                invalidate()
            }

        }
        return true
    }
}