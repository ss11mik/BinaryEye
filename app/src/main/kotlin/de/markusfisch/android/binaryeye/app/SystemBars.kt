package de.markusfisch.android.binaryeye.app

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AbsListView
import de.markusfisch.android.binaryeye.R

val windowInsets = Rect()
val systemBarScrollListener = object : AbsListView.OnScrollListener {
	override fun onScroll(
		view: AbsListView,
		firstVisibleItem: Int,
		visibleItemCount: Int,
		totalItemCount: Int
	) {
		// give Android some time to settle down before running this;
		// not putting it on the queue makes it only work sometimes
		view.post {
			val scrolled = firstVisibleItem > 0 ||
					(totalItemCount > 0 && view.getChildAt(0).top < 0)
			val scrollable = if (scrolled) true else totalItemCount > 0 &&
					view.getChildAt(view.lastVisiblePosition).bottom > view.height
			setSystemAndToolBarTransparency(view.context, scrolled, scrollable)
		}
	}

	override fun onScrollStateChanged(
		view: AbsListView,
		scrollState: Int
	) {
	}
}

fun setSystemAndToolBarTransparency(
	context: Context,
	scrolled: Boolean = false,
	scrollable: Boolean = false
) {
	val opaqueColor = ContextCompat.getColor(context, R.color.primary)
	val transparentColor = 0x00000000
	val topColor = if (scrolled) opaqueColor else transparentColor
	val bottomColor = if (scrolled || scrollable) opaqueColor else transparentColor
	val activity = getAppCompatActivity(context) ?: return
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		val window = activity.window
		window.statusBarColor = topColor
		window.navigationBarColor = bottomColor
	}
	activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(topColor))
}

private fun getAppCompatActivity(context: Context): AppCompatActivity? {
	var ctx = context
	while (ctx is ContextWrapper) {
		if (ctx is AppCompatActivity) {
			return ctx
		}
		ctx = ctx.baseContext ?: break
	}
	return null
}

fun initSystemBars(activity: AppCompatActivity) {
	activity.findViewById(R.id.main_layout)?.also { view ->
		setWindowInsetListener(view)
	}
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		activity.window.decorView.systemUiVisibility =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
					View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	}
	setSystemAndToolBarTransparency(activity)
}

private fun setWindowInsetListener(view: View) {
	ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
		if (insets.hasSystemWindowInsets()) {
			val left = insets.systemWindowInsetLeft
			val top = insets.systemWindowInsetTop
			val right = insets.systemWindowInsetRight
			val bottom = insets.systemWindowInsetBottom
			view.setPadding(left, top, right, bottom)
			windowInsets.set(left, top, right, bottom)
		}
		insets.consumeSystemWindowInsets()
	}
}
