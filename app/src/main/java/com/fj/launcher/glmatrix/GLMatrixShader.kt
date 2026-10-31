package com.fj.launcher.glmatrix

import android.opengl.GLES20
import android.util.Log

object GLMatrixShader {
    private const val TAG = "GLMatrixShader"

    const val VERTEX_SHADER_CODE = """
        uniform mat4 u_MVPMatrix;
        attribute vec4 a_Position;
        attribute vec2 a_TexCoordinate;
        attribute vec4 a_Color;
        
        varying vec2 v_TexCoordinate;
        varying vec4 v_Color;
        
        void main() {
            gl_Position = u_MVPMatrix * a_Position;
            v_TexCoordinate = a_TexCoordinate;
            v_Color = a_Color;
        }
    """

    const val FRAGMENT_SHADER_CODE = """
        precision mediump float;
        uniform sampler2D u_Texture;
        varying vec2 v_TexCoordinate;
        varying vec4 v_Color;
        
        void main() {
            vec4 texColor = texture2D(u_Texture, v_TexCoordinate);
            // Character text is white with alpha channel
            gl_FragColor = vec4(v_Color.rgb * texColor.rgb, texColor.a * v_Color.a);
        }
    """

    fun createProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error linking program: " + GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
}
