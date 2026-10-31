package com.fj.launcher.glmatrix

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.GLES20
import android.opengl.GLUtils

class MatrixGlyphAtlas {
    var textureId: Int = 0
        private set

    val glyphs = buildGlyphList()
    val glyphCount: Int = glyphs.size
    private val cols = 10
    private val rows = (glyphCount + cols - 1) / cols

    private val atlasWidth = 512
    private val atlasHeight = 512

    fun init() {
        val bitmap = Bitmap.createBitmap(atlasWidth, atlasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val cellW = atlasWidth.toFloat() / cols
        val cellH = atlasHeight.toFloat() / rows

        for (i in glyphs.indices) {
            val col = i % cols
            val row = i / cols
            val x = col * cellW + cellW / 2f
            val y = row * cellH + cellH * 0.78f
            canvas.drawText(glyphs[i].toString(), x, y, paint)
        }

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    fun getUVs(glyphIndex: Int): FloatArray {
        val idx = Math.abs(glyphIndex) % glyphCount
        val col = idx % cols
        val row = idx / cols

        val u0 = col.toFloat() / cols
        val v0 = row.toFloat() / rows
        val u1 = (col + 1).toFloat() / cols
        val v1 = (row + 1).toFloat() / rows

        return floatArrayOf(u0, v0, u1, v1)
    }

    private fun buildGlyphList(): List<Char> {
        val list = mutableListOf<Char>()
        // Authentic Half-width Katakana characters from xscreensaver glmatrix.c (0xFF66 to 0xFF9D)
        for (code in 0xFF66..0xFF9D) {
            list.add(code.toChar())
        }
        // Digits
        for (c in '0'..'9') {
            list.add(c)
        }
        // Matrix symbols
        val symbols = listOf('Z', 'X', 'Y', 'M', 'K', ':', '*', '+', '-', '=', '<', '>', '#', '%')
        list.addAll(symbols)
        return list
    }
}
