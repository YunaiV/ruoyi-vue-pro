package cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流的表单结果
 * 用户每次填写工作流的申请表单时，会保存一条记录到该表】
 *
 * @author 芋道源码
 */
@TableName(value = "wf_form", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WfFormData extends BaseDO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 表单编号
     *
     * 关联 {@link WfForm#getId()}
     */
    private Long formId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 表单配置
     *
     * 冗余 {@link WfForm#getFields()}
     * 主要考虑，表单是可以修改的
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fields;
    /**
     * 表单值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> values;

}
