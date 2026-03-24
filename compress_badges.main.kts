#!/usr/bin/env kotlin

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val drawableDir = File("app/src/main/res/drawable")
    val searchDir = if (drawableDir.exists()) drawableDir else File("src/main/res/drawable")
    
    if (!searchDir.exists()) {
        println("Error: Drawable directory not found! Checked: ${searchDir.absolutePath}")
        return
    }

    println("Scanning for badges in: ${searchDir.absolutePath}")
    val badges = searchDir.listFiles { _, name -> 
        name.startsWith("object_illustration_") && !name.endsWith("_small.png") && name.endsWith(".png")
    }

    if (badges.isNullOrEmpty()) {
        println("No badges found to optimize.")
        return
    }

    badges.forEach { file ->
        try {
            val img = ImageIO.read(file) ?: return@forEach

            // Create a memory-efficient 128x128 thumbnail
            val small = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
            val g = small.createGraphics()
            
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            
            g.drawImage(img, 0, 0, 128, 128, null)
            g.dispose()

            val targetName = file.nameWithoutExtension + "_small.png"
            val target = File(searchDir, targetName)
            ImageIO.write(small, "png", target)
            
            println("SUCCESS: Optimized ${file.name} -> $targetName")
        } catch (e: Exception) {
            println("FAILED to optimize ${file.name}: ${e.message}")
        }
    }
    println("Optimization complete! ✨")
}

main()
