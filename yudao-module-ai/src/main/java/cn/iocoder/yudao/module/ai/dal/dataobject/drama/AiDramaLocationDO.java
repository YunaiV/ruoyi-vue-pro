package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.util.List;

// DONE @AI：场景使用 location 是合理的么？先讨论哈；（评估后保留 location：指拍摄地点，避免与分镜的场次 sceneNo 混淆；表名、类名、字段均不变）
/**
 * AI 短剧场景 DO
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_location", autoResultMap = true)
@KeySequence("ai_drama_location_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaLocationDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     * <p>
     * 关联 {@link AiDramaProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 场景名称
     */
    private String name;
    /**
     * 场景设定
     */
    private String description;
    /**
     * 场景出图提示词
     */
    private String prompt;
    /**
     * 候选图，结构同 {@link AiDramaCharacterAppearanceDO.Candidate}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AiDramaCharacterAppearanceDO.Candidate> candidates;
    /**
     * 选用的候选下标
     */
    private Integer selectedIndex;
    /**
     * 用户参考图
     */
    private String referenceImageUrl;
    /**
     * 出图任务编号：每次提交出图生成一个；异步出图的最终回写以它为条件，被标记失败后又重新提交时，旧的执行结果不再写入
     */
    private String taskId;
    /**
     * 最近一次生成的状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}，为空表示未生成
     */
    private Integer status;
    /**
     * 生成失败原因
     */
    private String errorMessage;

}
