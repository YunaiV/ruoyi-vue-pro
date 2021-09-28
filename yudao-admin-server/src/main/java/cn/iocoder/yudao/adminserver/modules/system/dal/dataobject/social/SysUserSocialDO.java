package cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.social;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户和第三方用户关联表
 * @author weir
 */
@TableName("sys_user_social")
@Data
public class SysUserSocialDO extends BaseDO {
    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 角色 ID
     */
    private String socialUserId;
}


