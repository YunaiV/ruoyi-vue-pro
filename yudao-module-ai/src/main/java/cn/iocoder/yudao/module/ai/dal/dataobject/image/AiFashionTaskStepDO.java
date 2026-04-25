package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * AI 服装设计流水线任务步骤 DO
 *
 * <p>每条记录代表一个流水线步骤（SDXL / POSE / FABRIC / UPSCALE / THREE_D），
 * 属于某个 {@link AiFashionTaskDO}。</p>
 *
 * @author deepay
 */
@TableName(value = "ai_fashion_task_step", autoResultMap = true)
@KeySequence("ai_fashion_task_step_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionTaskStepDO extends BaseDO {

    /**
     * 步骤编号
     */
    @TableId
    private Long id;

    /**
     * 所属任务编号
     *
     * 关联 {@link AiFashionTaskDO#getId()}
     */
    private Long taskId;

    /**
     * 步骤顺序（0-based）
     */
    private Integer stepOrder;

    /**
     * 步骤类型
     *
     * 枚举 {@link AiFashionTaskStepTypeEnum}
     */
    private String stepType;

    /**
     * 步骤状态
     *
     * 枚举 {@link AiFashionTaskStepStatusEnum}
     */
    private Integer status;

    /**
     * 输入图片地址（上一步产物或原始参考图）
     */
    private String inputPicUrl;

    /**
     * 输出图片地址（本步骤产物）
     */
    private String outputPicUrl;

    /**
     * 输入参数（JSON），记录提交给模型的完整请求参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> inputOptions;

    /**
     * 输出元信息（JSON），记录模型返回的 seed、model hash 等
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> outputOptions;

    /**
     * 本步骤使用的模型名称
     */
    private String modelName;

    /**
     * 本步骤耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 失败原因
     */
    private String errorMessage;

}
