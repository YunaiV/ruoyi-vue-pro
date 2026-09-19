package cn.iocoder.yudao.module.oa.dal.dataobject.announcement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 公告接收关系 DO
 *
 * @author 芋道源码
 */
@TableName("oa_announcement_receiver")
@KeySequence("oa_announcement_receiver_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaAnnouncementReceiverDO extends BaseDO {

    /**
     * 接收关系编号
     */
    @TableId
    private Long id;
    /**
     * 公告编号
     *
     * 关联 {@link OaAnnouncementDO#getId()}
     */
    private Long announcementId;
    /**
     * 接收人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long receiverUserId;
    /**
     * 是否已读
     */
    private Boolean readStatus;

}
