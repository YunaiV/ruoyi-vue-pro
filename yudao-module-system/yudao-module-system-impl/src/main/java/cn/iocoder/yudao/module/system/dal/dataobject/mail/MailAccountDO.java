package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@TableName(value = "system_mail_account", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MailAccountDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 邮箱
     */
    private String from;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 是否开启ssl
     */
    private Boolean sslEnable;


}
