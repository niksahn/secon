package ru.secon.ui.auth.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

data object ProfileUi : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ProfileViewModel>()
        Scaffold {
            Box(
                Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp).height(64.dp),
                    onClick = viewModel::logout,
                    content = {
                        Text("Выйти")
                    }
                )
            }
        }
    }
}
