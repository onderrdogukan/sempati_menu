package com.sempati.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sempati.app.ui.viewmodel.IstatistikViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstatistikEkrani(onBackClick: () -> Unit, viewModel: IstatistikViewModel) {
    val gunlukCiro by viewModel.gunlukCiro.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dükkan Raporu") }, navigationIcon = {
                IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Geri") }
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Son Günlerin Cirosu", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(gunlukCiro.size) { index ->
                    val tarih = gunlukCiro.keys.toList()[index]
                    val tutar = gunlukCiro[tarih]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tarih)
                            Text("$tutar TL", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}