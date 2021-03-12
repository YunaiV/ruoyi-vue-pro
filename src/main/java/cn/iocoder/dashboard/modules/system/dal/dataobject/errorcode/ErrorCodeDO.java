package cn.iocoder.dashboard.modules.system.dal.dataobject.errorcode;

import cn.iocoder.dashboard.modules.system.enums.errorcode.ErrorCodeTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName(value = "system_error_code")
@EqualsAndHashCode()
@Accessors(chain = true)
public class ErrorCodeDO {

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
     *
     * 外键 {@link ErrorCodeTypeEnum}
     */
    private Integer type;
    /**
     * 错误码分组
     *
     * 一般情况下，可以采用应用名
     */
    @TableField("`group`") // 避免和数据库关键字冲突
    private String group;
    /**
     * 错误码备注
     */
    private String memo;
}
