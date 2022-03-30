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

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value="MailAccount对象", description="邮箱账号") // TODO @wangjingyi：不需要 swagger 注解
@TableName(value = "system_mail_account", autoResultMap = true) // TODO @wangjingyi：这个放在最上面，关键字段
public class MailAccountDO extends BaseDO implements Serializable {

    // TODO @wangjingyi：每个字段的注释；字段名，如果一直，不用 @TableField

    @TableId
    private Long id;

    @TableField("from")
    private String from;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("sslEnable")
    private Boolean sslEnable;


}
