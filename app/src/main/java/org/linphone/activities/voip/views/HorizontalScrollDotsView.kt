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
import org.linphone.utils.AppUtils

class HorizontalScrollDotsView : View {
    private val dotAmount = 3

    private var dotRadius: Float = 5f
    private var margin: Float = 2f

    private lateinit var paint: Paint

    constructor(context: Context) : super(context) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    fun init(context: Context) {
        dotRadius = AppUtils.dpToPixels(context, 5f)
        margin = AppUtils.dpToPixels(context, 5f)
        paint = Paint()
        paint.color = Color.parseColor("#4B5964")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until dotAmount) {
            canvas.drawCircle((i + 1) * margin + (i * 2 + 1) * dotRadius, dotRadius, dotRadius, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = ((dotRadius * 2 + margin) * dotAmount).toInt()
        val height: Int = dotRadius.toInt() * 2

        setMeasuredDimension(width, height)
    }
}
