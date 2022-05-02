package cn.iocoder.yudao.module.system.dal.dataobject.sms;

import cn.iocoder.yudao.module.system.enums.sms.SmsTemplateTypeEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 短信模板 DO
 *
 * @author zzf
 * @since 2021-01-25
 */
@TableName(value = "system_sms_template", autoResultMap = true)
@KeySequence("system_sms_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplateDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

    // ========= 模板相关字段 =========

    /**
     * 短信类型
     *
     * 枚举 {@link SmsTemplateTypeEnum}
     */
    private Integer type;
    /**
     * 启用状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 模板编码，保证唯一
     */
    private String code;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板内容
     *
     * 内容的参数，使用 {} 包括，例如说 {name}
     */
    private String content;
    /**
     * 参数数组(自动根据内容生成)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> params;
    /**
     * 备注
     */
    private String remark;
    /**
     * 短信 API 的模板编号
     */
    private String apiTemplateId;

    // ========= 渠道相关字段 =========

    /**
     * 短信渠道编号
     *
     * 关联 {@link SmsChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 短信渠道编码
     *
     * 冗余 {@link SmsChannelDO#getCode()}
     */
    private String channelCode;

}
