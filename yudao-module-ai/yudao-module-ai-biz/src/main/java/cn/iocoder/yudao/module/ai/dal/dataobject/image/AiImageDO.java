package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ai 绘画
 *
 * @author fansili
 * @time 2024/4/25 15:53
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("ai_image")
public class AiImageDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型")
    private String modal;

    @Schema(description = "生成图像的尺寸大小。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。")
    private String size;

    @Schema(description = "绘画状态：提交、排队、绘画中、绘画完成、绘画失败")
    private String drawingStatus;

    @Schema(description = "绘画图片地址")
    private String drawingImageUrl;

    @Schema(description = "绘画错误信息")
    private String drawingError;

}

