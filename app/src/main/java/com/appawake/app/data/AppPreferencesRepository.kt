package com.appawake.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_awake_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_APPS = "key_selected_apps"
        private const val KEY_SERVICE_ACTIVE = "key_service_active"
        private const val KEY_GHOST_TOUCH = "key_ghost_touch"
        private const val KEY_GHOST_TOUCH_INTERVAL = "key_ghost_touch_interval"
        private const val KEY_KEEP_SCREEN_AWAKE = "key_keep_screen_awake"
        private const val KEY_AUTO_RELAUNCH = "key_auto_relaunch"

        @Volatile
        private var INSTANCE: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _selectedApps = MutableStateFlow<Set<String>>(loadSelectedApps())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _isServiceActive = MutableStateFlow(prefs.getBoolean(KEY_SERVICE_ACTIVE, true))
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _isGhostTouchEnabled = MutableStateFlow(prefs.getBoolean(KEY_GHOST_TOUCH, true))
    val isGhostTouchEnabled: StateFlow<Boolean> = _isGhostTouchEnabled.asStateFlow()

    private val _ghostTouchIntervalSec = MutableStateFlow(prefs.getInt(KEY_GHOST_TOUCH_INTERVAL, 30))
    val ghostTouchIntervalSec: StateFlow<Int> = _ghostTouchIntervalSec.asStateFlow()

    private val _isKeepScreenAwake = MutableStateFlow(prefs.getBoolean(KEY_KEEP_SCREEN_AWAKE, true))
    val isKeepScreenAwake: StateFlow<Boolean> = _isKeepScreenAwake.asStateFlow()

    private val _isAutoRelaunch = MutableStateFlow(prefs.getBoolean(KEY_AUTO_RELAUNCH, false))
    val isAutoRelaunch: StateFlow<Boolean> = _isAutoRelaunch.asStateFlow()

    private fun loadSelectedApps(): Set<String> {
        return prefs.getStringSet(KEY_SELECTED_APPS, emptySet()) ?: emptySet()
    }

    fun toggleApp(packageName: String) {
        val current = _selectedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedApps.value = current
        prefs.edit().putStringSet(KEY_SELECTED_APPS, current).apply()
    }

    fun selectAll(packages: List<String>) {
        val current = _selectedApps.value.toMutableSet()
        current.addAll(packages)
        _selectedApps.value = current
        prefs.edit().putStringSet(KEY_SELECTED_APPS, current).apply()
    }

    fun deselectAll() {
        _selectedApps.value = emptySet()
        prefs.edit().putStringSet(KEY_SELECTED_APPS, emptySet()).apply()
    }

    fun setServiceActive(active: Boolean) {
        _isServiceActive.value = active
        prefs.edit().putBoolean(KEY_SERVICE_ACTIVE, active).apply()
    }

    fun setGhostTouchEnabled(enabled: Boolean) {
        _isGhostTouchEnabled.value = enabled
        prefs.edit().putBoolean(KEY_GHOST_TOUCH, enabled).apply()
    }

    fun setGhostTouchInterval(seconds: Int) {
        _ghostTouchIntervalSec.value = seconds
        prefs.edit().putInt(KEY_GHOST_TOUCH_INTERVAL, seconds).apply()
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        _isKeepScreenAwake.value = enabled
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_AWAKE, enabled).apply()
    }

    fun setAutoRelaunch(enabled: Boolean) {
        _isAutoRelaunch.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_RELAUNCH, enabled).apply()
    }
}
