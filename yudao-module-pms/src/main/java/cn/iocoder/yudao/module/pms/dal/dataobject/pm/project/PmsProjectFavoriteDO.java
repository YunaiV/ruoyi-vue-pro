package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 项目收藏 DO
 *
 * @author 芋道源码
 */
@TableName("pms_project_favorite")
@KeySequence("pms_project_favorite_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectFavoriteDO extends BaseDO {

    /**
     * 收藏编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     *
     * 关联 {@link PmsProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 收藏人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;

}
