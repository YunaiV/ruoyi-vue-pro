package cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 动态表单 DO
 *
 * @author 芋艿
 */
@TableName("os_form")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsFormDO extends BaseDO {

    /**
     * 表单编号
     */
    @TableId
    private Long id;
    /**
     * 表单名称
     */
    private String name;
    /**
     * 商户状态
     */
    private Integer status;
    /**
     * 表单JSON
     */
    private String formJson;
    /**
     * 备注
     */
    private String remark;

}
