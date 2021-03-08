package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms;

import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
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
@TableName(value = "sms_send_log", autoResultMap = true)
public class SysSmsSendLogDO implements Serializable {

    /**
     * 自增编号
     */
    private Long id;

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
     * 备注
     */
    private String remark;

    /**
     * 发送状态
     *
     * @see SmsSendStatusEnum
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private Date sendTime;

}
