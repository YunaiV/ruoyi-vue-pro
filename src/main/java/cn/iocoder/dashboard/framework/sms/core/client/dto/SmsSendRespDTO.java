package cn.iocoder.dashboard.framework.sms.core.client.dto;

import lombok.Data;

/**
 * 短信发送响应 DTO
 *
 * @author 芋道源码
 */
@Data
public class SmsSendRespDTO {

    /**
     * 短信 API 发送返回的序号
     */
    private String serialNo;

}
