package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.AppLayout
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.UnitViewModel

class UnitsDetailsScreen( private val unitId:Int ): Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val unitUi by unitVm.state.collectAsState()
        var selectedIndex by remember { mutableStateOf(2) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(unitUi.error) {
            unitUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        val unit = unitUi.unit.find { it.id == unitId }

        AppLayout(
            actualScreen = "Administrar Unidad",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8F0))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (unit != null) {
                    Text(unit.name)
                }
            }
        }

    }
}