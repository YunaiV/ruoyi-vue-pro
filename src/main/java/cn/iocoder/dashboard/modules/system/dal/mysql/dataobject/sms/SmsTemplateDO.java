package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 短信模板
 *
 * @author zzf
 * @since 2021-01-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sms_template", autoResultMap = true)
public class SmsTemplateDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

    /**
     * 短信渠道编码(来自枚举类)
     */
    private String channelCode;

    /**
     * 短信渠道id (对于前端来说就是绑定一个签名)
     */
    private Long channelId;

    /**
     * 消息类型 [0验证码 1短信通知 2推广短信 3国际/港澳台消息]
     */
    private Integer type;

    /**
     * 业务编码(来自数据字典, 用户自定义业务场景 一个场景可以有多个模板)
     */
    private String bizCode;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 实际渠道模板唯一标识
     */
    private String apiTemplateId;

    /**
     * 内容
     */
    private String content;

    /**
     * 参数数组(自动根据内容生成)
     */
    private String params;

    /**
     * 备注
     */
    private String remark;

    /**
     * 启用状态（0正常 1停用）
     */
    private Integer status;

}
