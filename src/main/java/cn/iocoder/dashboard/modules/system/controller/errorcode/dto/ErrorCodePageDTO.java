package cn.iocoder.dashboard.modules.system.controller.errorcode.dto;

import cn.iocoder.dashboard.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 错误码分页 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ErrorCodePageDTO extends PageParam {

    /**
     * 错误码编码
     */
    private Integer code;
    /**
     * 错误码错误提示
     *
     * 模糊匹配
     */
    private String message;
    /**
     * 错误码分组
     *
     * 模糊匹配
     */
    private String group;

}
