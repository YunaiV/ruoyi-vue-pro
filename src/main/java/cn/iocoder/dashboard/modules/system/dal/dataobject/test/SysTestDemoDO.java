package cn.iocoder.dashboard.modules.system.dal.dataobject.test;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
* 字典类型 DO
*
* @author 芋艿
*/
@TableName("sys_test_demo")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysTestDemoDO extends BaseDO {

    /**
    * 字典主键
    */
    @TableId
    private Long id;
    /**
    * 字典名称
    */
    private String name;
    /**
    * 字典类型
    */
    private String dictType;
    /**
    * 状态
    */
    private Integer status;
    /**
    * 备注
    */
    private String remark;

}
