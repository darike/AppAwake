package com.appawake.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appawake.app.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isKeepScreenAwake: Boolean,
    isGhostTouchEnabled: Boolean,
    ghostTouchInterval: Int,
    isAutoRelaunch: Boolean,
    onToggleKeepScreen: (Boolean) -> Unit,
    onToggleGhostTouch: (Boolean) -> Unit,
    onChangeInterval: (Int) -> Unit,
    onToggleAutoRelaunch: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Engine Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure how target apps are kept awake and active",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Keep Screen Awake
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prevent Screen Sleep",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Screen will never turn off while target apps are open",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isKeepScreenAwake,
                    onCheckedChange = onToggleKeepScreen,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Ghost Touch (Anti-Idle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ghost Touch (Anti-Idle)",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Simulates tiny touch so Instagram/Games don't go idle",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isGhostTouchEnabled,
                    onCheckedChange = onToggleGhostTouch,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                )
            }

            if (isGhostTouchEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Touch Interval: ${ghostTouchInterval}s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60, 120).forEach { sec ->
                        FilterChip(
                            selected = ghostTouchInterval == sec,
                            onClick = { onChangeInterval(sec) },
                            label = { Text("${sec}s", fontSize = 11.sp) }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Auto-Relaunch on close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Relaunch on Exit",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automatically reopens target app if it gets minimized or killed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAutoRelaunch,
                    onCheckedChange = onToggleAutoRelaunch,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                )
            }
        }
    }
}
