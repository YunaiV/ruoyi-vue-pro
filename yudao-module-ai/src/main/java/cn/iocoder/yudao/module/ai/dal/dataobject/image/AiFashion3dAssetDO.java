package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AI 服装 3D 资产 DO
 *
 * <p>存储 2D → 3D 转换的产出物信息。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_3d_asset")
@KeySequence("ai_fashion_3d_asset_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashion3dAssetDO extends BaseDO {

    /** 资产编号 */
    @TableId
    private Long id;

    /** 用户编号 */
    private Long userId;

    /** 来源设计任务ID（可选） */
    private Long taskId;

    /** 来源 2D 图片地址 */
    private String sourceImageUrl;

    /** 生成的深度图地址 */
    private String depthMapUrl;

    /** OBJ 网格文件地址 */
    private String objFileUrl;

    /** MTL 材质文件地址 */
    private String mtlFileUrl;

    /** 纹理图片地址 */
    private String textureUrl;

    /** 多角度预览图 JSON（{"angle_0":"url", "angle_45":"url", ...}） */
    private String previewUrlsJson;

    /** 旋转动画/合成图地址 */
    private String rotationGifUrl;

    /** 网格顶点数 */
    private Integer verticesCount;

    /** 网格面片数 */
    private Integer facesCount;

    /** 网格精度（每个维度的采样点数） */
    private Integer gridResolution;

    /** 颜色变换 Hex（如 #FF0000） */
    private String colorChange;

    /** 输出格式（OBJ/GLTF/STL） */
    private String outputFormat;

    /** 处理状态（PROCESSING/SUCCESS/FAIL） */
    private String status;

    /** 失败原因 */
    private String errorMessage;

    /** 处理总耗时（毫秒） */
    private Long durationMs;

}
