package com.appawake.app.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appawake.app.data.AppCategory
import com.appawake.app.data.AppItem
import com.appawake.app.data.AppPreferencesRepository
import com.appawake.app.service.KeepAliveAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppPreferencesRepository.getInstance(application)
    private val packageManager: PackageManager = application.packageManager

    private val _rawInstalledApps = MutableStateFlow<List<AppItem>>(emptyList())
    val isLoading = MutableStateFlow(true)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    val selectedApps = repository.selectedApps
    val isServiceActive = repository.isServiceActive
    val isGhostTouchEnabled = repository.isGhostTouchEnabled
    val ghostTouchIntervalSec = repository.ghostTouchIntervalSec
    val isKeepScreenAwake = repository.isKeepScreenAwake
    val isAutoRelaunch = repository.isAutoRelaunch

    // Live accessibility service states
    val isAccessibilityRunning = KeepAliveAccessibilityService.isServiceRunning
    val activeAwakePackage = KeepAliveAccessibilityService.activeAwakePackage

    val filteredApps: StateFlow<List<AppItem>> = combine(
        _rawInstalledApps,
        _searchQuery,
        _selectedCategory,
        selectedApps
    ) { apps, query, category, selectedSet ->
        apps.map { it.copy(isSelected = selectedSet.contains(it.packageName)) }
            .filter { app ->
                val matchesQuery = query.isBlank() ||
                        app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)

                val matchesCategory = when (category) {
                    AppCategory.ALL -> true
                    AppCategory.ACTIVE -> app.isSelected
                    else -> app.category == category
                }

                matchesQuery && matchesCategory
            }
            .sortedWith(
                compareByDescending<AppItem> { it.isSelected }
                    .thenBy { it.appName.lowercase() }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            val appsList = mutableListOf<AppItem>()

            try {
                val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }

                for (appInfo in packages) {
                    // Filter out our own app
                    if (appInfo.packageName == getApplication<Application>().packageName) continue

                    // Include user apps and launchable system apps
                    val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                        val name = appInfo.loadLabel(packageManager).toString()
                        val icon = try {
                            appInfo.loadIcon(packageManager)
                        } catch (e: Exception) {
                            null
                        }

                        val category = categorizePackage(appInfo.packageName, name)
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                        appsList.add(
                            AppItem(
                                packageName = appInfo.packageName,
                                appName = name,
                                icon = icon,
                                isSystemApp = isSystem,
                                category = category
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _rawInstalledApps.value = appsList
            isLoading.value = false
        }
    }

    private fun categorizePackage(pkg: String, name: String): AppCategory {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()

        return when {
            lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
                    lowerPkg.contains("tiktok") || lowerPkg.contains("whatsapp") ||
                    lowerPkg.contains("twitter") || lowerPkg.contains("snapchat") ||
                    lowerPkg.contains("telegram") || lowerPkg.contains("reddit") -> AppCategory.SOCIAL

            lowerPkg.contains("uber") || lowerPkg.contains("indrive") ||
                    lowerPkg.contains("careem") || lowerPkg.contains("bykea") ||
                    lowerPkg.contains("foodpanda") || lowerPkg.contains("driver") ||
                    lowerPkg.contains("delivery") -> AppCategory.RIDER

            lowerPkg.contains("game") || lowerPkg.contains("pubg") ||
                    lowerPkg.contains("roblox") || lowerPkg.contains("youtube") ||
                    lowerPkg.contains("netflix") || lowerName.contains("game") -> AppCategory.GAMES

            else -> AppCategory.OTHER
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun toggleApp(packageName: String) {
        repository.toggleApp(packageName)
    }

    fun selectAllVisible() {
        val pkgs = filteredApps.value.map { it.packageName }
        repository.selectAll(pkgs)
    }

    fun deselectAll() {
        repository.deselectAll()
    }

    fun toggleMaster(active: Boolean) {
        repository.setServiceActive(active)
    }

    fun toggleKeepScreen(enabled: Boolean) {
        repository.setKeepScreenAwake(enabled)
    }

    fun toggleGhostTouch(enabled: Boolean) {
        repository.setGhostTouchEnabled(enabled)
    }

    fun setGhostTouchInterval(seconds: Int) {
        repository.setGhostTouchInterval(seconds)
    }

    fun toggleAutoRelaunch(enabled: Boolean) {
        repository.setAutoRelaunch(enabled)
    }

    fun isAccessibilityPermissionGranted(context: Context): Boolean {
        val service = "${context.packageName}/${KeepAliveAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(service, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
