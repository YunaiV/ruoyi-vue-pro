package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value="MailAccount对象", description="邮箱账号")
@TableName(value = "system_mail_account", autoResultMap = true)
public class MailAccountDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;
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
