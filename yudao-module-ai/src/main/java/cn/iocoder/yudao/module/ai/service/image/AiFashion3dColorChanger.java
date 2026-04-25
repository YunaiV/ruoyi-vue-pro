package cn.iocoder.yudao.module.ai.service.image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * AI 服装 3D 颜色变换工具类
 *
 * <p>纯 Java AWT 实现，无 OpenCV 依赖。</p>
 *
 * @author deepay
 */
public final class AiFashion3dColorChanger {

    private AiFashion3dColorChanger() {}

    /** 简单调色板（用于主色检测） */
    private static final String[] PALETTE_NAMES = {
            "red", "blue", "green", "yellow", "purple",
            "orange", "pink", "black", "white", "gray"
    };
    private static final int[] PALETTE_RGB = {
            0xFF0000, 0x0000FF, 0x00AA00, 0xFFFF00, 0x800080,
            0xFF6600, 0xFF69B4, 0x111111, 0xEEEEEE, 0x808080
    };

    /**
     * 变换图像颜色（HSB 色调偏移）
     *
     * @param src            原始图像
     * @param targetColorHex 目标颜色 Hex（如 "#FF0000"）
     * @return 颜色变换后的图像
     */
    public static BufferedImage changeColor(BufferedImage src, String targetColorHex) {
        Color target;
        try {
            target = Color.decode(targetColorHex);
        } catch (NumberFormatException e) {
            return src;
        }

        float[] targetHsb = Color.RGBtoHSB(target.getRed(), target.getGreen(), target.getBlue(), null);
        float targetHue = targetHsb[0];
        float targetSat = targetHsb[1];

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb  = src.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int r     = (argb >> 16) & 0xFF;
                int g     = (argb >> 8)  & 0xFF;
                int b     = argb & 0xFF;

                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                float brightness = hsb[2];

                // 跳过极暗（黑色）和极亮（白色/高光）区域
                if (brightness < 0.05f || brightness > 0.95f) {
                    result.setRGB(x, y, argb);
                    continue;
                }

                // 色调→目标色调，饱和度 30% 原始 + 70% 目标，亮度保持
                float newHue = targetHue;
                float newSat = hsb[1] * 0.3f + targetSat * 0.7f;
                float newBri = brightness;

                int newRgb = Color.HSBtoRGB(newHue, newSat, newBri);
                int newArgb = (alpha << 24) | (newRgb & 0x00FFFFFF);
                result.setRGB(x, y, newArgb);
            }
        }
        return result;
    }

    /**
     * 检测图像主色
     *
     * @param image 输入图像
     * @return 颜色名称（英文）
     */
    public static String detectDominantColor(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] counts = new int[PALETTE_NAMES.length];

        for (int y = 0; y < h; y += 4) {
            for (int x = 0; x < w; x += 4) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b = rgb & 0xFF;

                int nearest = 0;
                int minDist = Integer.MAX_VALUE;
                for (int i = 0; i < PALETTE_RGB.length; i++) {
                    int pr = (PALETTE_RGB[i] >> 16) & 0xFF;
                    int pg = (PALETTE_RGB[i] >> 8)  & 0xFF;
                    int pb = PALETTE_RGB[i] & 0xFF;
                    int dist = (r - pr) * (r - pr) + (g - pg) * (g - pg) + (b - pb) * (b - pb);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = i;
                    }
                }
                counts[nearest]++;
            }
        }

        int maxIdx = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[maxIdx]) maxIdx = i;
        }
        return PALETTE_NAMES[maxIdx];
    }

}
