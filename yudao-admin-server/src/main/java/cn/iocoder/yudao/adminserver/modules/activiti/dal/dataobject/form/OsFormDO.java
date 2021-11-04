package cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @风里雾里：切到 https://gitee.com/zhijiantianya/ruoyi-vue-pro/blob/feature/activiti/yudao-admin-server/src/main/java/cn/iocoder/yudao/adminserver/modules/workflow/dal/dataobject/form/WfForm.java 。status 添加进去哈。
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
