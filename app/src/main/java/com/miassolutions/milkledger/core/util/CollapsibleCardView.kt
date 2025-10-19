package com.miassolutions.milkledger.core.util

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.miassolutions.milkledger.databinding.ViewCollapsibleCardBinding

class CollapsibleCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewCollapsibleCardBinding =
        ViewCollapsibleCardBinding.inflate(LayoutInflater.from(context), this)

    var isExpanded = true
        private set

    init {
        radius = 12f
        cardElevation = 4f
        useCompatPadding = true

        binding.headerLayout.setOnClickListener {
            toggleCard()
        }
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setContent(view: View) {
        binding.contentContainer.removeAllViews()
        binding.contentContainer.addView(view)
    }

    fun toggleCard() {
        if (isExpanded) collapse() else expand()
    }

    fun expand() {
        val content = binding.contentContainer
        content.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.UNSPECIFIED
        )

        val targetHeight = content.measuredHeight

        content.layoutParams.height = 0
        content.isVisible = true

        val animator = ValueAnimator.ofInt(0, targetHeight).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                content.layoutParams.height = value
                content.requestLayout()
            }
        }

        fadeIn(content)
        animator.start()

        // Rotate arrow up
        binding.ivArrow.animate()
            .rotation(270f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        isExpanded = true
    }

    fun collapse() {
        val content = binding.contentContainer
        val initialHeight = content.height

        val animator = ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                content.layoutParams.height = value
                content.requestLayout()
                if (value == 0) content.isVisible = false
            }
        }

        fadeOut(content)
        animator.start()

        // Rotate arrow down
        binding.ivArrow.animate()
            .rotation(90f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        isExpanded = false
    }

    private fun fadeIn(view: View) {
        val fade = AlphaAnimation(0f, 1f).apply {
            duration = 200
            fillAfter = true
        }
        view.startAnimation(fade)
    }

    private fun fadeOut(view: View) {
        val fade = AlphaAnimation(1f, 0f).apply {
            duration = 150
            fillAfter = true
        }
        view.startAnimation(fade)
    }
}
