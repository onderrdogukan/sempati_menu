package com.sempati.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sempati.app.ui.screen.*
import com.sempati.app.ui.theme.SempatiTheme
import com.sempati.app.ui.viewmodel.IstatistikViewModel
import com.sempati.app.ui.viewmodel.IstatistikViewModelFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SempatiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SempatiApp()
                }
            }
        }
    }
}

@Composable
fun SempatiApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as SempatiApplication

    // NavHost: Tüm sayfaların ana sahnesi
    NavHost(navController = navController, startDestination = "masalar") {

        // 1. ANA EKRAN (MASALAR)
        composable("masalar") {
            MasalarEkrani(
                // BURASI GÜNCELLENDİ: Artık direktTahsilat bayrağını da yakalıyor
                onMasaClick = { masaId, direktTahsilat ->
                    navController.navigate("siparis/$masaId?direktTahsilat=$direktTahsilat")
                },
                onKasaClick = {
                    navController.navigate("kasa")
                },
                onMenuClick = {
                    navController.navigate("menu")
                },
                onIstatistikClick = {
                    navController.navigate("istatistik")
                }
            )
        }

        // 2. SİPARİŞ EKRANI (GÜNCELLENDİ)
        composable(
            route = "siparis/{masaId}?direktTahsilat={direktTahsilat}", // URL şemasına opsiyonel parametre eklendi
            arguments = listOf(
                navArgument("masaId") { type = NavType.IntType },
                navArgument("direktTahsilat") { type = NavType.BoolType; defaultValue = false } // Gelmezse varsayılan false
            )
        ) { backStackEntry ->
            val masaId = backStackEntry.arguments?.getInt("masaId") ?: 1
            val direktTahsilat = backStackEntry.arguments?.getBoolean("direktTahsilat") ?: false

            SiparisEkrani(
                masaId = masaId,
                direktTahsilat = direktTahsilat,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. KASA (Z-RAPORU) EKRANI
        composable("kasa") {
            KasaEkrani(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 4. MENÜ YÖNETİMİ EKRANI
        composable("menu") {
            UrunYonetimiEkrani(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 5. İSTATİSTİK / RAPORLAR EKRANI
        composable("istatistik") {
            val istatistikViewModel: IstatistikViewModel = viewModel(
                factory = IstatistikViewModelFactory(application.satisGecmisiRepository)
            )
            IstatistikEkrani(
                onBackClick = { navController.popBackStack() },
                viewModel = istatistikViewModel
            )
        }
    }
}