package org.example.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.dtos.Role
import org.example.project.viewModel.UsersUiState
import kotlin.math.round

@Composable
fun UserStatisticsSection(
    state: UsersUiState,
    modifier: Modifier = Modifier
) {
    val currentUserRol = state.currentUser?.role ?: "student"
    val completedUnitsCount = state.userStats?.completedUnitsCount ?: state.completedUnits.size
    val completedExercisesCount = state.userStats?.completedExercisesCount ?: state.completedExercises.size
    val completedTestsCount = state.userStats?.completedTestsCount ?: state.completedTests.size
    val failedTestsCount = state.userStats?.failedTestsCount ?: state.failedTests.size
    val weeklyHours = state.userStats?.weeklyHours ?: state.weeklyHours

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Estadísticas del usuario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        when {
            state.statsLoading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            state.statsError != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = state.statsError,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            else -> {
                if(currentUserRol != Role.STUDENT){
                    StatGridRow(
                        leftTitle = "Horas semanales",
                        leftValue = weeklyHours.formatHours(),
                        leftColor = Color(0xFFF9FAFB)
                    )
                }else{
                    StatGridRow(
                        leftTitle = "Unidades completadas",
                        leftValue = completedUnitsCount.toString(),
                        leftColor = Color(0xFFF7FFF3),
                        rightTitle = "Ejercicios completados",
                        rightValue = completedExercisesCount.toString(),
                        rightColor = Color(0xFFFFF9EA)
                    )
                    StatGridRow(
                        leftTitle = "Tests completados",
                        leftValue = completedTestsCount.toString(),
                        leftColor = Color(0xFFF3F8FF),
                        rightTitle = "Tests fallidos",
                        rightValue = failedTestsCount.toString(),
                        rightColor = Color(0xFFFFF1F1)
                    )
                    StatGridRow(
                        leftTitle = "Horas semanales",
                        leftValue = weeklyHours.formatHours(),
                        leftColor = Color(0xFFF9FAFB)
                    )

                }

            }
        }
    }
}

@Composable
private fun StatGridRow(
    leftTitle: String,
    leftValue: String,
    leftColor: Color,
    rightTitle: String? = null,
    rightValue: String? = null,
    rightColor: Color? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            title = leftTitle,
            value = leftValue,
            background = leftColor,
            modifier = Modifier.weight(1f)
        )
        if (rightTitle != null && rightValue != null && rightColor != null) {
            StatCard(
                title = rightTitle,
                value = rightValue,
                background = rightColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}

private fun Double.formatHours(): String {
    val rounded = round(this * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
