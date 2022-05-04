package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮箱账号
 * 配置发送邮箱的账号
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_account", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class MailAccountDO extends BaseDO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 邮箱
     */
    @TableField("`from`")
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
     * 是否开启 SSL
     */
    private Boolean sslEnable;

}
