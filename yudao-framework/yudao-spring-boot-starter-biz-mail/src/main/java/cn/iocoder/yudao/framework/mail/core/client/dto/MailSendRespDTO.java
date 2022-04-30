package cn.iocoder.yudao.framework.mail.core.client.dto;

import lombok.Data;

/**
 * 短信发送 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class MailSendRespDTO {

    /**
     * 短信 API 发送返回的序号
     */
    private String serialNo;

}
