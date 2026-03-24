package com.dicoding.sentinel.util

import com.dicoding.sentinel.R

object GamificationUtils {

    data class Level(
        val name: String,
        val thresholdDays: Int,
        val badgeResId: Int
    )

    val levels = listOf(
        Level("Legend", 365, R.drawable.object_illustration_01),
        Level("Champion", 240, R.drawable.object_illustration_02),
        Level("Diamond Will", 180, R.drawable.object_illustration_03),
        Level("Platinum Will", 150, R.drawable.object_illustration_04),
        Level("Gold Will", 120, R.drawable.object_illustration_05),
        Level("Silver Will", 90, R.drawable.object_illustration_06),
        Level("Bronze Will", 60, R.drawable.object_illustration_07),
        Level("Iron Will", 45, R.drawable.object_illustration_08),
        Level("Master Sentinel", 30, R.drawable.object_illustration_09),
        Level("Elite Sentinel", 21, R.drawable.object_illustration_10),
        Level("Sentinel", 15, R.drawable.object_illustration_11),
        Level("Guardian", 10, R.drawable.object_illustration_12),
        Level("Sentry", 7, R.drawable.object_illustration_13),
        Level("Apprentice", 3, R.drawable.object_illustration_14),
        Level("Initiate", 1, R.drawable.object_illustration_15),
        Level("Neophyte", 0, R.drawable.object_illustration_16)
    ).sortedByDescending { it.thresholdDays }

    fun getLevel(days: Long): Level {
        return levels.find { days >= it.thresholdDays } ?: levels.last()
    }

    fun getNextLevel(days: Long): Level? {
        val currentLevelIndex = levels.indexOf(getLevel(days))
        return if (currentLevelIndex > 0) levels[currentLevelIndex - 1] else null
    }

    fun decodeSampledBitmapFromResource(
        res: android.content.res.Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): android.graphics.Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return android.graphics.BitmapFactory.decodeResource(res, resId, options)
    }

    private fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // Fungsi Kotlin untuk mengompres dan menyimpan gambar secara background
    suspend fun compressAndCacheBadges(context: android.content.Context) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cacheDir = java.io.File(context.cacheDir, "compressed_badges")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        levels.forEach { level ->
            val targetFile = java.io.File(cacheDir, "badge_${level.badgeResId}.png")
            // Jika gambar belum dikompres, kita kompres sekarang
            if (!targetFile.exists()) {
                try {
                    val bitmap = decodeSampledBitmapFromResource(
                        context.resources,
                        level.badgeResId,
                        128,
                        128
                    )
                    var out: java.io.FileOutputStream? = null
                    try {
                        out = java.io.FileOutputStream(targetFile)
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    } finally {
                        out?.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Mengambil file gambar yang sudah dikompres
    fun getCompressedBadgeFile(context: android.content.Context, badgeResId: Int): java.io.File? {
        val targetFile = java.io.File(context.cacheDir, "compressed_badges/badge_${badgeResId}.png")
        return if (targetFile.exists()) targetFile else null
    }
}