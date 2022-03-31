package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    // TODO @wangjingyi：应该使用 accountId 呀
    /**
     * 用户名
     */
    private String username;
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
    private String status; // TODO @wangjingyi：Integer
    /**
     * 备注
     */
    private String remark;

}
