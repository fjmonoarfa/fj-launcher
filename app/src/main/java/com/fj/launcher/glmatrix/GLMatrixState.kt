package com.fj.launcher.glmatrix

import com.fj.launcher.model.MatrixTheme
import java.util.concurrent.atomic.AtomicBoolean

class GLMatrixState {
    @Volatile
    var theme: MatrixTheme = MatrixTheme.CLASSIC_GREEN
        set(value) {
            field = value
            updateColors()
        }

    @Volatile
    var speedMultiplier: Float = 1.0f
    @Volatile
    var density: Float = 1.0f
    @Volatile
    var cameraSwayEnabled: Boolean = true
    @Volatile
    var dimmerAlpha: Float = 0.55f
    @Volatile
    var mode: String = "matrix" // "matrix", "binary", "hexadecimal", "decimal", "dna"
    @Volatile
    var wavesEnabled: Boolean = true
    @Volatile
    var fogEnabled: Boolean = true

    val isPaused: AtomicBoolean = AtomicBoolean(false)

    // Current primary color RGB (0.0f - 1.0f)
    @Volatile
    var primaryR: Float = 0.0f
        private set
    @Volatile
    var primaryG: Float = 1.0f
        private set
    @Volatile
    var primaryB: Float = 0.25f
        private set

    // Current leading head (brightest tip) color RGB (0.0f - 1.0f)
    @Volatile
    var headR: Float = 0.2f
        private set
    @Volatile
    var headG: Float = 1.0f
        private set
    @Volatile
    var headB: Float = 0.35f
        private set

    init {
        updateColors()
    }

    private fun updateColors() {
        when (theme) {
            MatrixTheme.CLASSIC_GREEN -> {
                primaryR = 0.0f
                primaryG = 1.0f
                primaryB = 0.25f
                headR = 0.2f
                headG = 1.0f
                headB = 0.35f
            }
            MatrixTheme.CYBERPUNK_CYAN -> {
                primaryR = 0.0f
                primaryG = 0.94f
                primaryB = 1.0f
                headR = 0.35f
                headG = 1.0f
                headB = 1.0f
            }
            MatrixTheme.GOLDEN_AMBER -> {
                primaryR = 1.0f
                primaryG = 0.69f
                primaryB = 0.0f
                headR = 1.0f
                headG = 0.92f
                headB = 0.35f
            }
            MatrixTheme.BLOOD_RED -> {
                primaryR = 1.0f
                primaryG = 0.2f
                primaryB = 0.2f
                headR = 1.0f
                headG = 0.5f
                headB = 0.5f
            }
            MatrixTheme.HACKER_WHITE -> {
                primaryR = 0.95f
                primaryG = 0.95f
                primaryB = 0.95f
                headR = 1.0f
                headG = 1.0f
                headB = 1.0f
            }
        }
    }
}
