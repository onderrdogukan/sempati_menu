package com.sempati.app.ui.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast

class VoiceListener(private val context: Context, private val onResult: (String) -> Unit) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        // 1. Serbest konuşma modeli: Uzun ve devrik cümleleri daha iyi anlar
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        // 2. Dili kesin olarak Türkçe'ye zorluyoruz
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")

        // 3. Kısmi sonuçları açıyoruz
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        // 4. Sadece 1 tahmin değil, en olası 3 tahmini iste (Yapay zeka için alternatifler)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        // 5. Arka plan gürültüsünü filtrelemeye yardımcı olan özel ayarlar
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
    }

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                // Şimdilik en yüksek güvenilirlikli (ilk) sonucu alıp ViewModel'a gönderiyoruz
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onResult(it) }
            }

            override fun onError(error: Int) {
                // Hataları Türkçe ve anlaşılır bir şekilde yakalıyoruz
                val mesaj = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Ses kayıt hatası! Mikrofonu kontrol et."
                    SpeechRecognizer.ERROR_NETWORK -> "İnternet bağlantısı yok!"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Söylediğini anlayamadım."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Sistem şu an meşgul."
                    SpeechRecognizer.ERROR_SERVER -> "Google Ses Sunucusu hatası."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hiçbir ses duymadım!"
                    else -> "Bilinmeyen bir hata oluştu: $error"
                }
                Toast.makeText(context, "Hata: $mesaj", Toast.LENGTH_LONG).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle?) {
                // İleride garson konuşurken ekranda anlık kelimeleri yazmak istersen burayı kullanacağız
            }
        })
    }

    // Dinlemeyi Başlat
    fun startListening() {
        speechRecognizer.startListening(intent)
    }

    // Dinlemeyi Manuel Olarak Durdur
    fun stopListening() {
        speechRecognizer.stopListening()
    }

    // MÜHENDİSLİK DOKUNUŞU: Bellek sızıntısını önlemek için servisi öldürür
    fun destroy() {
        speechRecognizer.destroy()
    }
}