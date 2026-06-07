package com.calculator.vault.security.detection

import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntruderCaptureCoordinator @Inject constructor(
    private val intruderCaptureManager: IntruderCaptureManager,
) {
    @Volatile
    var lifecycleOwner: LifecycleOwner? = null

    @Volatile
    var hasCameraPermission: () -> Boolean = { false }

    private val executor: Executor = Executors.newSingleThreadExecutor()

    suspend fun capturePhoto(): String? {
        if (!hasCameraPermission()) return null
        val owner = lifecycleOwner ?: return null
        return intruderCaptureManager.capturePhoto(owner, executor)
    }
}
