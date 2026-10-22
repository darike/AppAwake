package com.appawake.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.appawake.app.data.AppPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KeepAliveAccessibilityService : AccessibilityService() {

    private lateinit var repository: AppPreferencesRepository
    private var wakeLock: PowerManager.WakeLock? = null
    private var windowManager: WindowManager? = null
    private var overlayKeepScreenOnView: View? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var ghostTouchJob: Job? = null

    private var currentForegroundApp: String? = null

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private val _activeAwakePackage = MutableStateFlow<String?>(null)
        val activeAwakePackage = _activeAwakePackage.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppPreferencesRepository.getInstance(applicationContext)
        _isServiceRunning.value = true

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "AppAwake::AccessibilityWakeLock"
        )
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return

            // Ignore system UI overlays, launchers, keyboards
            if (pkgName == "com.android.systemui" || pkgName == "com.google.android.inputmethod.latin") {
                return
            }

            currentForegroundApp = pkgName
            evaluateKeepAliveState(pkgName)
        }
    }

    private fun evaluateKeepAliveState(packageName: String) {
        val selectedApps = repository.selectedApps.value
        val isMasterOn = repository.isServiceActive.value

        if (isMasterOn && selectedApps.contains(packageName)) {
            // Target app is currently in foreground!
            _activeAwakePackage.value = packageName
            activateKeepAlive()
        } else {
            // Not a target app or master switch is OFF
            _activeAwakePackage.value = null
            deactivateKeepAlive()
        }
    }

    private fun activateKeepAlive() {
        // 1. Keep Screen Awake using WakeLock & System Overlay
        if (repository.isKeepScreenAwake.value) {
            acquireWakeLock()
            attachKeepScreenOnOverlay()
        }

        // 2. Start Ghost Touch heartbeat if enabled
        if (repository.isGhostTouchEnabled.value) {
            startGhostTouchLoop()
        } else {
            stopGhostTouchLoop()
        }
    }

    private fun deactivateKeepAlive() {
        releaseWakeLock()
        removeKeepScreenOnOverlay()
        stopGhostTouchLoop()
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24 hours max safeguard
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun attachKeepScreenOnOverlay() {
        if (overlayKeepScreenOnView != null || windowManager == null) return

        try {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                1, 1, // 1x1 invisible pixel
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            val view = View(this)
            windowManager?.addView(view, params)
            overlayKeepScreenOnView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeKeepScreenOnOverlay() {
        overlayKeepScreenOnView?.let { view ->
            try {
                windowManager?.removeViewImmediate(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayKeepScreenOnView = null
    }

    private fun startGhostTouchLoop() {
        ghostTouchJob?.cancel()
        val intervalMs = (repository.ghostTouchIntervalSec.value.coerceAtLeast(10)) * 1000L

        ghostTouchJob = serviceScope.launch {
            while (isActive) {
                delay(intervalMs)
                // Execute harmless micro-flick on screen corner
                performMicroGesture()
            }
        }
    }

    private fun stopGhostTouchLoop() {
        ghostTouchJob?.cancel()
        ghostTouchJob = null
    }

    /**
     * Simulates a 1-pixel micro-swipe in safe area so target apps remain active and reset idle timers.
     */
    private fun performMicroGesture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            // Micro gesture at top safe edge (x: 200, y: 100 to y: 101)
            path.moveTo(200f, 100f)
            path.lineTo(200f, 101f)

            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                }
            }, Handler(Looper.getMainLooper()))
        }
    }

    override fun onInterrupt() {
        deactivateKeepAlive()
    }

    override fun onDestroy() {
        super.onDestroy()
        deactivateKeepAlive()
        _isServiceRunning.value = false
        _activeAwakePackage.value = null
    }
}
