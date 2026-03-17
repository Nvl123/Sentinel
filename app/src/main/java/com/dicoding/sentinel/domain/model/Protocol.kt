package com.dicoding.sentinel.domain.model

sealed class Protocol(
    val id: Int,
    val name: String,
    val instruction: String,
    val durationSeconds: Int = 0,
    val type: ProtocolType
) {
    object Physical : Protocol(
        1, "Physical Interruption", "Lakukan push-up selama 60 detik untuk membakar energi negatif.", 60, ProtocolType.TIMER
    )
    object Grayscale : Protocol(
        2, "Grayscale Mode", "Ubah layar HP menjadi hitam-putih untuk mengurangi stimulasi visual.", 0, ProtocolType.ACTION
    )
    object Grounding : Protocol(
        3, "Grounding 5-4-3-2-1", "Sebutkan 5 benda yang kamu lihat, 4 suara, 3 sentuhan, 2 aroma, dan 1 rasa.", 0, ProtocolType.INTERACTIVE
    )
    object Migration : Protocol(
        4, "Migration", "Pindah ke ruangan lain atau keluar rumah sekarang juga.", 0, ProtocolType.CHECKLIST
    )
    object ColdReset : Protocol(
        5, "Cold Reset", "Mandi air dingin selama 3 menit untuk meredam sistem saraf.", 180, ProtocolType.TIMER
    )
    object Visualization : Protocol(
        6, "Future Visualization", "Bayangkan penyesalanmu 10 menit dari sekarang jika kamu menyerah.", 0, ProtocolType.INTERACTIVE
    )
    object SocialBoundary : Protocol(
        7, "Social Boundary", "Kirim pesan ke teman/pasangan untuk mengalihkan pikiran.", 0, ProtocolType.ACTION
    )

    companion object {
        val library = listOf(Physical, Grayscale, Grounding, Migration, ColdReset, Visualization, SocialBoundary)
    }
}

enum class ProtocolType {
    TIMER, INSTRUCTION, INTERACTIVE, CHECKLIST, ACTION
}
