package cn.iocoder.yudao.module.oa.dal.dataobject.mail;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 企业邮箱服务配置 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_mail_provider", autoResultMap = true)
@KeySequence("oa_mail_provider_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaMailProviderDO extends BaseDO {

    /**
     * 服务配置编号
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * IMAP 连接配置
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private ConnectionConfig imap;
    /**
     * SMTP 连接配置
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private ConnectionConfig smtp;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 邮箱连接配置
     */
    @Data
    public static class ConnectionConfig {

        /**
         * 服务器域名
         */
        private String host;
        /**
         * 服务器端口
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

}
