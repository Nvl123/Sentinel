package com.dicoding.sentinel.util;

import org.junit.Test;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility to batch-compress badge images into small thumbnails.
 * To run: Click the green play icon next to the class name or method below.
 */
public class ImageOptimizer {
    @Test
    public void compressBadges() {
        // Path to your drawable directory
        File drawableDir = new File("app/src/main/res/drawable");
        if (!drawableDir.exists()) {
            drawableDir = new File("src/main/res/drawable");
        }
        
        if (!drawableDir.exists()) {
            System.err.println("Drawable directory not found! Checked: " + drawableDir.getAbsolutePath());
            return;
        }

        System.out.println("Scanning badges in: " + drawableDir.getAbsolutePath());
        File[] badges = drawableDir.listFiles((dir, name) -> 
            name.startsWith("object_illustration_") && !name.endsWith("_small.png") && name.endsWith(".png")
        );

        if (badges == null || badges.length == 0) {
            System.out.println("No badges found to optimize.");
            return;
        }

        for (File file : badges) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) continue;

                // Create a 128x128 thumbnail
                BufferedImage small = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = small.createGraphics();
                
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g.drawImage(img, 0, 0, 128, 128, null);
                g.dispose();

                String targetName = file.getName().replace(".png", "_small.png");
                File target = new File(drawableDir, targetName);
                ImageIO.write(small, "png", target);
                
                System.out.println("SUCCESS: Optimized " + file.getName() + " -> " + targetName);
            } catch (IOException e) {
                System.err.println("FAILED to optimize " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
