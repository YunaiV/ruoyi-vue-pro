package cn.iocoder.yudao.module.system.framework.sms.core.client.dto;

import lombok.Data;

/**
 * 短信发送 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class SmsSendRespDTO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * API 请求编号
     */
    private String apiRequestId;

    // ==================== 成功时字段 ====================

    /**
     * 短信 API 发送返回的序号
     */
    private String serialNo;

    // ==================== 失败时字段 ====================

    /**
     * API 返回错误码
     *
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiCode;
    /**
     * API 返回提示
     */
    private String apiMsg;

}
