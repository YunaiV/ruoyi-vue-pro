package cn.iocoder.yudao.module.ai.service.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 服装 3D 网格生成工具类
 *
 * <p>从深度图生成三角网格，并提供简单的深度估计算法。</p>
 *
 * <p>纯工具类，无 Spring 依赖。</p>
 *
 * @author deepay
 */
public final class AiFashionMeshGenerator {

    private AiFashionMeshGenerator() {}

    /**
     * 从深度图生成 3D 网格
     *
     * @param depthImage     灰度深度图（越亮=越近）
     * @param gridResolution 网格精度（每个维度的采样点数）
     * @return 生成的网格数据
     */
    public static AiFashionMeshData generateFromDepthMap(BufferedImage depthImage, int gridResolution) {
        int gridW = gridResolution;
        int gridH = gridResolution;
        int srcW = depthImage.getWidth();
        int srcH = depthImage.getHeight();

        List<double[]> vertices  = new ArrayList<>(gridW * gridH);
        List<double[]> texCoords = new ArrayList<>(gridW * gridH);
        List<double[]> normals   = new ArrayList<>(gridW * gridH);
        List<int[]>    faces     = new ArrayList<>((gridW - 1) * (gridH - 1) * 2);

        // 采样深度图，构建顶点
        double[][] depthGrid = new double[gridH][gridW];
        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                int px = (int) Math.round((double) gx / (gridW - 1) * (srcW - 1));
                int py = (int) Math.round((double) gy / (gridH - 1) * (srcH - 1));
                px = Math.min(px, srcW - 1);
                py = Math.min(py, srcH - 1);

                int rgb   = depthImage.getRGB(px, py);
                int gray  = (rgb >> 16) & 0xFF; // R channel of grayscale
                double zNorm = (gray / 255.0) * 0.5;
                depthGrid[gy][gx] = zNorm;

                double xNorm = (double) gx / (gridW - 1) * 2.0 - 1.0;
                double yNorm = (double) gy / (gridH - 1) * 2.0 - 1.0;

                vertices.add(new double[]{xNorm, -yNorm, zNorm});
                texCoords.add(new double[]{(double) gx / (gridW - 1), (double) gy / (gridH - 1)});
                normals.add(new double[]{0, 0, 1}); // placeholder, updated below
            }
        }

        // 计算每个内部顶点的法线
        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                int idx = gy * gridW + gx;
                double dzDx, dzDy;
                if (gx > 0 && gx < gridW - 1) {
                    dzDx = (depthGrid[gy][gx + 1] - depthGrid[gy][gx - 1]) / 2.0;
                } else if (gx == 0) {
                    dzDx = depthGrid[gy][gx + 1] - depthGrid[gy][gx];
                } else {
                    dzDx = depthGrid[gy][gx] - depthGrid[gy][gx - 1];
                }
                if (gy > 0 && gy < gridH - 1) {
                    dzDy = (depthGrid[gy + 1][gx] - depthGrid[gy - 1][gx]) / 2.0;
                } else if (gy == 0) {
                    dzDy = depthGrid[gy + 1][gx] - depthGrid[gy][gx];
                } else {
                    dzDy = depthGrid[gy][gx] - depthGrid[gy - 1][gx];
                }
                double nx = -dzDx;
                double ny = -dzDy;
                double nz = 1.0;
                double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len > 0) { nx /= len; ny /= len; nz /= len; }
                normals.set(idx, new double[]{nx, ny, nz});
            }
        }

        // 构建三角面
        for (int gy = 0; gy < gridH - 1; gy++) {
            for (int gx = 0; gx < gridW - 1; gx++) {
                int v0 = gy * gridW + gx;
                int v1 = gy * gridW + (gx + 1);
                int v2 = (gy + 1) * gridW + gx;
                int v3 = (gy + 1) * gridW + (gx + 1);
                faces.add(new int[]{v0, v1, v2});
                faces.add(new int[]{v1, v3, v2});
            }
        }

        return new AiFashionMeshData(vertices, faces, texCoords, normals);
    }

    /**
     * 简单深度估计：将 2D 图像转换为深度图
     *
     * <p>策略：图像中心 = 浅（亮），边缘 = 深（暗）；与反转灰度混合。</p>
     *
     * @param src 原始 ARGB/RGB 图像
     * @return 灰度深度图
     */
    public static BufferedImage estimateDepthSimple(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;
        double maxDist = Math.sqrt(cx * cx + cy * cy);

        BufferedImage depth = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = depth.createGraphics();
        g2d.dispose();

        int[] rgbArray = src.getRGB(0, 0, w, h, null, 0, w);
        int[] depthArray = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                int rgb = rgbArray[idx];
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // 反转灰度
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                int invertedGray = 255 - gray;

                // 中心距离因子（中心=浅=255，边缘=深=0）
                double dx = x - cx;
                double dy = y - cy;
                double dist = Math.sqrt(dx * dx + dy * dy);
                int centerFactor = (int)((1.0 - dist / maxDist) * 255);

                // 混合：60% 中心因子 + 40% 反转灰度
                int depthVal = (int)(centerFactor * 0.6 + invertedGray * 0.4);
                depthVal = Math.max(0, Math.min(255, depthVal));
                depthArray[idx] = (0xFF << 24) | (depthVal << 16) | (depthVal << 8) | depthVal;
            }
        }

        // 写入深度图
        BufferedImage depthArgb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        depthArgb.setRGB(0, 0, w, h, depthArray, 0, w);

        // 3x3 均值模糊
        return blur3x3(depthArgb);
    }

    /** 3x3 均值模糊 */
    private static BufferedImage blur3x3(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] input = src.getRGB(0, 0, w, h, null, 0, w);
        int[] output = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rSum = 0, count = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int rgb = input[ny * w + nx];
                            rSum += (rgb >> 16) & 0xFF;
                            count++;
                        }
                    }
                }
                int avg = rSum / count;
                output[y * w + x] = (0xFF << 24) | (avg << 16) | (avg << 8) | avg;
            }
        }
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, output, 0, w);
        return result;
    }

}
