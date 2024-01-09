package cn.iocoder.yudao.module.bpm.dal.dataobject.cc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 流程抄送对象
 *
 * @author kyle
 * @date 2022-05-19
 */
@TableName(value = "bpm_process_instance_copy", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmProcessInstanceCopyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 发起人Id
     */
    private Long startUserId;
    /**
     * 表单名
     */
    private String name;
    /**
     * 流程主键
     */
    private String processInstanceId;

    /**
     * 任务主键
     */
    private String taskId;
    /**
     * 用户主键
     */
    private Long userId;


}
