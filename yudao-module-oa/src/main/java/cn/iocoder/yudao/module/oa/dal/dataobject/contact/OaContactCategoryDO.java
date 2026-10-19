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
 * OA 联系人分类 DO
 *
 * @author 芋道源码
 */
@TableName("oa_contact_category")
@KeySequence("oa_contact_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaContactCategoryDO extends BaseDO {

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 显示顺序
     */
    private Integer sort;

}
