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

    // TODO @fan：要加一个平台字段；platform；因为一个平台，会有多种 model 模型；

    @Schema(description = "模型 dall2/dall3、MJ、NIJI")
    private String model;

    @Schema(description = "生成图像的尺寸大小。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。")
    private String size;

    @Schema(description = "风格")
    private String style;

    @Schema(description = "图片地址(自己服务器)")
    private String picUrl;

    @Schema(description = "绘画状态：提交、排队、绘画中、绘画完成、绘画失败")
    private String status;

    @Schema(description = "绘画图片地址(绘画好的服务器)")
    private String originalPicUrl;

    @Schema(description = "绘画错误信息")
    private String errorMessage;

    @Schema(description = "是否发布")
    private String publicStatus;

    // ============ mj 需要字段

    @Schema(description = "用户操作的Nonce编号(MJ返回)")
    private String mjNonceId;

    @Schema(description = "用户操作的操作编号(MJ返回)")
    private String mjOperationId;

    @Schema(description = "用户操作的操作名字(MJ返回)")
    private String mjOperationName;

    @Schema(description = "mj图片生产成功保存的 components json 数组")
    private String mjOperations;

}

