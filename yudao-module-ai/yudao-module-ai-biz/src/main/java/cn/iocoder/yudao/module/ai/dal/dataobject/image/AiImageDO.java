package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 绘画 DO
 *
 * @author fansili
 */
@TableName(value = "ai_image", autoResultMap = true)
@Data
public class AiImageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 平台
     *
     * 枚举 {@link cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型
     *
     * 冗余 {@link AiChatModelDO#getModel()}
     */
    private String model;

    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 生成状态
     *
     * 枚举 {@link AiImageStatusEnum}
     */
    private Integer status;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 绘画错误信息
     */
    private String errorMessage;

    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 是否公开
     */
    private Boolean publicStatus;

    /**
     * 绘制参数，不同 platform 的不同参数
     *
     * 1. {@link OpenAiImageOptions}
     * 2. {@link StabilityAiImageOptions}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> options;

    /**
     * mj buttons 按钮
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MidjourneyApi.Button> buttons;

    /**
     * 任务编号
     *
     * 1. midjourney proxy：关联的 task id
     */
    private String taskId;

}

