package com.tomoe.wordscanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ScanOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    val scanRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val frameWidth = width * 0.82f
        val frameHeight = min(height * 0.28f, frameWidth * 0.58f)
        val left = (width - frameWidth) / 2f
        val top = height * 0.29f
        scanRect.set(left, top, left + frameWidth, top + frameHeight)

        canvas.drawRect(0f, 0f, width.toFloat(), scanRect.top, shadePaint)
        canvas.drawRect(0f, scanRect.bottom, width.toFloat(), height.toFloat(), shadePaint)
        canvas.drawRect(0f, scanRect.top, scanRect.left, scanRect.bottom, shadePaint)
        canvas.drawRect(scanRect.right, scanRect.top, width.toFloat(), scanRect.bottom, shadePaint)
        canvas.drawRoundRect(scanRect, 28f, 28f, framePaint)
    }
}
