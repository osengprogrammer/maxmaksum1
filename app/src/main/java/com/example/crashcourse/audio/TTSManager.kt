package com.example.crashcourse.audio

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSManager(context: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) {
            tts?.language = Locale.US
            tts?.setSpeechRate(1.0f)
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

    fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
