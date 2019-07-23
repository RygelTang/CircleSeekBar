package rygel.cn.utils.wheeldemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

/**
 * Created by Peach on 2017/11/13.
 * version : 1.0.0
 */

/**
 * 这个控件是用来设定治疗时间和治疗压力的。
 * 需求说明
 *  1、仿写Forest的时间设定控件
 */
class SettingWheel : View {

    /**
     * 变量默认值区域
     */
    private val defaultHeight = 300
    private val defaultWidth = 300
    private val defaultRingWidth = 20f
    private val defaultButtonRadius = 20f
    private val defaultMaxProgress = 100
    private val defaultStartProgress = 0
    private val defaultColor = Color.GRAY
    private val defaultTextSize = 40f
    /**
     * 私有设定区域
     */
    private val ringBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var centerX = 0f
    private var centerY = 0f
    private var buttonCenterX = 0f
    private var buttonCenterY = 0f
    private var centerRadius = 0f
    //当这个变量为假的时候，我们不能为SettingWheel设定数值
    private var isSetProgressBegin = false
    private var currentDirection = Direction.UNKNOWN
    private var userMessage = "0"
    /**
     * 开放的设定区域
     */
    /**
     * 圆环宽度
     */
    var ringWidth = 20f
    /**
     * 按钮半径
     */
    var buttonRadius = 25f
    /**
     * 字体大小
     */
    var textSize = defaultTextSize
    /**
     * 圆环背景颜色
     */
    var ringBackgroundColor = Color.GRAY
    /**
     * 文字颜色
     */
    var textColor = Color.GRAY
    /**
     * 控件名称
     */
    var name = ""
    /**
     * 进度值列表，为了不同进度有不同颜色而设
     */
    var progressValueList = ArrayList<Int>(10)
    /**
     * 颜色进度列表
     */
    var progressColorList = ArrayList<Int>(10)
    //进度相关
    /**
     * 最大进度
     */
    var maxProgress = 100
    /**
     * 当前进度
     */
    var currentProgress = 0
    /**
     * 进度选择回调
     */
    var progressSetListener: OnProgressSetListener? = null

    var onSettingTouched : OnSettingTouched? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttributeSet: Int) : super(context, attributeSet, defStyleAttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.SettingWheel)
        ringWidth = typeArray.getDimension(R.styleable.SettingWheel_ringWidth, defaultRingWidth)
        buttonRadius = typeArray.getDimension(R.styleable.SettingWheel_buttonRadius, defaultButtonRadius)
        maxProgress = typeArray.getInt(R.styleable.SettingWheel_maxProgress, defaultMaxProgress)
        currentProgress = typeArray.getInt(R.styleable.SettingWheel_currentProgress, defaultStartProgress)
        ringBackgroundColor = typeArray.getColor(R.styleable.SettingWheel_ringBackground, defaultColor)
        textColor = typeArray.getColor(R.styleable.SettingWheel_textColor, defaultColor)
        textSize = typeArray.getDimension(R.styleable.SettingWheel_textSize, defaultTextSize)
        name = typeArray.getString(R.styleable.SettingWheel_name)
        typeArray.recycle()

        this.setOnTouchListener(OnTouch())
    }

    //当布局中的宽或者高属性设置的是wrap_content的时候，我们返回默认宽或者高
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, defaultHeight)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, defaultHeight)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val realWidth = width - paddingLeft - paddingRight
        val realHeight = height - paddingBottom - paddingTop
        val minOfWidthAndHeight = Math.min(realWidth, realHeight)
        centerRadius = minOfWidthAndHeight / 2 - buttonRadius
        centerX = realWidth / 2f + paddingLeft
        centerY = realHeight / 2f + paddingTop
        drawRingBackground(canvas)
        drawProgress(canvas)
    }

    /**
     * 绘制圆环背景
     */
    private fun drawRingBackground(canvas: Canvas?) {
        ringBackgroundPaint.color = ringBackgroundColor
        ringBackgroundPaint.strokeWidth = ringWidth
        ringBackgroundPaint.style = Paint.Style.STROKE
        canvas?.drawCircle(centerX, centerY, centerRadius, ringBackgroundPaint)
    }

    /**
     * 绘制进度：1、进度圆环  2、可控制的按钮  3、当前进度的数字显示 （4、进度对应的图片切换）
     */
    private fun drawProgress(canvas: Canvas?) {
        drawProgressRingAndButton(canvas)
        drawProgressText(canvas)
    }

    private fun drawProgressRingAndButton(canvas: Canvas?) {
        if (progressColorList.size != progressValueList.size) {
            throw RuntimeException("progressColor should correspond to progressValue")
        }
        progressPaint.color = getCorrespondColor()
        progressPaint.strokeWidth = ringWidth
        progressPaint.style = Paint.Style.STROKE
        val arc = currentProgress * 360f / maxProgress
        val rectF = RectF(paddingLeft + buttonRadius, paddingTop + buttonRadius, width - paddingLeft - buttonRadius, height - paddingTop - buttonRadius)
        canvas?.drawArc(rectF, -90f, arc, false, progressPaint)

        progressPaint.style = Paint.Style.FILL
        val radian = Math.toRadians(arc.toDouble())
        buttonCenterX = (centerRadius * Math.sin(radian)).toFloat() + centerX
        buttonCenterY = (-centerRadius * Math.cos(radian)).toFloat() + centerY
        canvas?.drawCircle(buttonCenterX, buttonCenterY, buttonRadius, progressPaint)
    }

    private fun drawProgressText(canvas: Canvas?) {
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
        drawTextHelper(canvas,name,true,textPaint)
        if (!TextUtils.isEmpty(userMessage))
            drawTextHelper(canvas,userMessage,false,textPaint)
    }

    /**
     * used to paint text
     */
    private fun drawTextHelper(canvas: Canvas?, text:String, isAbove: Boolean = true,mPaint: Paint) {
        if (TextUtils.isEmpty(text)) return
        val textHeight = mPaint.textSize
        if (isAbove)
            canvas?.drawText(text, centerX, centerY - textHeight / 2, mPaint)
        else
            canvas?.drawText(text, centerX, centerY + textHeight, mPaint)
    }

    private fun getCorrespondColor(): Int {
        var paintColor = Color.RED
        val valueArray = progressValueList.toIntArray()
        Arrays.sort(valueArray)
        var index = 0
        for (i in valueArray) {
            if (i >= currentProgress) {
                paintColor = progressColorList[index]
                break
            }
            index++
        }
        return paintColor
    }

    /**
     * 计算手指落点的角度
     */
    private fun computeFingerArc(x: Float, y: Float): Double {
        var tempArc: Double
        if (x == centerX && y - centerY < 0) {
            tempArc = 0.0
        } else if (x == centerX && y - centerY > 0) {
            tempArc = 180.0
        } else if (x - centerX > 0 && y == centerY) {
            tempArc = 90.0
        } else if (x - centerX < 0 && y == centerY) {
            tempArc = 270.0
        } else if (x == centerX && y == centerY) {
            return 0.0
        } else {
            val tan = (x - centerX).toDouble() / (y - centerY)
            tempArc = Math.atan(tan) / Math.PI * 180
            //判断当前是在第几象限
            if (x - centerX > 0 && y - centerY > 0) {  //第四象限
                tempArc = 180 - tempArc
            } else if (x - centerX > 0 && y - centerY < 0) {  //第一象限
                tempArc = -tempArc
            } else if (x - centerX < 0 && y - centerY > 0) { //第三象限
                tempArc = -tempArc
                tempArc += 180
            } else {  //第一象限
                tempArc = -tempArc
            }
        }
        if (tempArc < 0) {
            tempArc += 360
        }
        return tempArc
    }

    /**
     *  如果有点击的话，在onTouch里面要做合适的调整
     */
    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun moveTo(progress: Int) {
        currentProgress = when {
            progress < 0 -> 0
            progress > maxProgress -> maxProgress
            else -> progress
        }
        invalidate()
    }

    fun setColorStep(value: List<Int>, color: List<Int>) {
        progressValueList = value as ArrayList<Int>
        progressColorList = color as ArrayList<Int>
        invalidate()
    }

    fun setUserMessage(message:String){
        userMessage = message
        invalidate()
    }

    private inner class OnTouch : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event != null) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val consume = onSettingTouched?.onSettingTouched(v,event) ?: false //最好不要放在外面，不然调用次数太多
                        if (consume) return false
                        currentDirection = Direction.UNKNOWN
                        val rectF = RectF(buttonCenterX - buttonRadius, buttonCenterY - buttonRadius, buttonCenterX + buttonRadius, buttonCenterY + buttonRadius)
                        if (rectF.contains(event.x, event.y)) {
                            isSetProgressBegin = true
                            moveToFinger(event.x, event.y)
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isSetProgressBegin) {
                            moveToFinger(event.x, event.y)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v?.performClick()
                        if (isSetProgressBegin) {
                            isSetProgressBegin = false
                            progressSetListener?.onProgressSet(this@SettingWheel,currentProgress)
                        }
                    }
                }
            }
            return false
        }

        private fun moveToFinger(x: Float, y: Float) {
            val arc = computeFingerArc(x, y).toInt()
            val newProgress: Int
            newProgress = if (currentDirection == Direction.INCREASE && arc == 0) {
                maxProgress
            } else {
                arc * maxProgress / 360
            }
            /**
             * 增加只可以增加到最大值，减少只可以减少到最小值
             */
            if (maxProgress / 2 in (newProgress + 1)..(currentProgress - 1) && Math.abs((currentProgress - newProgress)) >= maxProgress/2) {
                currentProgress = maxProgress
                this@SettingWheel.invalidate()
                return
            }

            if (maxProgress / 2 in (currentProgress + 1)..(newProgress - 1) && Math.abs((currentProgress - newProgress)) >= maxProgress/2) {
                currentProgress = 0
                this@SettingWheel.invalidate()
                return
            }

            currentDirection = when {
                newProgress > currentProgress -> Direction.INCREASE
                newProgress < currentProgress -> Direction.DECREASE
                else -> Direction.UNKNOWN
            }
            currentProgress = newProgress
            this@SettingWheel.invalidate()
        }

    }

    private enum class Direction {
        INCREASE, DECREASE, UNKNOWN
    }

    interface OnProgressSetListener {
        fun onProgressSet(which: SettingWheel,progress: Int)
    }

    interface OnSettingTouched{
        /**
         *@return 如果为真的话，那么这个点击事件就被消耗了，换句话说就是我们无法设置数值了
         */
        fun onSettingTouched(v:View?,event:MotionEvent?):Boolean
    }

}