package cn.iocoder.yudao.module.oa.dal.dataobject.announcement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.announcement.OaAnnouncementTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 公告 DO
 *
 * @author 芋道源码
 */
@TableName("oa_announcement")
@KeySequence("oa_announcement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaAnnouncementDO extends BaseDO {

    /**
     * 公告编号
     */
    @TableId
    private Long id;
    /**
     * 公告类型
     *
     * 枚举 {@link OaAnnouncementTypeEnum}
     */
    private Integer type;
    /**
     * 优先级
     *
     * 枚举 {@link OaPriorityEnum}
     */
    private Integer priority;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 相关链接
     */
    private String url;
    /**
     * 是否置顶
     */
    private Boolean top;
    // 公告创建即发布，发布时间复用 BaseDO.createTime，编辑时保持不变。

}
