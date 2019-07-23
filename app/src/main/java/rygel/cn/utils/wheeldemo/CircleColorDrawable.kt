package rygel.cn.utils.wheeldemo

import android.graphics.*
import android.graphics.drawable.Drawable

class CircleColorDrawable(var color: Int = Color.GRAY) : Drawable() {

    private val paint = Paint()

    override fun draw(canvas: Canvas) {
        //canvas.drawCircle()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}