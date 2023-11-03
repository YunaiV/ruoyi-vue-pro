package cn.iocoder.yudao.module.system.dal.dataobject.sms;

import cn.iocoder.yudao.module.system.enums.sms.SmsReceiveStatusEnum;
import cn.iocoder.yudao.module.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 短信日志 DO
 *
 * @author zzf
 * @since 2021-01-25
 */
@TableName(value = "system_sms_log", autoResultMap = true)
@KeySequence("system_sms_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsLogDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

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

    // ========= 模板相关字段 =========

    /**
     * 模板编号
     *
     * 关联 {@link SmsTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 模板编码
     *
     * 冗余 {@link SmsTemplateDO#getCode()}
     */
    private String templateCode;
    /**
     * 短信类型
     *
     * 冗余 {@link SmsTemplateDO#getType()}
     */
    private Integer templateType;
    /**
     * 基于 {@link SmsTemplateDO#getContent()} 格式化后的内容
     */
    private String templateContent;
    /**
     * 基于 {@link SmsTemplateDO#getParams()} 输入后的参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateParams;
    /**
     * 短信 API 的模板编号
     *
     * 冗余 {@link SmsTemplateDO#getApiTemplateId()}
     */
    private String apiTemplateId;

    // ========= 手机相关字段 =========

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    // ========= 发送相关字段 =========

    /**
     * 发送状态
     *
     * 枚举 {@link SmsSendStatusEnum}
     */
    private Integer sendStatus;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 发送结果的编码
     *
     * 枚举 {@link SmsFrameworkErrorCodeConstants}
     */
    private Integer sendCode;
    /**
     * 发送结果的提示
     *
     * 一般情况下，使用 {@link SmsFrameworkErrorCodeConstants}
     * 异常情况下，通过格式化 Exception 的提示存储
     */
    private String sendMsg;
    /**
     * 短信 API 发送结果的编码
     *
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiSendCode;
    /**
     * 短信 API 发送失败的提示
     */
    private String apiSendMsg;
    /**
     * 短信 API 发送返回的唯一请求 ID
     *
     * 用于和短信 API 进行定位于排错
     */
    private String apiRequestId;
    /**
     * 短信 API 发送返回的序号
     *
     * 用于和短信 API 平台的发送记录关联
     */
    private String apiSerialNo;

    // ========= 接收相关字段 =========

    /**
     * 接收状态
     *
     * 枚举 {@link SmsReceiveStatusEnum}
     */
    private Integer receiveStatus;
    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;
    /**
     * 短信 API 接收结果的编码
     */
    private String apiReceiveCode;
    /**
     * 短信 API 接收结果的提示
     */
    private String apiReceiveMsg;

}
