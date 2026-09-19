package cn.iocoder.yudao.module.oa.dal.dataobject.contact;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 联系人关联 DO，creator 为实际共享人，userId 为持有人
 *
 * @author 芋道源码
 */
@TableName("oa_contact_share")
@KeySequence("oa_contact_share_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaContactShareDO extends BaseDO {

    /**
     * 共享记录编号
     */
    @TableId
    private Long id;
    /**
     * 联系人编号
     *
     * 关联 {@link OaContactDO#getId()}
     */
    private Long contactId;
    /**
     * 共享接收人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 接收人分类编号
     *
     * 关联 {@link OaContactCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 是否已处理
     */
    private Boolean handleStatus;

}
