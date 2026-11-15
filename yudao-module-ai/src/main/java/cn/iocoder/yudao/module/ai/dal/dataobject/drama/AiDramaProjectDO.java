package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStepEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;

/**
 * AI 短剧项目 DO
 *
 * 只保留最近一次成片的结果
 *
 * @author 芋道源码
 */
@TableName("ai_drama_project")
@KeySequence("ai_drama_project_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaProjectDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 故事想法
     */
    private String idea;
    // DONE @AI：是有枚举么？还是？？？（没有枚举：画风是自由文本，前端输入框给出示例，也参与广场关键字搜索）
    /**
     * 画风 / 视觉风格
     */
    private String style;
    /**
     * 画幅，默认 9:16：项目的设置，用于生成镜头视频；成片实际使用的见 {@link #composeAspectRatio}
     */
    private String aspectRatio;
    /**
     * 转场类型
     * <p>
     * 枚举 {@link AiDramaTransitionTypeEnum}
     */
    private Integer composeTransitionType;
    /**
     * 转场时长（秒）
     */
    private Double composeTransitionDurationSeconds;
    // DONE @AI：我记得竞品没这个字段？所以我们去掉了？是为什么加回来么？（保留：工作台按它恢复当前步骤，路由独立后刷新、重新进入都要用；项目列表、管理后台也按它展示、筛选）
    /**
     * 当前步骤
     * <p>
     * 枚举 {@link AiDramaProjectStepEnum}
     */
    private Integer status;
    /**
     * 是否公开：只有成片成功后才能公开
     */
    private Boolean publicStatus;
    /**
     * 最近一次成片的状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}，为空表示未合成
     */
    private Integer composeStatus;
    /**
     * 合成任务编号：每次提交合成生成一个；异步合成的最终回写以它为条件，被标记失败后又重新合成时，旧的执行结果不再写入
     */
    private String composeTaskId;
    /**
     * 成片地址（文件服务）
     */
    private String composeFileUrl;
    /**
     * 成片时长（秒）
     */
    private Double composeDuration;
    /**
     * 成片画幅：成片实际使用的画幅，合成时可另选，可能与项目的画幅 {@link #aspectRatio} 不同
     */
    private String composeAspectRatio;
    /**
     * 成片镜头数：成片实际拼接的镜头数，合成时可只选部分镜头
     */
    private Integer composeStoryboardCount;
    /**
     * 合成失败原因
     */
    private String composeErrorMessage;

}
