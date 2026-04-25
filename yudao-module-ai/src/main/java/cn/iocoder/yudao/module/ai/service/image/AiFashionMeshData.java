package cn.iocoder.yudao.module.ai.service.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 服装 3D 网格数据值对象
 *
 * <p>包含顶点、面片、纹理坐标和法线数据，用于 OBJ 导出。</p>
 *
 * @author deepay
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiFashionMeshData {

    /** 顶点列表，每个元素为 [x, y, z] */
    private List<double[]> vertices;

    /** 面片索引列表（三角形），每个元素为 [v0, v1, v2]（0-based） */
    private List<int[]> faces;

    /** UV 纹理坐标列表，每个元素为 [u, v] */
    private List<double[]> texCoords;

    /** 法线列表，每个元素为 [nx, ny, nz] */
    private List<double[]> normals;

}
