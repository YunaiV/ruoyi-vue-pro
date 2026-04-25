package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * AI 悬浮对话框页面状态 DO
 *
 * <p>对应 Python {@code ai_assistant_state} 表，
 * 记录每个用户在各页面的 AI 悬浮窗位置与最小化状态，
 * 实现"不用时缩到角落、随时唤醒"的交互体验。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_assistant_state")
@KeySequence("ai_fashion_assistant_state_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionAssistantStateDO extends BaseDO {

    /** 编号 */
    @TableId
    private Long id;

    /** 用户编号 */
    private Long userId;

    /**
     * 页面名称
     * home / model_library / design_studio / image_library / 3d_viewer
     */
    private String pageName;

    /** 是否最小化（缩到角落） */
    private Boolean minimized;

    /** 窗口 X 坐标（前端像素） */
    private Integer positionX;

    /** 窗口 Y 坐标（前端像素） */
    private Integer positionY;

    /** 最近一次活跃时间 */
    private LocalDateTime lastActiveTime;

}
