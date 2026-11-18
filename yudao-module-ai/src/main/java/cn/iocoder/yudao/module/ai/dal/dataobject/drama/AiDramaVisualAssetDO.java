package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;

import java.util.List;

/**
 * AI 短剧视觉资产（道具等） DO
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_visual_asset", autoResultMap = true)
@KeySequence("ai_drama_visual_asset_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaVisualAssetDO extends BaseDO {

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
     * 所属分镜编号：关键帧所属的分镜；道具等未绑定分镜时为空
     * <p>
     * 关联 {@link AiDramaStoryboardDO#getId()}
     */
    private Long storyboardId;
    /**
     * 父资产编号：历史记录所属的当前资产；当前资产为空
     * <p>
     * 关联 {@link AiDramaVisualAssetDO#getId()}
     */
    private Long parentId;
    /**
     * 资产类型
     * <p>
     * 枚举 {@link AiDramaVisualAssetKindEnum}
     */
    private Integer kind;
    /**
     * 资产名称
     */
    private String title;
    /**
     * 出图提示词
     */
    private String prompt;
    /**
     * 出图参考：参考图及判断关键帧是否过期的版本
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private ReferenceImages referenceImages;
    /**
     * 结果图地址（文件服务）
     */
    private String imageUrl;
    /**
     * 生成使用的绘画模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型标识：生成时的快照
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;
    /**
     * 出图输入摘要：对本次出图的输入计算，用于判断当前生成输入是否已变更
     */
    private String sourceHash;
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
     * 失败原因
     */
    private String errorMessage;

    /**
     * 出图参考
     *
     * @author 芋道源码
     */
    @Data
    public static class ReferenceImages {

        /**
         * 道具默认关联的镜头序号：道具规划时确定；分镜显式指定了道具的（{@link AiDramaStoryboardDO#getVisualAssetIds()} 不为空），以分镜为准
         */
        private List<Integer> shotNumbers;
        /**
         * 用户参考图地址：道具使用
         */
        private String referenceImageUrl;
        /**
         * 参考图地址数组：关键帧使用，为本镜角色形象、场景的参考图
         */
        private List<String> urls;
        /**
         * 道具版本：关键帧使用，规划时的道具清单快照；参与 {@link AiDramaVisualAssetDO#getSourceHash()} 计算，用于判断关键帧是否对应当前输入
         */
        private String propsVersion;
        /**
         * 上一版关键帧地址：关键帧修正构图时记录，作为本次生成的参考图
         */
        private String previousImageUrl;

    }

}
