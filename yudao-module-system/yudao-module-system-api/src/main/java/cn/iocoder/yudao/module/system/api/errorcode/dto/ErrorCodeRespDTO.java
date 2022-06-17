package cn.iocoder.yudao.module.system.api.errorcode.dto;

import lombok.Data;

import java.util.Date;

/**
 * 错误码的 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class ErrorCodeRespDTO {

    /**
     * 错误码编码
     */
    private Integer code;
    /**
     * 错误码错误提示
     */
    private String message;
    /**
     * 更新时间
     */
    private Date updateTime;

}
