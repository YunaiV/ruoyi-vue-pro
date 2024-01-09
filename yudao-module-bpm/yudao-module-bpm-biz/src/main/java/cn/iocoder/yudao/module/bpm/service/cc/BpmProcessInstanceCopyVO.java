package cn.iocoder.yudao.module.bpm.service.cc;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


/**
 * 流程抄送视图对象 wf_copy
 *
 * @author ruoyi
 * @date 2022-05-19
 */
@Data
public class BpmProcessInstanceCopyVO {

    /**
     * 编号
     */
    @Schema(description = "抄送主键")
    private Long id;

    /**
     * 发起人Id
     */
    @Schema(description = "发起人Id")
    private Long startUserId;
    /**
     * 表单名
     */
    @Schema(description = "流程实例的名字")
    private String name;
    /**
     * 流程主键
     */
    @Schema(description = "流程实例的主键")
    private String processInstanceId;

    /**
     * 任务主键
     */
    @Schema(description = "发起抄送的任务编号")
    private String taskId;
    /**
     * 用户主键
     */
    @Schema(description = "用户编号")
    private Long userId;
}
