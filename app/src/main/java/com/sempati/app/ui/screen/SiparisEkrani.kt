package com.sempati.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sempati.app.SempatiApplication
import com.sempati.app.ui.viewmodel.SiparisViewModel
import com.sempati.app.ui.viewmodel.SiparisViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiparisEkrani(
    masaId: Int,
    direktTahsilat: Boolean = false, // 1. YENİ SİLAH: Ekranın dışarıdan aldığı komut bayrağı
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as SempatiApplication
    val scope = rememberCoroutineScope()

    var odemeDialogGoster by remember { mutableStateOf(false) }
    var menuDialogGoster by remember { mutableStateOf(false) }
    var girilenTutar by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: SiparisViewModel = viewModel(
        factory = SiparisViewModelFactory(
            masaId = masaId,
            urunRepository = application.urunRepository,
            hesapKalemiRepository = application.hesapKalemiRepository,
            masaRepository = application.masaRepository,
            satisGecmisiRepository = application.satisGecmisiRepository
        )
    )

    val aktifUrunler by viewModel.aktifUrunler.collectAsState()
    val masaninHesabi by viewModel.masaninHesabi.collectAsState()
    val masaAdi by viewModel.masaAdi.collectAsState()
    val toplamTutar by viewModel.toplamTutar.collectAsState()

    // 2. KESKİN NİŞANCI (LAUNCHED EFFECT) MANTIĞI:
    // Bu blok 'direktTahsilat' ve 'toplamTutar' değişkenlerini anlık dinler.
    // Eğer sesten "Tahsilatı aç" emri gelmişse VE veritabanı tutarı 0'dan büyükse (yani veri yüklendiyse):
    LaunchedEffect(direktTahsilat, toplamTutar) {
        if (direktTahsilat && !odemeDialogGoster && toplamTutar > 0.0) {
            girilenTutar = toplamTutar.toString() // Tutarı otomatik doldur
            odemeDialogGoster = true // Onay popup'ını yüzüne fırlat
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("$masaAdi - Adisyon") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { menuDialogGoster = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Menu, "Menüyü Aç") },
                text = { Text("Menü", style = MaterialTheme.typography.titleMedium) }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(masaninHesabi) { kalem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${kalem.urunAdiSnapshot} (x${kalem.adet})", style = MaterialTheme.typography.bodyLarge)
                                Text("${kalem.birimFiyatSnapshot} TL", style = MaterialTheme.typography.bodyMedium)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    viewModel.adisyondanSil(kalem)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${kalem.urunAdiSnapshot} silindi",
                                        actionLabel = "GERİ AL",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.geriAl()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, "Sil", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Genel Toplam:", style = MaterialTheme.typography.titleLarge)
                Text("$toplamTutar TL", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MANUEL HESAP KAPAT BUTONU
            Button(
                onClick = { girilenTutar = toplamTutar.toString(); odemeDialogGoster = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = masaninHesabi.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Hesabı Kapat", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (menuDialogGoster) {
            AlertDialog(
                onDismissRequest = { menuDialogGoster = false },
                title = { Text("Menü") },
                text = {
                    LazyColumn {
                        items(aktifUrunler) { urun ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable { viewModel.adisyonaEkle(urun) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(urun.ad)
                                    Text("${urun.fiyat} TL")
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { menuDialogGoster = false }) { Text("Kapat") } }
            )
        }

        // TAHSİLAT DİYALOGU (Hem sesle hem elle tetiklenir)
        if (odemeDialogGoster) {
            AlertDialog(
                onDismissRequest = { odemeDialogGoster = false },
                title = { Text("Tahsilat") },
                text = {
                    OutlinedTextField(value = girilenTutar, onValueChange = { girilenTutar = it }, label = { Text("Tutar") })
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.hesabiKapat(girilenTutar.toDoubleOrNull() ?: 0.0) {
                            odemeDialogGoster = false
                            onBackClick() // Ödeme başarılıysa ana ekrana (Masalar) geri dön
                        }
                    }) { Text("Tahsil Et") }
                }
            )
        }
    }
}