package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;

import java.util.List;

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
     * 是否有背景人群
     */
    private Boolean crowd;
    /**
     * 背景人群描述
     */
    private String crowdDescription;
    /**
     * 可站位置数组：分镜中角色的站位从中选取
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> availableSlots;
    /**
     * 出图描述数组：多条差异化的场景出图描述
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> descriptions;
    /**
     * 用户参考图
     */
    private String referenceImageUrl;
    /**
     * 候选图地址数组（文件服务）
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> imageUrls;
    /**
     * 候选图描述数组：与 {@link #imageUrls} 按下标一一对应，为生成该图时的提示词
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> imageDescriptions;
    /**
     * 选用的候选下标
     */
    private Integer selectedIndex;
    /**
     * 上一轮候选图地址数组：重新生成后保留，用于撤销
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> previousImageUrls;
    /**
     * 上一轮候选图描述数组：与 {@link #previousImageUrls} 按下标一一对应
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> previousDescriptions;
    /**
     * 最近一次出图使用的绘画模型编号：供重试与展示
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
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
