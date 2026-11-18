package com.fj.launcher.glmatrix

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GLMatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    val renderer: GLMatrixRenderer = GLMatrixRenderer(context)

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        preserveEGLContextOnPause = true
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun pauseRendering() {
        renderer.state.isPaused.set(true)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun resumeRendering() {
        renderer.state.isPaused.set(false)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun onActivityPause() {
        pauseRendering()
        onPause()
    }

    fun onActivityResume() {
        onResume()
        resumeRendering()
    }
}
