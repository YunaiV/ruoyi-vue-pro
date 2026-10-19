package cn.iocoder.yudao.module.oa.dal.dataobject.note;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 笔记持有关系 DO
 *
 * @author 芋道源码
 */
@TableName("oa_note_receiver")
@KeySequence("oa_note_receiver_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaNoteReceiverDO extends BaseDO {

    /**
     * 持有关系编号
     */
    @TableId
    private Long id;
    /**
     * 笔记编号
     *
     * 关联 {@link OaNoteDO#getId()}
     */
    private Long noteId;
    /**
     * 持有人用户编号，包含创建人和共享接收人
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;

}
