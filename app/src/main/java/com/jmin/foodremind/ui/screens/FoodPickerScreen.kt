package com.jmin.foodremind.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.CustomizeMenuDialog
import com.jmin.foodremind.ui.components.FoodCard
import com.jmin.foodremind.ui.components.PrimaryButton
import com.jmin.foodremind.ui.components.RecordMealDialog
import com.jmin.foodremind.ui.components.SecondaryButton
import com.jmin.foodremind.ui.theme.PrimaryColor
import com.jmin.foodremind.ui.theme.SecondaryColor
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext

@Composable
fun FoodPickerScreen(
    viewModel: FoodViewModel
) {
    val scrollState = rememberScrollState()
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }
    var displayedFood by remember { mutableStateOf<String?>(null) }
    var party by remember { mutableStateOf<Party?>(null) }
    val context = LocalContext.current

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            val foodOptions = viewModel.foodOptions
            if (foodOptions.size < 2) {
                isSpinning = false
                return@LaunchedEffect
            }

            val startTime = System.currentTimeMillis()
            val duration = 3000L // 动画持续3秒
            var lastIndex = -1

            while (System.currentTimeMillis() - startTime < duration) {
                var newIndex: Int
                do {
                    newIndex = Random.nextInt(foodOptions.size)
                } while (newIndex == lastIndex)
                
                displayedFood = foodOptions[newIndex]
                lastIndex = newIndex
                delay(100) // 每100毫秒切换一次
            }

            val finalFood = foodOptions.random()
            displayedFood = finalFood
            viewModel.setFinalSelectedFood(finalFood)
            isSpinning = false
            party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
            // Trigger vibration
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Slot machine card
            FoodCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Slot machine container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                     if (viewModel.pickCount >= 3) {
                        Text(
                            text = stringResource(id = R.string.stop_picking_message),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                    } else if (viewModel.foodOptions.isNotEmpty()) {
                        Text(
                            text = displayedFood ?: viewModel.foodOptions.first(),
                            style = (if (isSpinning) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge).copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSpinning) Color.Gray else PrimaryColor,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Show empty state hint
                        Text(
                            text = "Please add food options first",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Result display
                if (viewModel.selectedFood != null && !isSpinning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.pick_result_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.selectedFood!!,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { showRecordDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryColor
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.btn_record_meal),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start picking/Pick again button
            PrimaryButton(
                text = stringResource(
                    id = if (viewModel.selectedFood == null) R.string.btn_start_pick else R.string.btn_pick_again
                ),
                icon = Icons.Filled.Refresh,
                onClick = {
                    if (viewModel.foodOptions.size >= 2) {
                        isSpinning = true
                    }
                },
                enabled = !isSpinning && viewModel.pickCount < 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Manage menu button
            SecondaryButton(
                text = stringResource(id = R.string.btn_manage_menu),
                icon = Icons.Filled.Settings,
                onClick = { showCustomizeDialog = true }
            )
        }

        if (party != null) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party!!),
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) {
                            party = null
                        }
                    }
                }
            )
        }

        // Customize menu dialog
        if (showCustomizeDialog) {
            CustomizeMenuDialog(
                foodOptions = viewModel.foodOptions,
                onDismiss = { showCustomizeDialog = false },
                onConfirm = { options ->
                    if (options.size >= 2) {
                        // Use ViewModel method to update food options
                        viewModel.updateFoodOptions(options)
                        showCustomizeDialog = false
                    }
                }
            )
        }

        // Record meal dialog
        if (showRecordDialog) {
            RecordMealDialog(
                initialMealName = viewModel.selectedFood ?: "",
                onDismiss = { showRecordDialog = false },
                onConfirm = { name, cost, taste ->
                    viewModel.addMealRecord(name, cost, taste)
                    showRecordDialog = false
                }
            )
        }
    }
} 