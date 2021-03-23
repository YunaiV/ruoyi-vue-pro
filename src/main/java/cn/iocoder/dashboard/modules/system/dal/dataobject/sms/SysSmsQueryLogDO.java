package cn.iocoder.dashboard.modules.system.dal.dataobject.sms;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信日志
 *
 * @author zzf
 * @since 2021-01-25
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName(value = "sms_query_log", autoResultMap = true)
public class SysSmsQueryLogDO implements Serializable {

    /**
     * 自增编号
     */
    private Long id;

    /**
     * 第三方唯一标识
     */
    private String apiId;

    /**
     * 短信渠道编码(来自枚举类)
     */
    private String channelCode;

    /**
     * 短信渠道id
     */
    private Long channelId;

    /**
     * 模板id
     */
    private String templateCode;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 内容
     */
    private String content;

    /**
     * 发送状态
     *
     * @see cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum
     */
    private Integer sendStatus;

    /**
     * 是否获取过结果[0否 1是]
     */
    private Integer gotResult;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发送时间
     */
    private Date sendTime;

}
