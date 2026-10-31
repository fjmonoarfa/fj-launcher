package com.fj.launcher.glmatrix

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.Random
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin

/**
 * Port of Jamie Zawinski's classic xscreensaver "glmatrix.c".
 * Uses the authentic matrix3.png glyph texture atlas and the exact
 * 3D arena, brightness waves, spinner droppers, glyph mutations,
 * depth fog, and auto-tracking camera angles from the original C source.
 */
class GLMatrixRenderer(
    private val context: Context,
    val state: GLMatrixState = GLMatrixState()
) : GLSurfaceView.Renderer {

    private val textureLoader = MatrixTextureLoader(context)
    private var programId = 0

    // Shader handles
    private var uMVPMatrixHandle = 0
    private var uTextureHandle = 0
    private var aPositionHandle = 0
    private var aTexCoordinateHandle = 0
    private var aColorHandle = 0

    // Matrices
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Constants from xscreensaver glmatrix.c
    private val GRID_SIZE = 70
    private val GRID_DEPTH = 35f
    private val WAVE_SIZE = 22
    private val SPLASH_RATIO = 0.7f

    // Precomputed brightness ramp from glmatrix.c
    private val brightnessRamp = FloatArray(WAVE_SIZE)

    // Camera view angles from nice_views[] in glmatrix.c
    private val niceViews = arrayOf(
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, -20f),
        floatArrayOf(0f, 20f),
        floatArrayOf(25f, 0f),
        floatArrayOf(-25f, 0f),
        floatArrayOf(25f, 20f),
        floatArrayOf(-25f, 20f),
        floatArrayOf(25f, -20f),
        floatArrayOf(-25f, -20f),
        floatArrayOf(10f, 0f),
        floatArrayOf(-10f, 0f),
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, 0f)
    )

    private var lastView = 0
    private var targetView = 0
    private var viewX = 0f
    private var viewY = 0f
    private var viewSteps = 100
    private var viewTick = 0
    private var autoTracking = false
    private var trackTick = 0

    private val random = Random()
    private val strips = mutableListOf<MatrixStrip>()

    // Vertex batching buffer
    private val floatsPerVertex = 9
    private val maxQuads = 1800
    private val maxVertices = maxQuads * 4
    private val maxIndices = maxQuads * 6

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer
    private val vertexArray = FloatArray(maxVertices * floatsPerVertex)

    private var aspectRatio = 1.0f
    private var lastTimeMs = SystemClock.uptimeMillis()

    init {
        // Compute the brightness ramp exactly as in glmatrix.c
        for (i in 0 until WAVE_SIZE) {
            var j = (WAVE_SIZE - i).toFloat() / (WAVE_SIZE - 1).toFloat()
            j *= (Math.PI.toFloat() / 2f)
            j = sin(j)
            j = 0.2f + (j * 0.8f)
            brightnessRamp[i] = j
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        // Authentic glBlendFunc(GL_SRC_ALPHA, GL_ONE) as specified by Jeff Epler in glmatrix.c
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        textureLoader.load()
        programId = GLMatrixShader.createProgram()

        uMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uTextureHandle = GLES20.glGetUniformLocation(programId, "u_Texture")
        aPositionHandle = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoordinateHandle = GLES20.glGetAttribLocation(programId, "a_TexCoordinate")
        aColorHandle = GLES20.glGetAttribLocation(programId, "a_Color")

        initBuffers()
        initStrips()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        aspectRatio = width.toFloat() / height.toFloat()
        GLES20.glViewport(0, 0, width, height)

        // gluPerspective (80.0, 1/h, 1.0, 100) from glmatrix.c reshape_matrix
        Matrix.perspectiveM(projMatrix, 0, 78f, aspectRatio, 1f, 100f)
        initStrips()
    }

    private fun initBuffers() {
        val bb = ByteBuffer.allocateDirect(maxVertices * floatsPerVertex * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()

        val ib = ByteBuffer.allocateDirect(maxIndices * 2)
        ib.order(ByteOrder.nativeOrder())
        indexBuffer = ib.asShortBuffer()

        val indices = ShortArray(maxIndices)
        for (i in 0 until maxQuads) {
            val vBase = (i * 4).toShort()
            val iBase = i * 6
            indices[iBase + 0] = vBase
            indices[iBase + 1] = (vBase + 1).toShort()
            indices[iBase + 2] = (vBase + 2).toShort()
            indices[iBase + 3] = (vBase + 2).toShort()
            indices[iBase + 4] = (vBase + 3).toShort()
            indices[iBase + 5] = vBase
        }
        indexBuffer.put(indices)
        indexBuffer.position(0)
    }

    private fun bellrand(n: Float): Float {
        return (random.nextFloat() * n + random.nextFloat() * n + random.nextFloat() * n) / 3f
    }

    private fun randomGlyph(): Int {
        val encoding = textureLoader.matrixEncoding
        return encoding[random.nextInt(encoding.size)] + 1
    }

    private fun resetStrip(s: MatrixStrip) {
        val spanX = (GRID_SIZE * aspectRatio * 0.6f)
        s.x = (random.nextFloat() * spanX) - (spanX / 2f)
        s.y = (GRID_SIZE / 2f) + bellrand(0.5f)
        s.z = (GRID_DEPTH * 0.2f) - (random.nextFloat() * GRID_DEPTH * 0.7f)
        s.spinnerY = 0f
        s.erasing = false

        val spd = state.speedMultiplier
        s.dz = bellrand(0.02f) * spd
        s.spinnerSpeed = (0.15f + bellrand(0.25f)) * spd

        s.spinSpeed = (bellrand(2f / spd) + 1).toInt().coerceAtLeast(1)
        s.spinTick = 0

        s.wavePosition = 0
        s.waveSpeed = (bellrand(3f / spd) + 1).toInt().coerceAtLeast(1)
        s.waveTick = 0

        s.spinnerGlyph = randomGlyph()

        for (i in 0 until GRID_SIZE) {
            val drawP = (random.nextInt(7) != 0) // ~85% chance of character presence
            val spinP = drawP && (random.nextInt(15) == 0)
            var g = if (drawP) randomGlyph() else 0
            if (spinP) g = -g
            s.glyphs[i] = g
        }
    }

    private fun initStrips() {
        strips.clear()
        val numStrips = (45 * state.density).toInt().coerceIn(25, 75)

        for (i in 0 until numStrips) {
            val s = MatrixStrip(GRID_SIZE)
            resetStrip(s)
            // Stagger initial strips randomly across screen so animation starts full
            s.spinnerY = random.nextFloat() * GRID_SIZE
            if (random.nextBoolean()) {
                s.erasing = true
                s.spinnerSpeed /= 2f
            }
            strips.add(s)
        }
    }

    private fun tickAutoTrack() {
        if (!state.cameraSwayEnabled) {
            viewX = 0f
            viewY = 0f
            return
        }

        val spd = state.speedMultiplier
        if (!autoTracking) {
            trackTick++
            if (trackTick < (20 / spd)) return
            trackTick = 0
            if (random.nextInt(20) == 0) {
                autoTracking = true
            } else {
                return
            }
        }

        val ox = niceViews[lastView][0]
        val oy = niceViews[lastView][1]
        val tx = niceViews[targetView][0]
        val ty = niceViews[targetView][1]

        val th = sin((Math.PI / 2.0) * (viewTick.toDouble() / viewSteps.toDouble())).toFloat()
        viewX = ox + (tx - ox) * th
        viewY = oy + (ty - oy) * th
        viewTick++

        if (viewTick >= viewSteps) {
            viewTick = 0
            viewSteps = (350f / spd).toInt().coerceAtLeast(60)
            lastView = targetView
            targetView = random.nextInt(niceViews.size - 1) + 1
            autoTracking = false
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = SystemClock.uptimeMillis()
        val deltaSec = ((now - lastTimeMs) / 1000f).coerceIn(0.001f, 0.1f)
        lastTimeMs = now

        if (state.isPaused.get()) {
            return
        }

        tickAutoTrack()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set camera: gluLookAt(0.0, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0) from glmatrix.c
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 25.0f, 0f, 0f, 0f, 0f, 1.0f, 0f)

        // Apply view rotation from auto-track
        Matrix.setIdentityM(rotMatrix, 0)
        Matrix.rotateM(rotMatrix, 0, viewX * 0.45f, 1f, 0f, 0f)
        Matrix.rotateM(rotMatrix, 0, viewY * 0.45f, 0f, 1f, 0f)

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, rotMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, tempMatrix, 0)

        GLES20.glUseProgram(programId)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureLoader.textureId)
        GLES20.glUniform1i(uTextureHandle, 0)

        var quadCount = 0
        var vIndex = 0

        val dimmer = (1.0f - state.dimmerAlpha).coerceIn(0.1f, 1.0f)
        val quadSize = 1.0f // S = 1 in glmatrix.c

        // Update and render strips sorted from back to front (as in glmatrix.c)
        strips.sortBy { it.z }

        for (s in strips) {
            // tick_strip logic
            s.z += s.dz
            if (s.z > GRID_DEPTH * SPLASH_RATIO) {
                resetStrip(s)
                continue
            }

            s.spinnerY += s.spinnerSpeed
            if (s.spinnerY >= GRID_SIZE) {
                if (s.erasing) {
                    resetStrip(s)
                    continue
                } else {
                    s.erasing = true
                    s.spinnerY = 0f
                    s.spinnerSpeed /= 2f
                }
            }

            // Spin spinners
            s.spinTick++
            if (s.spinTick > s.spinSpeed) {
                s.spinTick = 0
                s.spinnerGlyph = randomGlyph()
                for (i in 0 until GRID_SIZE) {
                    if (s.glyphs[i] < 0) {
                        s.glyphs[i] = -randomGlyph()
                        if (random.nextInt(600) == 0) s.glyphs[i] = -s.glyphs[i]
                    }
                }
            }

            // Move color wave
            s.waveTick++
            if (s.waveTick > s.waveSpeed) {
                s.waveTick = 0
                s.wavePosition = (s.wavePosition + 1) % WAVE_SIZE
            }

            // Depth fog calculation from glmatrix.c
            val depthRatio = (s.z / GRID_DEPTH) + 0.5f
            val depthScale = 0.2f + (depthRatio.coerceIn(0f, 1f) * 0.8f)

            // Splash fade near camera
            var splashFade = 1.0f
            if (s.z > GRID_DEPTH / 2f) {
                val ratio = (s.z - (GRID_DEPTH / 2f)) / ((GRID_DEPTH * SPLASH_RATIO) - (GRID_DEPTH / 2f))
                val idx = (ratio * WAVE_SIZE).toInt().coerceIn(0, WAVE_SIZE - 1)
                splashFade = brightnessRamp[idx]
            }

            // draw_strip logic
            for (i in 0 until GRID_SIZE) {
                if (quadCount >= maxQuads) break

                val g = s.glyphs[i]
                var belowP = (s.spinnerY >= i)
                if (s.erasing) belowP = !belowP

                if (g != 0 && belowP) {
                    val waveIdx = WAVE_SIZE - ((i + (GRID_SIZE - s.wavePosition)) % WAVE_SIZE)
                    var b = brightnessRamp[waveIdx.coerceIn(0, WAVE_SIZE - 1)]
                    b *= depthScale * splashFade

                    val rCol = state.primaryR * b * dimmer
                    val gCol = state.primaryG * b * dimmer
                    val bCol = state.primaryB * b * dimmer
                    val alpha = (b * dimmer).coerceIn(0.1f, 1.0f)

                    val uvs = textureLoader.getUVs(g)
                    val qx = s.x
                    val qy = s.y - i
                    val qz = s.z

                    vIndex = putQuad(vIndex, qx, qy, qz, quadSize, uvs, rCol, gCol, bCol, alpha)
                    quadCount++
                }
            }

            // Draw spinner head (leading bright character)
            if (!s.erasing && quadCount < maxQuads) {
                val headUvs = textureLoader.getUVs(s.spinnerGlyph)
                val qx = s.x
                val qy = s.y - s.spinnerY
                val qz = s.z

                // Leading head glows white/pastel as in the film and glmatrix.c
                val headR = 0.95f * dimmer
                val headG = 1.0f * dimmer
                val headB = 0.95f * dimmer
                val headA = 1.0f * dimmer

                vIndex = putQuad(vIndex, qx, qy, qz, quadSize, headUvs, headR, headG, headB, headA)
                quadCount++
            }
        }

        if (quadCount > 0) {
            vertexBuffer.position(0)
            vertexBuffer.put(vertexArray, 0, quadCount * 4 * floatsPerVertex)
            vertexBuffer.position(0)

            val stride = floatsPerVertex * 4

            vertexBuffer.position(0)
            GLES20.glEnableVertexAttribArray(aPositionHandle)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)

            vertexBuffer.position(3)
            GLES20.glEnableVertexAttribArray(aTexCoordinateHandle)
            GLES20.glVertexAttribPointer(aTexCoordinateHandle, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer)

            vertexBuffer.position(5)
            GLES20.glEnableVertexAttribArray(aColorHandle)
            GLES20.glVertexAttribPointer(aColorHandle, 4, GLES20.GL_FLOAT, false, stride, vertexBuffer)

            indexBuffer.position(0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, quadCount * 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

            GLES20.glDisableVertexAttribArray(aPositionHandle)
            GLES20.glDisableVertexAttribArray(aTexCoordinateHandle)
            GLES20.glDisableVertexAttribArray(aColorHandle)
        }
    }

    private fun putQuad(
        startIndex: Int,
        x: Float, y: Float, z: Float, size: Float,
        uvs: FloatArray,
        r: Float, g: Float, b: Float, a: Float
    ): Int {
        var i = startIndex
        // 32x46 character aspect ratio from matrix3.png (~0.69565)
        val charAspect = 32f / 46f
        val quadWidth = size * charAspect
        val quadHeight = size

        val x0 = x
        val x1 = x + quadWidth
        val y0 = y
        val y1 = y + quadHeight

        val u0 = uvs[0] // left U
        val v0 = uvs[1] // top V
        val u1 = uvs[2] // right U
        val v1 = uvs[3] // bottom V

        // Vertex 0: Top-Left (x0, y1)
        i = putV(i, x0, y1, z, u0, v0, r, g, b, a)
        // Vertex 1: Bottom-Left (x0, y0)
        i = putV(i, x0, y0, z, u0, v1, r, g, b, a)
        // Vertex 2: Bottom-Right (x1, y0)
        i = putV(i, x1, y0, z, u1, v1, r, g, b, a)
        // Vertex 3: Top-Right (x1, y1)
        i = putV(i, x1, y1, z, u1, v0, r, g, b, a)

        return i
    }

    private fun putV(
        index: Int,
        x: Float, y: Float, z: Float,
        u: Float, v: Float,
        r: Float, g: Float, b: Float, a: Float
    ): Int {
        var i = index
        vertexArray[i++] = x
        vertexArray[i++] = y
        vertexArray[i++] = z
        vertexArray[i++] = u
        vertexArray[i++] = v
        vertexArray[i++] = r
        vertexArray[i++] = g
        vertexArray[i++] = b
        vertexArray[i++] = a
        return i
    }

    private class MatrixStrip(gridSize: Int) {
        var x = 0f
        var y = 0f
        var z = 0f
        var dz = 0f

        var erasing = false

        var spinnerGlyph = 16
        var spinnerY = 0f
        var spinnerSpeed = 0.2f

        val glyphs = IntArray(gridSize)

        var spinSpeed = 1
        var spinTick = 0

        var wavePosition = 0
        var waveSpeed = 1
        var waveTick = 0
    }
}
