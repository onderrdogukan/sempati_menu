package com.sempati.app.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sempati.app.SempatiApplication
import com.sempati.app.domain.model.Masa
import com.sempati.app.domain.model.MasaDurumu
import com.sempati.app.ui.utils.VoiceListener
import com.sempati.app.ui.viewmodel.KomutTipi
import com.sempati.app.ui.viewmodel.MasaViewModel
import com.sempati.app.ui.viewmodel.MasaViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

fun zekiMasaBulucu(rawText: String): Int? {
    var islenmisMetin = rawText.lowercase(Locale("tr", "TR"))

    val rakamSozlugu = mapOf(
        "bir" to "1", "iki" to "2", "üç" to "3", "dört" to "4", "beş" to "5",
        "altı" to "6", "yedi" to "7", "sekiz" to "8", "dokuz" to "9", "on" to "10",
        "on bir" to "11", "on iki" to "12", "yirmi" to "20", "otuz" to "30"
    )
    rakamSozlugu.forEach { (kelime, rakam) ->
        islenmisMetin = islenmisMetin.replace(Regex("\\b$kelime\\b"), rakam)
    }

    val regexSonra = Regex("masa(?:\\s+no|\\s+numarası)?\\s+(\\d+)")
    val matchSonra = regexSonra.find(islenmisMetin)
    if (matchSonra != null) return matchSonra.groupValues[1].toInt()

    val regexOnce = Regex("(\\d+)\\s*(?:\\.|nci|inci|numaralı)?\\s*masa")
    val matchOnce = regexOnce.find(islenmisMetin)
    if (matchOnce != null) return matchOnce.groupValues[1].toInt()

    return null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MasalarEkrani(
    onMasaClick: (Int, Boolean) -> Unit,
    onKasaClick: () -> Unit,
    onMenuClick: () -> Unit,
    onIstatistikClick: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as SempatiApplication
    val masaRepository = application.masaRepository
    val urunRepository = application.urunRepository
    val hesapKalemiRepository = application.hesapKalemiRepository
    val viewModel: MasaViewModel = viewModel(
        factory = MasaViewModelFactory(
            masaRepository,
            urunRepository,
            hesapKalemiRepository
        )
    )
    val masalar by viewModel.tumMasalar.collectAsState()

    // Onay mekanizmasını ekrandan dinliyoruz
    val onayBekleyenSiparis by viewModel.pendingOrder.collectAsState()

    // Aksiyon mekanizmasını ekrandan dinliyoruz
    val aktifAksiyon by viewModel.masaAksiyonu.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Toast.makeText(context, "İzin verildi! Tekrar basınız.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Mikrofon izni olmadan sesli komut çalışmaz.", Toast.LENGTH_LONG).show()
    }

    val scope = rememberCoroutineScope()

    val voiceListener = remember(context) {
        VoiceListener(context) { metin ->
            scope.launch {
                val kucukMetin = metin.lowercase(Locale("tr", "TR"))
                val masaNo = zekiMasaBulucu(kucukMetin)

                if (masaNo != null) {
                    val hedonMasa = masalar.find { masa ->
                        val masadakiRakam = masa.ad.replace(Regex("[^0-9]"), "")
                        masadakiRakam == masaNo.toString()
                    }

                    if (hedonMasa != null) {
                        viewModel.sesliSiparisiAyristir(hedonMasa.id, hedonMasa.ad, kucukMetin)
                    } else {
                        Toast.makeText(context, "Sistemde Masa $masaNo bulunamadı!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Hangi masa? Lütfen 'Masa 5...' şeklinde söyleyin.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ViewModel'den gelen anlık hata olaylarını dinleyen profesyonel gözlemci
    LaunchedEffect(Unit) {
        viewModel.sesliKomutHatasi.collect { hataMesaji ->
            Toast.makeText(context, hataMesaji, Toast.LENGTH_LONG).show()
        }
    }

    val siraliMasalar = remember(masalar) {
        masalar.sortedWith(compareBy<Masa> {
            it.ad.replace(Regex("[^0-9]"), "").toIntOrNull() ?: Int.MAX_VALUE
        }.thenBy { it.ad })
    }

    var silinecekMasa by remember { mutableStateOf<Masa?>(null) }
    var duzenlemeModuAcik by remember { mutableStateOf<Masa?>(null) }
    var masaEkleDialogAcik by remember { mutableStateOf(false) }
    var yeniMasaAdi by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masa Düzeni") },
                actions = {
                    IconButton(onClick = { onKasaClick() }) {
                        Icon(Icons.Filled.PointOfSale, "Gün Sonu", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { onIstatistikClick() }) {
                        Icon(Icons.Filled.BarChart, "İstatistik", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { onMenuClick() }) {
                        Icon(Icons.Filled.RestaurantMenu, "Menü", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // KÜÇÜK BUTON: Yeni Masa Ekle
                SmallFloatingActionButton(
                    onClick = {
                        val mevcutNo = masalar.mapNotNull {
                            it.ad.replace(Regex("[^0-9]"), "").toIntOrNull()
                        }
                        var n = 1
                        while (mevcutNo.contains(n)) n++

                        yeniMasaAdi = "Masa $n"
                        masaEkleDialogAcik = true
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) { Icon(Icons.Filled.Add, "Yeni Masa") }

                // DEVASA BUTON: Sesli Komut (Ana Silah)
                FloatingActionButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PermissionChecker.PERMISSION_GRANTED
                        ) {
                            voiceListener.startListening()
                            Toast.makeText(context, "Dinliyorum...", Toast.LENGTH_SHORT).show()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 16.dp
                    ),
                    modifier = Modifier.size(72.dp) // Başparmak için özel devasa boyut
                ) {
                    Icon(Icons.Filled.Mic, "Sesli Komut", modifier = Modifier.size(36.dp))
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = siraliMasalar,
                key = {masa -> masa.id}
                ) { masa ->
                Box(modifier = Modifier.height(100.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = {
                                    if (duzenlemeModuAcik != null) {
                                        duzenlemeModuAcik = null
                                    } else {
                                        onMasaClick(masa.id, false)
                                    }
                                },
                                onLongClick = { duzenlemeModuAcik = masa }
                            ),
                        colors = CardDefaults.cardColors(containerColor = if (masa.durum == MasaDurumu.Bos) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = masa.ad, style = MaterialTheme.typography.titleLarge)
                            Text(text = masa.durum.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (duzenlemeModuAcik == masa) {
                        IconButton(
                            onClick = {
                                if (masa.durum == MasaDurumu.Bos) silinecekMasa = masa else Toast.makeText(context, "Dolu masa silinemez!", Toast.LENGTH_SHORT).show()
                                duzenlemeModuAcik = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Filled.Delete, "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // KUSURSUZ GÜVENLİK: SESLİ SİPARİŞ ONAY DIALOGU
        onayBekleyenSiparis?.let { siparis ->
            AlertDialog(
                onDismissRequest = { viewModel.siparisiIptalEt() },
                title = { Text("${siparis.masaAdi} - Sesli Sipariş Onayı") },
                text = {
                    Column {
                        Text("Sesten algılanan ürünler listeleniyor. Onaylıyor musunuz?", modifier = Modifier.padding(bottom = 8.dp))
                        siparis.items.forEach { item ->
                            Text(
                                text = "• ${item.adet} adet ${item.urun.ad}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onaylananSiparisiKaydet() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Onayla") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.siparisiIptalEt() }) { Text("İptal Et") }
                }
            )
        }

        // YENİ GÜVENLİK DUVARI: SİPARİŞ DEĞİL, AKSİYON ONAYI EKRANI
        aktifAksiyon?.let { aksiyon ->
            val komut = aksiyon.first
            val masa = aksiyon.second

            AlertDialog(
                onDismissRequest = { viewModel.aksiyonuIptalEt() },
                title = { Text("Masa İşlemi Onayı", color = MaterialTheme.colorScheme.error) },
                text = {
                    val islemAdi = if (komut == KomutTipi.HESAP_KAPAT)
                        "hesabını ve adisyonunu tamamen kapatmak"
                    else
                        "adisyonunu yazıcıya göndermek"

                    Text(
                        text = "${masa.ad} $islemAdi istediğinize emin misiniz? Bu işlem geri alınamaz.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.aksiyonuTamamla()
                            onMasaClick(masa.id, true)
                            Toast.makeText(context, "${masa.ad} Tahsilat Ekranı açılıyor...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Evet, Tahsilat Aç") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.aksiyonuIptalEt() }) { Text("İptal") }
                }
            )
        }

        if (silinecekMasa != null) {
            AlertDialog(onDismissRequest = { silinecekMasa = null },
                title = { Text("Masayı Sil") },
                text = { Text("${silinecekMasa?.ad} silinsin mi?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.masaSil(silinecekMasa!!); silinecekMasa = null }) { Text("Evet, Sil", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { silinecekMasa = null }) { Text("İptal") } }
            )
        }

        if (masaEkleDialogAcik) {
            AlertDialog(onDismissRequest = { masaEkleDialogAcik = false },
                title = { Text("Yeni Masa Ekle") },
                text = {
                    OutlinedTextField(
                        value = yeniMasaAdi,
                        onValueChange = { yeniMasaAdi = it },
                        label = { Text("Ad") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (yeniMasaAdi.isNotBlank()) {
                            viewModel.masaEkle(yeniMasaAdi.trim()); masaEkleDialogAcik = false
                        }
                    }) { Text("Ekle") }
                },
                dismissButton = {
                    TextButton(onClick = { masaEkleDialogAcik = false }) { Text("İptal") }
                }
            )
        }
    }
}