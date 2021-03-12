package cn.iocoder.dashboard.modules.system.service.errorcode.bo;

import cn.iocoder.dashboard.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 错误码分页 BO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ErrorCodePageBO extends PageParam {

    /**
     * 错误码编码
     */
    private Integer code;
    /**
     * 错误码错误提示
     */
    private String message;
    /**
     * 错误码分组
     */
    private String group;

}