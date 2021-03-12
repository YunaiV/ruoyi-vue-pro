package cn.iocoder.dashboard.modules.system.service.errorcode.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 错误码 BO
 */
@Data
@Accessors(chain = true)
public class ErrorCodeBO {

    /**
     * 错误码编号
     */
    private Integer id;
    /**
     * 错误码编码
     */
    private Integer code;
    /**
     * 错误码错误提示
     */
    private String message;
    /**
     * 错误码类型
     */
    private Integer type;
    /**
     * 错误码分组
     */
    private String group;
    /**
     * 错误码备注
     */
    private String memo;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最后更新时间
     */
    private Date updateTime;

}