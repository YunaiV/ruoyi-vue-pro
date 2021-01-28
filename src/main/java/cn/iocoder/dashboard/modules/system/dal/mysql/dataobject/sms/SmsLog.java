package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName(value = "sms_log", autoResultMap = true)
public class SmsLog implements Serializable {

    /**
     * 自增编号
     */
    private Long id;

    /**
     * 短信渠道编码(来自枚举类)
     */
    private String channelCode;

    /**
     * 实际渠道短信唯一标识
     */
    private String apiSmsId;

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发送状态（0发送中 1成功 2失败）
     */
    private Integer sendStatus;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

}
