/*
 * Copyright (c) 2010-2021 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.voip.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.linphone.R
import org.linphone.utils.AppUtils

class HorizontalScrollDotsView : View {
    private var dotAmount = 2
    private var selectedDot = 0

    private var dotRadius: Float = 5f
    private var margin: Float = 2f

    private lateinit var dotPaint: Paint
    private lateinit var selectedDotPaint: Paint

    constructor(context: Context) : super(context) { init(context) }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init(context) }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HorizontalScrollDot,
            0, 0
        ).apply {
            try {
                dotRadius = getDimension(R.styleable.HorizontalScrollDot_dotRadius, 5f)

                dotAmount = getInt(R.styleable.HorizontalScrollDot_dotCount, 1)

                val color = getColor(R.styleable.HorizontalScrollDot_dotColor, context.resources.getColor(R.color.voip_gray_background))
                dotPaint.color = color
                val selectedColor = getColor(R.styleable.HorizontalScrollDot_selectedDotColor, context.resources.getColor(R.color.voip_dark_gray))
                selectedDotPaint.color = selectedColor

                selectedDot = getInt(R.styleable.HorizontalScrollDot_selectedDot, 1)

                invalidate()
            } finally {
                recycle()
            }
        }
    }

    fun init(context: Context) {
        dotRadius = AppUtils.dpToPixels(context, 5f)
        margin = AppUtils.dpToPixels(context, 5f)
        dotPaint = Paint()
        dotPaint.color = Color.parseColor("#D8D8D8")
        selectedDotPaint = Paint()
        selectedDotPaint.color = Color.parseColor("#4B5964")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until dotAmount) {
            if (i == selectedDot) {
                canvas.drawCircle(
                    (i + 1) * margin + (i * 2 + 1) * dotRadius,
                    dotRadius,
                    dotRadius,
                    selectedDotPaint
                )
            } else {
                canvas.drawCircle(
                    (i + 1) * margin + (i * 2 + 1) * dotRadius,
                    dotRadius,
                    dotRadius,
                    dotPaint
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = ((dotRadius * 2 + margin) * dotAmount + margin).toInt()
        val height: Int = dotRadius.toInt() * 2

        setMeasuredDimension(width, height)
    }

    fun setDotCount(count: Int) {
        dotAmount = count
        invalidate()
    }

    fun setSelectedDot(index: Int) {
        selectedDot = index
        invalidate()
    }
}
