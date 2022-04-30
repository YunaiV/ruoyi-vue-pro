package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 邮箱模版
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_template", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class MailTemplateDO extends BaseDO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 模版名称
     */
    private String name;
    /**
     * 模版编号
     */
    private String code;
    /**
     * 邮箱账号主键
     */
    private Long accountId;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
