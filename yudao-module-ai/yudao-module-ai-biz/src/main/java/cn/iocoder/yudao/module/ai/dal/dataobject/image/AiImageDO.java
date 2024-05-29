package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * AI 绘画 DO
 *
 * @author fansili
 */
@TableName("ai_image")
@Data
public class AiImageDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "图片宽度")
    private String width;

    @Schema(description = "图片高度")
    private String height;

    @Schema(description = "绘画状态：提交、排队、绘画中、绘画完成、绘画失败")
    private String status;

    @Schema(description = "是否发布")
    private String publicStatus;

    @Schema(description = "图片地址(自己服务器)")
    private String picUrl;

    @Schema(description = "绘画图片地址(绘画好的服务器)")
    private String originalPicUrl;

    @Schema(description = "绘画错误信息")
    private String errorMessage;

    // ============ 绘画请求参数

    /**
     * - style
     */
    @Schema(description = "绘画请求参数")
    private Map<String, Object> drawRequest;

    /**
     * - mjNonceId
     * - mjOperationId
     * - mjOperationName
     * - mjOperations
     */
    @Schema(description = "绘画请求响应参数")
    private Map<String, Object> drawResponse;

}

