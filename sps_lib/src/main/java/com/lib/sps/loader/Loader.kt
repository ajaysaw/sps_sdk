import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.lib.sps.R

class Loader(private val activity: Activity) {

    private var loaderView: View? = null

    init {
        initializeLoader()
    }

    private fun initializeLoader() {
        // Inflate the loader layout
        loaderView = LayoutInflater.from(activity).inflate(R.layout.loader_layout, null)

        // Add the loader to the root of the activity's content
        val rootLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        rootLayout.addView(loaderView)

        // Set up touch blocking
        loaderView?.setOnTouchListener { _, event ->
            // Block all touch events if loader is visible
            return@setOnTouchListener true
        }
    }

    // Show the loader with animation
    fun show() {
        loaderView?.visibility = View.VISIBLE
        val lottieAnimationView: LottieAnimationView = loaderView?.findViewById(R.id.lottie_loader)!!
        lottieAnimationView.playAnimation()
    }

    // Hide the loader
    fun hide() {
        loaderView?.visibility = View.GONE
        val lottieAnimationView: LottieAnimationView = loaderView?.findViewById(R.id.lottie_loader)!!
        lottieAnimationView.cancelAnimation()
    }
}
