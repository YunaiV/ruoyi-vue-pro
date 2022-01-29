package cn.iocoder.yudao.module.system.dal.dataobject.sms;

import cn.iocoder.yudao.module.system.enums.sms.SysSmsTemplateTypeEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
@TableName(value = "sys_sms_template", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysSmsTemplateDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

    // ========= 模板相关字段 =========

    /**
     * 短信类型
     *
     * 枚举 {@link SysSmsTemplateTypeEnum}
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
     * 关联 {@link SysSmsChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 短信渠道编码
     *
     * 冗余 {@link SysSmsChannelDO#getCode()}
     */
    private String channelCode;

}
