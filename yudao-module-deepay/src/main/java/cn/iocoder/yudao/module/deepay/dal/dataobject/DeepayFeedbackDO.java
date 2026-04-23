package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈学习表 deepay_feedback
 *
 * <p>由 {@link cn.iocoder.yudao.module.deepay.agent.FeedbackAgent} 写入。</p>
 */
@TableName("deepay_feedback")
@Data
public class DeepayFeedbackDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 deepay_design_image.id */
    private Long imageId;

    /** 图片 URL */
    private String imageUrl;

    /** 用户 ID */
    private String userId;

    /** 是否被选中（1=是 / 0=否） */
    private Integer selected;

    private LocalDateTime createdAt;

}
