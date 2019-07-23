package rygel.cn.utils.wheeldemo

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import kotlin.math.*

/**
 * 圆形SeekBar
 * @author Rygel
 */
class WheelController : View {

    companion object {
        private const val TAG = "WheelController"
    }

    private var controllerMargin = 8F
    private var outerRadius = 320F
    private var innerRadius = 240F
    private var bigCalibrationLength = 28F
    private var smallCalibrationLength = 16F
    private var calibrationMargin = 8F
    private var bigCalibrationWidth = 6F
    private var smallCalibrationWidth = 3F

    private var calibrationTextSize = 36F
    private var calibrationTextMargin = 24F

    private var textSize = 72F

    private var bgColor = Color.parseColor("#EEEEEE")
    private var maxColor = Color.parseColor("#FB8C00")
    private var minColor = Color.parseColor("#FFF8E1")

    private var textColor = Color.parseColor("#333333")

    private var startColor = Color.parseColor("#FF7043")
    private var endColor = Color.parseColor("#FFFFFF")

    private var calibrationColor = Color.parseColor("#8D8D8D")

    private var max = 100
    private var min = 0

    private var smallStep = 7.2F
    private var bigStep = 36F

    // 此处采用Float类型是为了滑动更加流畅，但是暴露给外面的依旧是Int类型
    private var curValue = 97F

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        obtainAttrs(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainAttrs(attrs)
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val endPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val calibrationTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val bCalibrationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sCalibrationPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val arcBound : RectF = RectF()
    private val textBound = Rect()

    private var animator : ObjectAnimator = ObjectAnimator.ofFloat(this, "curValue",0F)

    private var downTime = 0L
    private var tempPath = Path()

    private var listener : OnValueChangeLister? = null

    init {
        bgPaint.color = bgColor
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = outerRadius - innerRadius
        startPaint.color = startColor
        endPaint.color = endColor

        bCalibrationPaint.style = Paint.Style.STROKE
        sCalibrationPaint.style = Paint.Style.STROKE
        bCalibrationPaint.strokeWidth = bigCalibrationWidth
        sCalibrationPaint.strokeWidth = smallCalibrationWidth
        bCalibrationPaint.color = calibrationColor
        sCalibrationPaint.color = calibrationColor

        calibrationTextPaint.color = textColor
        calibrationTextPaint.textAlign = Paint.Align.CENTER
        calibrationTextPaint.textSize = calibrationTextSize

        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true

        fgPaint.shader = generateGradient()
        fgPaint.style = Paint.Style.STROKE
        fgPaint.strokeWidth = outerRadius - innerRadius
        fgPaint.strokeCap = Paint.Cap.ROUND

        animator.duration = 300
        animator.setAutoCancel(true)
    }

    /************************************** setters *******************************************/
    fun setControllerMargin(margin: Float) {
        controllerMargin = margin
        postInvalidate()
    }

    fun setOuterRadius(radius: Float) {
        outerRadius = radius
        bgPaint.strokeWidth = outerRadius - innerRadius
        fgPaint.strokeWidth = outerRadius - innerRadius
        postInvalidate()
    }

    fun setInnerRadius(radius: Float) {
        innerRadius = radius
        bgPaint.strokeWidth = outerRadius - innerRadius
        fgPaint.strokeWidth = outerRadius - innerRadius
        postInvalidate()
    }

    fun setSmallCalibrationLength(length: Float) {
        smallCalibrationLength = length
        postInvalidate()
    }

    fun setBigCalibrationLength(length: Float) {
        bigCalibrationLength = length
        postInvalidate()
    }

    fun setCalibrationMargin(margin: Float) {
        calibrationMargin = margin
        postInvalidate()
    }

    fun setBigCalibrationWidth(width: Float) {
        bigCalibrationWidth = width
        bCalibrationPaint.strokeWidth = bigCalibrationWidth
        postInvalidate()
    }

    fun setSmallCalibrationWidth(width: Float) {
        smallCalibrationWidth = width
        sCalibrationPaint.strokeWidth = smallCalibrationWidth
        postInvalidate()
    }

    fun setCalibrationTextSize(size: Float) {
        calibrationTextSize = size
        calibrationTextPaint.textSize = calibrationTextSize
        postInvalidate()
    }

    fun setCalibrationTextMargin(margin: Float) {
        calibrationTextMargin = margin
        postInvalidate()
    }

    fun setTextSize(size: Float) {
        textSize = size
        textPaint.textSize = textSize
        postInvalidate()
    }

    fun setBgColor(color: Int) {
        bgColor = color
        bgPaint.color = bgColor
        postInvalidate()
    }

    fun setMaxColor(color: Int) {
        maxColor = color
        fgPaint.shader = generateGradient()
        postInvalidate()
    }

    fun setMinColor(color: Int) {
        minColor = color
        fgPaint.shader = generateGradient()
        postInvalidate()
    }

    fun setTextColor(color: Int) {
        textColor = color
        textPaint.color = textColor
        calibrationTextPaint.color = textColor
        postInvalidate()
    }

    fun setStartColor(color: Int) {
        startColor = color
        startPaint.color = startColor
        postInvalidate()
    }

    fun setEndColor(color: Int) {
        endColor = color
        endPaint.color = endColor
        postInvalidate()
    }

    fun setCalibrationColor(color: Int) {
        calibrationColor = color
        bCalibrationPaint.color = calibrationColor
        sCalibrationPaint.color = calibrationColor
        postInvalidate()
    }

    fun setMax(max: Int) {
        this.max = max
        postInvalidate()
    }

    fun setMin(min: Int) {
        this.min = min
        postInvalidate()
    }

    fun setSmallStep(step: Float) {
        smallStep = step
        postInvalidate()
    }

    fun setBigStep(step: Float) {
        bigStep = step
        postInvalidate()
    }

    fun setOnValueChangeListener(listener: OnValueChangeLister) {
        this.listener = listener
    }

    fun setCurValue(value : Int) {
        if (!checkBounds(value)) {
            Log.e(TAG, "value : $value out of bound!")
            return
        }
        curValue = value.toFloat()
        listener?.onValueChanged(curValue.toInt())
        postInvalidate()
    }

    private fun setCurValue(value : Float) {
        if (!checkBounds(value.toInt())) {
            Log.e(TAG, "value : $value out of bound!")
            return
        }
        curValue = value
        listener?.onValueChanged(curValue.toInt())
        postInvalidate()
    }

    fun getCurValue() : Int = curValue.toInt()

    fun obtainAttrs(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.WheelController)
        setControllerMargin(array.getDimension(R.styleable.WheelController_controllerMargin, controllerMargin))
        setOuterRadius(array.getDimension(R.styleable.WheelController_outerRadius, outerRadius))
        setInnerRadius(array.getDimension(R.styleable.WheelController_innerRadius, innerRadius))
        setBigCalibrationLength(array.getDimension(R.styleable.WheelController_bigCalibrationLength, bigCalibrationLength))
        setSmallCalibrationLength(array.getDimension(R.styleable.WheelController_smallCalibrationLength, smallCalibrationLength))
        setCalibrationMargin(array.getDimension(R.styleable.WheelController_calibrationMargin, calibrationMargin))
        setBigCalibrationWidth(array.getDimension(R.styleable.WheelController_bigCalibrationWidth, bigCalibrationWidth))
        setSmallCalibrationWidth(array.getDimension(R.styleable.WheelController_smallCalibrationWidth, smallCalibrationWidth))
        setCalibrationTextSize(array.getDimension(R.styleable.WheelController_calibrationTextSize, calibrationTextSize))
        setCalibrationTextMargin(array.getDimension(R.styleable.WheelController_calibrationTextMargin, calibrationTextMargin))
        setTextSize(array.getDimension(R.styleable.WheelController_valueTextSize, textSize))

        setBgColor(array.getColor(R.styleable.WheelController_bgColor, bgColor))
        setMaxColor(array.getColor(R.styleable.WheelController_maxColor, maxColor))
        setMinColor(array.getColor(R.styleable.WheelController_minColor, minColor))
        setTextColor(array.getColor(R.styleable.WheelController_valueTextColor, textColor))
        setStartColor(array.getColor(R.styleable.WheelController_startColor, startColor))
        setEndColor(array.getColor(R.styleable.WheelController_endColor, endColor))
        setCalibrationColor(array.getColor(R.styleable.WheelController_calibrationColor, calibrationColor))

        setMax(array.getInteger(R.styleable.WheelController_max, max))
        setMin(array.getInteger(R.styleable.WheelController_min, min))
        setCurValue(array.getInteger(R.styleable.WheelController_curValue, curValue.toInt()))

        setSmallStep(array.getFloat(R.styleable.WheelController_smallStep, smallStep))
        setBigStep(array.getFloat(R.styleable.WheelController_bigStep, bigStep))
        array.recycle()
    }

    /**
     * 重写on measure
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = outerRadius.toInt() * 2
        var height = outerRadius.toInt() * 2
        MeasureSpec.UNSPECIFIED
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {

        } else if (widthMode == MeasureSpec.AT_MOST) {
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            width = widthSize
        } else {
            height = heightSize
            width = widthSize
        }
        setMeasuredDimension(width, height)
        arcBound.top = fgPaint.strokeWidth / 2
        arcBound.left = fgPaint.strokeWidth / 2
        arcBound.bottom = height.toFloat() - fgPaint.strokeWidth / 2
        arcBound.right = width.toFloat() - fgPaint.strokeWidth / 2
    }

    /**
     * 自定义绘制
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        var endAngle = (curValue - min) * 360.toFloat() / (max - min)
        var startAngle = calculateStartAngle().toFloat()
        var offset = abs(endAngle - 360)
        if (offset <= startAngle) {
            startAngle -= startAngle - offset
        }
        canvas.save()
        canvas.drawCircle(outerRadius, outerRadius, outerRadius - bgPaint.strokeWidth / 2, bgPaint)
        // 绘制弧形进度条
        canvas.rotate(-90F - startAngle, outerRadius, outerRadius)
        canvas.drawArc(arcBound, calculateStartAngle().toFloat(), endAngle, false, fgPaint)
        canvas.restore()

        // 绘制刻度
        canvas.drawPath(generateBigCalibrationPath(), bCalibrationPaint)
        canvas.drawPath(generateSmallCalibrationPath(), sCalibrationPaint)
        drawCalibrationText(canvas)

        // 绘制控制按钮
        canvas.drawCircle(outerRadius, bgPaint.strokeWidth / 2, bgPaint.strokeWidth / 2 - controllerMargin, startPaint)
        canvas.drawCircle(calculateXByAngleAndRadius(endAngle, innerRadius + bgPaint.strokeWidth / 2),
            calculateYByAngleAndRadius(endAngle, innerRadius + bgPaint.strokeWidth / 2),
            bgPaint.strokeWidth / 2 - controllerMargin, endPaint)

        textPaint.getTextBounds(curValue.toInt().toString(), 0, curValue.toInt().toString().length, textBound)
        canvas.drawText(curValue.toInt().toString(), outerRadius, outerRadius - textBound.centerY(), textPaint)
    }

    /**
     * 绘制表盘数值
     */
    private fun drawCalibrationText(canvas: Canvas) {
        var angle = 0F
        while (angle < 360F) {
            val str = (min + angle * (max - min) / 360F).toInt().toString()
            calibrationTextPaint.getTextBounds(str, 0, str.length, textBound)
            canvas.drawText(str,
                calculateXByAngleAndRadius(angle, innerRadius - bigCalibrationLength - calibrationMargin - calibrationTextMargin),
                calculateYByAngleAndRadius(angle, innerRadius - bigCalibrationLength - calibrationMargin - calibrationTextMargin) - textBound.centerY(),
                calibrationTextPaint)
            angle += bigStep
        }
    }

    /**
     * 初始化大表盘的路径
     */
    private fun generateBigCalibrationPath() : Path {
        tempPath.reset()
        var angle = 0F
        while (angle < 360F) {
            tempPath.moveTo(calculateXByAngleAndRadius(angle, innerRadius - calibrationMargin),
                calculateYByAngleAndRadius(angle, innerRadius - calibrationMargin))
            tempPath.lineTo(calculateXByAngleAndRadius(angle, innerRadius - bigCalibrationLength - calibrationMargin),
                calculateYByAngleAndRadius(angle, innerRadius - bigCalibrationLength - calibrationMargin))
            angle += bigStep
        }
        return tempPath
    }

    /**
     * 初始化小表盘的路径
     */
    private fun generateSmallCalibrationPath() : Path {
        tempPath.reset()
        var angle = 0F
        while (angle < 360F) {
            tempPath.moveTo(calculateXByAngleAndRadius(angle, innerRadius - calibrationMargin),
                calculateYByAngleAndRadius(angle, innerRadius - calibrationMargin))
            tempPath.lineTo(calculateXByAngleAndRadius(angle, innerRadius - smallCalibrationLength - calibrationMargin),
                calculateYByAngleAndRadius(angle, innerRadius - smallCalibrationLength - calibrationMargin))
            angle += smallStep
        }
        return tempPath
    }

    /**
     * 重写touch event
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        var flag = false
        Log.i(TAG, "action : ${event.action} location : (${event.x}, ${event.y}) time : ${event.downTime}")
        when (event.action) {
            ACTION_DOWN -> {
                flag = checkIsInRing(event.x, event.y)
                downTime = event.downTime
                Log.i(TAG, "is in ring? $flag")
            }
            ACTION_MOVE -> {
                // 抛弃80毫秒内的移动， 这是为了点击动画做出的让步，否者将会导致动画无法显示
                if (event.eventTime - downTime > 80L) {
                    onMoveToPosition(event.x, event.y)
                }
            }
            ACTION_UP -> {
                smoothMoveToPosition(event.x, event.y)
            }
            else -> {
                Log.i(TAG, "event no register : ${event.action}")
            }
        }
        return flag
    }

    /**
     * 滑动到指定位置，带动画
     */
    private fun smoothMoveToPosition(x : Float, y : Float) {
        val angle = calculateAngleByLocation(x, y)
        Log.i(TAG, "angle : $angle")
        setCurValueWithAnim((min + (max - min) * angle / 360).toInt())
    }

    /**
     * 设置选中值，带动画
     */
    fun setCurValueWithAnim(value: Int) {
        if (!checkBounds(value)) {
            Log.e(TAG, "value : $value out of bound!")
            return
        }
        animator.setFloatValues(curValue, value.toFloat())
        animator.start()
    }

    /**
     * 检查value是否在min和max之内
     */
    private fun checkBounds(value: Int) : Boolean {
        return value in min..max
    }

    /**
     * 滑动到指定位置
     */
    private fun onMoveToPosition(x : Float, y : Float) {
        val angle = calculateAngleByLocation(x, y)
        Log.i(TAG, "angle : $angle")
        curValue = min + (max - min) * angle / 360
        listener?.onValueChanged(curValue.toInt())
        postInvalidate()
    }

    /**
     * 检查点击位置是否在圆环内
     */
    private fun checkIsInRing(x : Float, y : Float) : Boolean{
        val _x = x - outerRadius
        val _y = outerRadius - y
        val l = sqrt(_x * _x + _y * _y)
        return l > innerRadius && l < outerRadius
    }

    /**
     * 根据手指按下位置计算角度
     */
    private fun calculateAngleByLocation(x : Float, y : Float) : Float {
        var angle = (atan2(x - outerRadius, outerRadius - y) * 360 / (2 * PI)).toFloat()
        while (angle < 0) {
            angle += 360
        }
        while (angle > 360) {
            angle -= 360
        }
        return angle
    }

    /**
     * 根据角度计算X轴坐标
     */
    private fun calculateXByAngleAndRadius(angle : Float, radius : Float) : Float{
        return sin((angle * 2 * PI / 360).toFloat()) * radius + outerRadius
    }

    /**
     * 根据角度计算Y轴坐标
     */
    private fun calculateYByAngleAndRadius(angle : Float, radius : Float) : Float{
        return outerRadius - cos((angle * 2 * PI / 360).toFloat()) * radius
    }

    /**
     * 由于画笔的stroke width比较大，所以要对起始位置有个偏移角度，否者显示将会有问题
     */
    private fun calculateStartAngle() : Double {
        return 360 * atan(fgPaint.strokeWidth / 2 / (innerRadius + fgPaint.strokeWidth / 2)) / (2 * PI)
    }

    /**
     * 初始化颜色条
     */
    private fun generateGradient() : SweepGradient{
        return SweepGradient(outerRadius, outerRadius, intArrayOf(minColor, maxColor), floatArrayOf(0F, 0.95F))
    }

    interface OnValueChangeLister {
        fun onValueChanged(value: Int)
    }

}