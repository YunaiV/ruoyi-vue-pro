package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AI 服装设计智能体链路任务 DO
 *
 * <p>一条链路（chainId 相同）包含多条有序的 step 记录，
 * 每条记录对应链路中的一个自动化步骤。</p>
 *
 * <p>链路示例：</p>
 * <pre>
 * 用户输入：帮我设计5款甜酷风红色连衣裙然后转3D旋转90度
 * 链路记录：
 *   step 0 – BATCH_GENERATE  status=SUCCESS  resultTaskIds=[101,102,103,104,105]
 *   step 1 – GENERATE_3D     status=SUCCESS  resultRef=assetId=201
 *   step 2 – ROTATE          status=SUCCESS  resultRef=rotationGifUrl=...
 * </pre>
 *
 * @author deepay
 */
@TableName("ai_fashion_agent_task")
@KeySequence("ai_fashion_agent_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionAgentTaskDO extends BaseDO {

    @TableId
    private Long id;

    /** 链路编号（同一句话产生的所有步骤共享同一 chainId） */
    private String chainId;

    /** 用户编号 */
    private Long userId;

    /** 步骤顺序（0-based） */
    private Integer stepOrder;

    /** 意图类型（对应 AiFashionIntentEnum.code） */
    private String intent;

    /** 步骤自然语言描述，如"批量生成5款甜酷风红色连衣裙" */
    private String intentDesc;

    /** 本步骤使用的 SD Prompt */
    private String prompt;

    /** 任务状态：PENDING / RUNNING / SUCCESS / FAIL / SKIPPED */
    private String status;

    /** 进度百分比 0-100 */
    private Integer progress;

    /**
     * 本步骤产生的主要结果引用（JSON）：
     * <ul>
     *   <li>BATCH_GENERATE → {@code {"taskIds":[1,2,3]}}</li>
     *   <li>GENERATE_3D    → {@code {"assetId":201}}</li>
     *   <li>ROTATE         → {@code {"gifUrl":"...","previewUrls":{...}}}</li>
     *   <li>MODIFY_COLOR   → {@code {"taskId":101,"picUrl":"..."}}</li>
     * </ul>
     */
    private String resultJson;

    /** 用于传给下一步的引用（如设计任务ID → 3D转换输入） */
    private String nextStepInputJson;

    /** 错误信息 */
    private String errorMessage;

    /** 本步骤耗时（毫秒） */
    private Long durationMs;

    /** 是否已向前端推送完成通知 */
    private Boolean notified;

}
