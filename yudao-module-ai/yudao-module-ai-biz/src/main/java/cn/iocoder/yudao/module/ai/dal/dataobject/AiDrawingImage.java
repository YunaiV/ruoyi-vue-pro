package cn.iocoder.yudao.module.ai.dal.dataobject;

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
@TableName("ai_drawing_image")
public class AiDrawingImage extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "绘画状态：提交、排队、绘画中、绘画完成、绘画失败")
    private String drawingStatus;

    @Schema(description = "绘画图片地址")
    private String drawingImageUrl;

}

