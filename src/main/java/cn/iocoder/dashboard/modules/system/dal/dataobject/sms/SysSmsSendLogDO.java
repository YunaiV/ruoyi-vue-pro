package cn.iocoder.dashboard.modules.system.dal.dataobject.sms;

import cn.iocoder.dashboard.common.enums.UserTypeEnum;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;
import java.util.Map;

/**
 * 短信发送日志
 *
 * @author zzf
 * @since 2021-01-25
 */
@TableName(value = "sms_send_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysSmsSendLogDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

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

    // ========= 模板相关字段 =========

    /**
     * 短信模板编号
     *
     * 关联 {@link SysSmsTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 模板编码
     *
     * 冗余 {@link SysSmsTemplateDO#getCode()}
     */
    private String templateCode;
    /**
     * 短信类型
     *
     * 冗余 {@link SysSmsTemplateDO#getType()}
     */
    private Integer templateType;
    /**
     * 基于 {@link SysSmsTemplateDO#getContent()} 格式化后的内容
     */
    private String templateContent;
    /**
     * 基于 {@link SysSmsTemplateDO#getParams()} 输入后的内容
     */
    private Map<String, Object> templateParams;
    /**
     * 短信 API 的模板编号
     *
     * 冗余 {@link SysSmsTemplateDO#getApiTemplateId()}
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
     * 枚举 {@link SysSmsSendStatusEnum}
     */
    private Integer sendStatus;
    /**
     * 发送失败的类型
     *
     * 枚举 {@link SmsSendFailureTypeEnum}
     */
    private Integer sendFailureType;
    /**
     * 发送成功时间
     */
    private Date sendTime;
    /**
     * 短信 API 发送失败的类型
     *
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiSendFailureType;
    /**
     * 短信 API 发送失败的提示
     */
    private String apiSendFailureMsg;
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
     * 是否获取过结果[0否 1是]
     */
    private Integer gotResult;

}
