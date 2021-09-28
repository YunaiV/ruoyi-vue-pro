package cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.social;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import me.zhyd.oauth.model.AuthUser;

/**
 * @author weir
 */
@Data
@TableName("user_connection")
public class SocialUserDO extends AuthUser {
}
