package com.fj.launcher.glmatrix

import com.fj.launcher.model.MatrixTheme
import java.util.concurrent.atomic.AtomicBoolean

class GLMatrixState {
    var theme: MatrixTheme = MatrixTheme.CLASSIC_GREEN
        set(value) {
            field = value
            updateColors()
        }

    var speedMultiplier: Float = 1.0f
    var density: Float = 1.0f
    var cameraSwayEnabled: Boolean = true
    var dimmerAlpha: Float = 0.55f
    var mode: String = "matrix" // "matrix", "binary", "hexadecimal", "decimal", "dna"
    var wavesEnabled: Boolean = true
    var fogEnabled: Boolean = true

    val isPaused: AtomicBoolean = AtomicBoolean(false)

    // Current primary color RGB (0.0f - 1.0f)
    var primaryR: Float = 0.0f
        private set
    var primaryG: Float = 1.0f
        private set
    var primaryB: Float = 0.25f
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
            }
            MatrixTheme.CYBERPUNK_CYAN -> {
                primaryR = 0.0f
                primaryG = 0.94f
                primaryB = 1.0f
            }
            MatrixTheme.GOLDEN_AMBER -> {
                primaryR = 1.0f
                primaryG = 0.69f
                primaryB = 0.0f
            }
            MatrixTheme.BLOOD_RED -> {
                primaryR = 1.0f
                primaryG = 0.2f
                primaryB = 0.2f
            }
            MatrixTheme.HACKER_WHITE -> {
                primaryR = 0.95f
                primaryG = 0.95f
                primaryB = 0.95f
            }
        }
    }
}
