package com.sempati.app.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sempati.app.SempatiApplication
import com.sempati.app.domain.model.SatisGecmisi
import com.sempati.app.ui.viewmodel.KasaViewModel
import com.sempati.app.ui.viewmodel.KasaViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ================= YARDIMCI GÜVENLİK VE RAPORLAMA FONKSİYONLARI =================

fun zRaporuPaylas(context: Context, tarih: String, ciro: Double, indirim: Double, islemSayisi: Int) {
    val raporMesaji = """
        *SEMPATİ POS - Z-RAPORU*
        Tarih: $tarih
        ------------------------
        💰 Net Ciro: $ciro TL
        🔻 Yapılan İndirim: $indirim TL
        🧾 Toplam İşlem: $islemSayisi
        ------------------------
        Kasa başarıyla kapatılmıştır.
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, raporMesaji)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Z-Raporunu Paylaş"))
}

fun veritabaniniYedekle(context: Context, islemler: List<SatisGecmisi>) {
    try {
        // Cihazın Belgeler (Documents) klasörüne Sempati klasörü aç
        val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SempatiYedekler")
        if (!backupDir.exists()) backupDir.mkdirs()

        val zamanDamgasi = SimpleDateFormat("yyyyMMdd_HHmm", Locale("tr")).format(Date())
        val backupFile = File(backupDir, "Sempati_Yedek_$zamanDamgasi.txt")

        // İşlemleri basit bir metin dosyasına yazdır (Kriz anında okunabilmesi için)
        val veriLogu = islemler.joinToString("\n") { "Masa: ${it.masaId} | Alınan: ${it.alinanTutar} TL | Tarih: ${it.tarihSaat}" }

        FileOutputStream(backupFile).use { output ->
            output.write(veriLogu.toByteArray())
        }
        Toast.makeText(context, "Sistem Yedeklendi: Belgeler/SempatiYedekler", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Yedekleme Başarısız! Depolama izni gerekebilir.", Toast.LENGTH_LONG).show()
    }
}

// ================= ARAYÜZ (UI) TASARIMI =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasaEkrani(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as SempatiApplication

    val viewModel: KasaViewModel = viewModel(
        factory = KasaViewModelFactory(repository = application.satisGecmisiRepository)
    )

    val gorunenTarih by viewModel.gorunenTarih.collectAsState()
    val filtrelenmisIslemler by viewModel.filtrelenmisIslemler.collectAsState()
    val gunlukCiro by viewModel.gunlukCiro.collectAsState()
    val gunlukIndirim by viewModel.gunlukIndirim.collectAsState()

    val bugununTarihi = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(System.currentTimeMillis() - (4L * 60 * 60 * 1000))
    val saatFormatlayici = SimpleDateFormat("HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gün Sonu (Z-Raporu)") },
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
            // 1. NAVİGASYON: TARİH SEÇİCİ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.oncekiGun() }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Önceki Gün")
                }

                Text(
                    text = gorunenTarih,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.sonrakiGun() },
                    enabled = gorunenTarih != bugununTarihi
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Sonraki Gün")
                }
            }

            // 2. ÜST PANEL: GÜNLÜK ÖZET KARTLARI
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Net Ciro", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$gunlukCiro TL",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Yapılan İndirim", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$gunlukIndirim TL",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= 3. YENİ BÖLÜM: AKSİYON MERKEZİ (PAYLAŞ & YEDEKLE) =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        zRaporuPaylas(context, gorunenTarih, gunlukCiro, gunlukIndirim, filtrelenmisIslemler.size)
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Paylaş", modifier = Modifier.padding(end = 8.dp))
                    Text("Paylaş", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { veritabaniniYedekle(context, filtrelenmisIslemler) },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = "Yedekle", modifier = Modifier.padding(end = 8.dp))
                    Text("Yedekle", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Geçmiş İşlemler", style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 4. ALT PANEL: GÜNLÜK İŞLEM LİSTESİ
            if (filtrelenmisIslemler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bu tarihte işlem bulunmamaktadır.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtrelenmisIslemler) { satis ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Masa ${satis.masaId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = saatFormatlayici.format(Date(satis.tarihSaat)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tahsil: ${satis.alinanTutar} TL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    if (satis.hesaplananTutar != satis.alinanTutar) {
                                        Text(
                                            "Hesap: ${satis.hesaplananTutar} TL",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}