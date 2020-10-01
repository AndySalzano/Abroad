package web.abroad.prototype

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter

class ImageSliderAdapter(val context : Context, images : ArrayList<Bitmap>) : PagerAdapter() {

    private var mImages : ArrayList<Bitmap> = images

    override fun getCount(): Int {
        return mImages.size
    }

    override fun isViewFromObject(view : View, any : Any) : Boolean {
        return view == any
    }

    override fun instantiateItem(container : ViewGroup, position: Int) : Any {
        val imageView = ImageView(context)
        imageView.scaleType = (ImageView.ScaleType.CENTER_CROP)
        imageView.setImageBitmap(mImages[position])
        container.addView(imageView, 0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any){
        container.removeView(any as ImageView)
    }
}