package com.sempati.app.ui.navigation

// Uygulamadaki tüm sayfaları ve rotalarını burada katı kurallarla (Sealed Class) tanımlıyoruz.
// Bu sayede "MasaListesi" yerine yanlışlıkla "MasaLisesi" yazıp uygulamayı çökertmenin önüne geçiyoruz.
sealed class Screen(val route: String) {
    object Masalar : Screen("masalar_ekrani")

    // Sipariş ekranı, hangi masaya tıklandığını bilmek zorunda (masaId parametresi alır)
    object Siparis : Screen("siparis_ekrani/{masaId}") {
        fun createRoute(masaId: Int) = "siparis_ekrani/$masaId"
    }
}