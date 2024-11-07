package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮箱账号 DO
 *
 * 用途：配置发送邮箱的账号
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_account", autoResultMap = true)
@KeySequence("system_mail_account_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class MailAccountDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 邮箱
     */
    private String mail;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * SMTP 服务器域名
     */
    private String host;
    /**
     * SMTP 服务器端口
     */
    private Integer port;
    /**
     * 是否开启 SSL
     */
    private Boolean sslEnable;
    /**
     * 是否开启 STARTTLS
     */
    private Boolean starttlsEnable;

}
