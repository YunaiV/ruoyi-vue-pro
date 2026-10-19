package cn.iocoder.yudao.module.oa.dal.dataobject.discussion;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 讨论 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_discussion", autoResultMap = true)
@KeySequence("oa_discussion_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaDiscussionDO extends BaseDO {

    /**
     * 讨论编号
     */
    @TableId
    private Long id;
    /**
     * 发布人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 类型
     *
     * 枚举 {@link OaDiscussionTypeEnum}
     */
    private Integer type;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 是否允许多选投票
     */
    private Boolean voteMultiple;
    /**
     * 投票开始时间
     */
    private LocalDateTime voteStartTime;
    /**
     * 投票结束时间
     */
    private LocalDateTime voteEndTime;

}
