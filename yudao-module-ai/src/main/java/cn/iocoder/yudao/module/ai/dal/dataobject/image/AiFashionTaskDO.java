package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 服装设计流水线主任务 DO
 *
 * <p>一条记录代表一次完整的服装设计任务，
 * 由多个步骤（{@link AiFashionTaskStepDO}）组成。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_task")
@KeySequence("ai_fashion_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionTaskDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 正向提示词
     */
    private String prompt;

    /**
     * 负向提示词
     */
    private String negativePrompt;

    /**
     * 生成宽度（像素）
     */
    private Integer width;

    /**
     * 生成高度（像素）
     */
    private Integer height;

    /**
     * 随机种子，-1 表示随机
     */
    private Long seed;

    /**
     * 质量预设（HIGH / MEDIUM / LOW）
     */
    private String qualityPreset;

    /**
     * SDXL checkpoint 名称（可覆盖服务器默认值）
     */
    private String modelCheckpoint;

    /**
     * ControlNet 姿势参考图片地址（可选）
     */
    private String poseImageUrl;

    /**
     * 面料参考图片地址（可选）
     */
    private String fabricRefUrl;

    /**
     * 面料转换强度（0~1），默认 0.70
     */
    private BigDecimal fabricStrength;

    /**
     * 是否执行超分辨率
     */
    private Boolean upscale;

    /**
     * 超分倍数，默认 2
     */
    private Integer upscaleFactor;

    /**
     * 超分模型名称
     */
    private String upscalerName;

    /**
     * 任务状态
     *
     * 枚举 {@link AiFashionTaskStatusEnum}
     */
    private Integer status;

    /**
     * 链路追踪 ID（写入 MDC 用于日志关联）
     */
    private String traceId;

    /**
     * 最终产出图片地址
     */
    private String finalPicUrl;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 完成/失败时间
     */
    private LocalDateTime finishTime;

}
