package com.sempati.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sempati.app.SempatiApplication
import com.sempati.app.ui.viewmodel.UrunYonetimiViewModel
import com.sempati.app.ui.viewmodel.UrunYonetimiViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrunYonetimiEkrani(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as SempatiApplication

    val viewModel: UrunYonetimiViewModel = viewModel(
        factory = UrunYonetimiViewModelFactory(urunRepository = application.urunRepository)
    )

    val tumUrunler by viewModel.tumUrunler.collectAsState()

    // Ekleme kutularının State'leri
    var yeniUrunAdi by remember { mutableStateOf("") }
    var yeniUrunFiyat by remember { mutableStateOf("") }

    // DÜZENLEME (UPDATE) State'leri
    var duzenlenecekUrun by remember { mutableStateOf<com.sempati.app.domain.model.Urun?>(null) }
    var duzenlenenAd by remember { mutableStateOf("") }
    var duzenlenenFiyat by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menü / Ürün Yönetimi") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ================= ÜST BÖLÜM: ÜRÜN EKLEME FORMU =================
            Text("Yeni Ürün Ekle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = yeniUrunAdi,
                    onValueChange = { yeniUrunAdi = it },
                    label = { Text("Ürün Adı") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = yeniUrunFiyat,
                    onValueChange = { yeniUrunFiyat = it },
                    label = { Text("Fiyat (TL)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val fiyatDouble = yeniUrunFiyat.toDoubleOrNull() ?: 0.0
                        viewModel.urunEkle(yeniUrunAdi, fiyatDouble)
                        yeniUrunAdi = ""
                        yeniUrunFiyat = ""
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Ekle")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Mevcut Menü Listesi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ================= ALT BÖLÜM: MEVCUT ÜRÜNLERİN LİSTESİ =================
            if (tumUrunler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Menüde henüz ürün yok. Yukarıdan ekleyin.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tumUrunler) { urun ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(urun.ad, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("${urun.fiyat} TL", color = MaterialTheme.colorScheme.primary)
                                }

                                Row {
                                    // 1. YENİ SİLAH: DÜZENLE BUTONU
                                    IconButton(onClick = {
                                        duzenlenenAd = urun.ad
                                        duzenlenenFiyat = urun.fiyat.toString()
                                        duzenlenecekUrun = urun // Dialog'u tetikler
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Ürünü Düzenle",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // 2. ESKİ SİLAH: SİL BUTONU
                                    IconButton(onClick = { viewModel.urunSil(urun.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Ürünü Menüden Sil",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= GÜVENLİK DUVARI: DÜZENLEME (UPDATE) DİYALOGU =================
        duzenlenecekUrun?.let { urun ->
            AlertDialog(
                onDismissRequest = { duzenlenecekUrun = null },
                title = { Text("Ürünü Düzenle") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = duzenlenenAd,
                            onValueChange = { duzenlenenAd = it },
                            label = { Text("Ürün Adı") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = duzenlenenFiyat,
                            onValueChange = { duzenlenenFiyat = it },
                            label = { Text("Fiyat (TL)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val yeniFiyat = duzenlenenFiyat.toDoubleOrNull() ?: urun.fiyat
                            // Veriyi ID'si bozulmadan yeni değerleriyle üzerine yazar (UPDATE)
                            viewModel.urunGuncelle(urun.copy(ad = duzenlenenAd.trim(), fiyat = yeniFiyat))
                            duzenlenecekUrun = null
                        }
                    ) { Text("Kaydet") }
                },
                dismissButton = {
                    TextButton(onClick = { duzenlenecekUrun = null }) { Text("İptal") }
                }
            )
        }
    }
}