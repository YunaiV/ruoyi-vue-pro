package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AI 服装设计会话 DO
 *
 * <p>持久化用户的会话上下文，用于支持多轮对话式设计交互。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_session")
@KeySequence("ai_fashion_session_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionSessionDO extends BaseDO {

    /** 会话编号 */
    @TableId
    private Long id;

    /** 用户编号 */
    private Long userId;

    /** 会话令牌（UUID，由前端存储并传入） */
    private String sessionToken;

    /** 最近一次任务ID */
    private Long lastTaskId;

    /** 最近一次完整 SD Prompt（最多2000字符） */
    private String lastPrompt;

    /** 当前风格 SD 关键词 */
    private String currentStyle;

    /** 当前颜色 SD 关键词 */
    private String currentColors;

    /** 当前面料 SD 关键词 */
    private String currentFabric;

    /** 当前长度 SD 关键词 */
    private String currentLength;

    /** 当前版型 SD 关键词 */
    private String currentFit;

    /** 近期批量任务ID列表（JSON 数组） */
    private String variantTaskIds;

}
