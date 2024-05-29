package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * midjourney req
 *
 * @author fansili
 * @time 2024/4/28 17:42
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiImageListRespVO extends PageParam {

    @Schema(description = "id编号", example = "1")
    private Long id;

    @Schema(description = "用户id", example = "1")
    private Long userId;

    @Schema(description = "提示词", example = "南极的小企鹅")
    private String prompt;

    @Schema(description = "平台", example = "openai")
    private String platform;

    @Schema(description = "模型", example = "dall2")
    private String model;

    @Schema(description = "图片宽度", example = "1024")
    private String width;

    @Schema(description = "图片高度", example = "1024")
    private String height;

    @Schema(description = "绘画状态：10 进行中、20 绘画完成、30 绘画失败", example = "10")
    private String status;

    @Schema(description = "是否发布", example = "public")
    private String publicStatus;

    @Schema(description = "图片地址(自己服务器)", example = "http://")
    private String picUrl;

    @Schema(description = "绘画图片地址(绘画好的服务器)", example = "http://")
    private String originalPicUrl;

    @Schema(description = "绘画错误信息", example = "图片错误信息")
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
