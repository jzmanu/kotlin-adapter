package com.wuhenzhizao.adapter.extension.sticky_header

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.MotionEvent
import android.text.method.Touch.onTouchEvent




/**
 * Created by liufei on 2017/12/16.
 */
class StickyHeaderTouchHelper(
        private val recyclerView: RecyclerView,
        private val decoration: StickyRecyclerItemDecoration)
    : RecyclerView.OnItemTouchListener {

    private val detector: GestureDetector
    private var listener: OnHeaderClickListener? = null

    init {
        detector = GestureDetector(recyclerView.context, SingleTapDetector())
    }

    fun setOnHeaderClickListener(listener: OnHeaderClickListener) {
        this.listener = listener
    }

    override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val tapDetectorResponse = this.detector.onTouchEvent(e)
        if (tapDetectorResponse) {
            // Don't return false if a single tap is detected
            return true
        }
        if (e.action == MotionEvent.ACTION_DOWN) {
            val headerPos = decoration.findHeaderPositionUnder(e.x, e.y)
            val position = headerPos.first
            return position != -1
        }
        return false
    }

    private inner class SingleTapDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val headerPos = decoration.findHeaderPositionUnder(e.x, e.y)
            val position = headerPos.first
            if (position != -1) {
                val headerView = headerPos.second
                performClick(headerView!!, e)
                val headerId = (recyclerView.adapter as StickyRecyclerViewAdapter<*>).getHeaderId(position)
                listener?.apply {
                    onHeaderClick(headerView, position, headerId)
                }
                recyclerView.playSoundEffect(SoundEffectConstants.CLICK)
                headerView.onTouchEvent(e)
                return true
            }
            return false
        }

        private fun performClick(view: View, e: MotionEvent) {
            if (view is ViewGroup) {
                (0 until view.childCount)
                        .map { view.getChildAt(it) }
                        .forEach { performClick(it, e) }
            }

            containsBounds(view, e)
        }

        private fun containsBounds(view: View, e: MotionEvent): View? {
            val x = e.x.toInt()
            val y = e.y.toInt()
            val rect = Rect()
            view.getHitRect(rect)
            if (view.visibility == View.VISIBLE
                    && view.dispatchTouchEvent(e)
                    && rect.left < rect.right
                    && rect.top < rect.bottom
                    && x >= rect.left
                    && x < rect.right
                    && y >= rect.top) {
                view.performClick()
                return view
            }
            return null
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }
}