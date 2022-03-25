package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 邮箱模版
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value="SystemMailTemplate对象", description="")
public class MailTemplateDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;

    @TableField("name")
    private String name;

    @TableField("code")
    private String code;

    @TableField("username")
    private String username;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;


}
