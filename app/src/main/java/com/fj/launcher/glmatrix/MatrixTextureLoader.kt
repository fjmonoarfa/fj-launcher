package com.fj.launcher.glmatrix

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log

class MatrixTextureLoader(private val context: Context) {
    var textureId: Int = 0
        private set

    val charCols = 16
    val charRows = 13
    var texCharWidth: Float = 0f
        private set
    var texCharHeight: Float = 0f
        private set

    // Authentic encoding from xscreensaver glmatrix.c:
    // Digits (row 1: 16-25) and authentic Japanese Katakana glyphs (row 12: 192-207)
    // Katakana glyphs are repeated to give authentic Matrix digital rain density
    val matrixEncoding = intArrayOf(
        // Digits 0-9
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
        // Katakana glyphs from row 12 of matrix3.png
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207
    )

    fun load(): Boolean {
        try {
            val inputStream = context.assets.open("matrix3.png")
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (original == null) {
                Log.e("MatrixTextureLoader", "Failed to decode matrix3.png from assets")
                return false
            }

            val origW = original.width  // 512
            val origH = original.height // 598
            val texW = 512
            val texH = 1024

            val cw = origW / charCols // 32
            val ch = origH / charRows // 46

            texCharWidth = cw.toFloat() / texW.toFloat()
            texCharHeight = ch.toFloat() / texH.toFloat()

            // Prepare pixels array
            val pixels = IntArray(origW * origH)
            original.getPixels(pixels, 0, origW, 0, 0, origW, origH)
            original.recycle()

            // Horizontally flip each character cell in place, exactly matching glmatrix.c (flip_p = 1)
            for (y in 0 until origH) {
                val rowOffset = y * origW
                for (col in 0 until charCols) {
                    val colStart = rowOffset + col * cw
                    for (x in 0 until cw / 2) {
                        val leftIdx = colStart + x
                        val rightIdx = colStart + cw - 1 - x
                        val tmp = pixels[leftIdx]
                        pixels[leftIdx] = pixels[rightIdx]
                        pixels[rightIdx] = tmp
                    }
                }
            }

            val processedPixels = IntArray(texW * texH)

            // Convert green channel brightness to alpha mask with clean black background
            for (y in 0 until origH) {
                val rowOffset = y * texW
                val origRowOffset = y * origW
                for (x in 0 until origW) {
                    val p = pixels[origRowOffset + x]
                    val g = (p shr 8) and 0xFF
                    val r = (p shr 16) and 0xFF
                    val b = p and 0xFF
                    val intensity = Math.max(g, Math.max(r, b))
                    // Threshold faint background noise (values < 25) to pure transparent black
                    val alpha = if (intensity > 25) intensity else 0
                    processedPixels[rowOffset + x] = (alpha shl 24) or 0x00FFFFFF
                }
            }

            val texBitmap = Bitmap.createBitmap(processedPixels, texW, texH, Bitmap.Config.ARGB_8888)

            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texBitmap, 0)
            texBitmap.recycle()

            return true
        } catch (e: Exception) {
            Log.e("MatrixTextureLoader", "Error loading matrix3.png", e)
            return false
        }
    }

    /**
     * UV lookup matching xscreensaver glmatrix.c:
     * cell = abs(glyphIndex) - 1
     * col = cell % 16, row = cell / 16.
     * Returns [u0, v0, u1, v1] where (u0,v0) is top-left and (u1,v1) is bottom-right.
     */
    fun getUVs(glyphIndex: Int): FloatArray {
        val g = Math.abs(glyphIndex).coerceAtLeast(1)
        val cell = g - 1
        val col = cell % charCols
        val row = cell / charCols

        val u0 = col * texCharWidth
        val u1 = (col + 1) * texCharWidth
        val v0 = row * texCharHeight
        val v1 = (row + 1) * texCharHeight

        return floatArrayOf(u0, v0, u1, v1)
    }
}
