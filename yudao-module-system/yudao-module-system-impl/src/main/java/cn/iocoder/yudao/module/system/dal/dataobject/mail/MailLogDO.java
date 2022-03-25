package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import java.sql.Timestamp;
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
 * 
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value="SystemMailLog对象", description="")
public class MailLogDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;

    @TableField("account_code")
    private String accountCode;

    @TableField("from")
    private String from;

    @TableField("temple_id")
    private String templeId;

    @TableField("temple_code")
    private String templeCode;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("to")
    private String to;

    @TableField("sendTime")
    private Timestamp sendTime;

    @TableField("sendStatus")
    private Boolean sendStatus;

    @TableField("sendResult")
    private String sendResult;


}
