package cn.iocoder.dashboard.framework.sms.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息内容实体类
 */
@Data
public class SmsResultDetail implements Serializable {

    /**
     * 状态   1成功 2失败 3等待回执
     */
    private Integer status;

    /**
     * 接收手机号
     */
    private String phone;

    /**
     * 提示
     */
    private String message;

    /**
     * 时间
     */
    private Date createTime;
}
