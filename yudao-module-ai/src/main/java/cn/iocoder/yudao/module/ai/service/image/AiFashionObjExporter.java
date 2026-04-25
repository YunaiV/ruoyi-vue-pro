package cn.iocoder.yudao.module.ai.service.image;

import java.util.List;

/**
 * AI 服装 OBJ/MTL 导出工具类
 *
 * <p>将 {@link AiFashionMeshData} 导出为标准 Wavefront OBJ / MTL 格式字符串。</p>
 *
 * <p>纯工具类，无 Spring 依赖。</p>
 *
 * @author deepay
 */
public final class AiFashionObjExporter {

    private AiFashionObjExporter() {}

    /**
     * 导出 OBJ 格式字符串
     *
     * @param mesh        网格数据
     * @param mtlFileName MTL 文件名（如 "fashion.mtl"），为 null 时不写 mtllib 行
     * @return OBJ 格式字符串
     */
    public static String exportObj(AiFashionMeshData mesh, String mtlFileName) {
        List<double[]> vertices  = mesh.getVertices();
        List<double[]> texCoords = mesh.getTexCoords();
        List<double[]> normals   = mesh.getNormals();
        List<int[]>    faces     = mesh.getFaces();

        StringBuilder sb = new StringBuilder();
        sb.append("# AI Fashion 3D Export\n");
        sb.append("# Vertices: ").append(vertices.size()).append("\n");
        sb.append("# Faces: ").append(faces.size()).append("\n");

        if (mtlFileName != null) {
            sb.append("mtllib ").append(mtlFileName).append("\n");
        }
        sb.append("usemtl fashion_material\n\n");

        // 顶点
        for (double[] v : vertices) {
            sb.append(String.format("v %.6f %.6f %.6f\n", v[0], v[1], v[2]));
        }
        sb.append("\n");

        // 纹理坐标
        for (double[] vt : texCoords) {
            sb.append(String.format("vt %.6f %.6f\n", vt[0], vt[1]));
        }
        sb.append("\n");

        // 法线
        for (double[] vn : normals) {
            sb.append(String.format("vn %.6f %.6f %.6f\n", vn[0], vn[1], vn[2]));
        }
        sb.append("\n");

        // 面（1-indexed）
        for (int[] f : faces) {
            int v0 = f[0] + 1;
            int v1 = f[1] + 1;
            int v2 = f[2] + 1;
            sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n",
                    v0, v0, v0,
                    v1, v1, v1,
                    v2, v2, v2));
        }

        return sb.toString();
    }

    /**
     * 导出 MTL 格式字符串
     *
     * @param textureFileName 纹理图片文件名（如 "texture.png"）
     * @return MTL 格式字符串
     */
    public static String exportMtl(String textureFileName) {
        return "# AI Fashion Material\n"
                + "newmtl fashion_material\n"
                + "Ka 1.000 1.000 1.000\n"
                + "Kd 1.000 1.000 1.000\n"
                + "Ks 0.500 0.500 0.500\n"
                + "Ns 32.000\n"
                + "d 1.000\n"
                + "illum 2\n"
                + "map_Kd " + textureFileName + "\n";
    }

}
