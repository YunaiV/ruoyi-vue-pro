package cn.iocoder.yudao.module.bpm.framework.flowable.core.simple;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskRejectHandlerType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 仿钉钉流程设计器审批节点配置 Model
 *
 * @author jason
 */
@Data
public class SimpleModelUserTaskConfig {

    /**
     * 候选人策略
     */
    private  Integer candidateStrategy;

    /**
     * 候选人参数
     */
    private  String candidateParam;

    /**
     * 字段权限
     */
    private List<Map<String,String>> fieldsPermission;

    /**
     * 审批方式
     */
    private  Integer approveMethod;

    /**
     * 超时处理
     */
    private TimeoutHandler timeoutHandler;

    /**
     * 用户任务拒绝处理
     */
    private RejectHandler rejectHandler;

    @Data
    public static class TimeoutHandler {

        /**
         * 是否开启超时处理
         */
        private Boolean enable;

        /**
         * 超时执行的动作
         */
        private Integer action;

        /**
         * 超时时间设置
         */
        private String timeDuration;

        /**
         * 如果执行动作是自动提醒, 最大提醒次数
         */
        private Integer maxRemindCount;
    }

    @Data
    public static class RejectHandler {

        /**
         * 用户任务拒绝处理类型 {@link BpmUserTaskRejectHandlerType}
         */
        private Integer type;

        /**
         * 用户任务拒绝后驳回的节点 Id
         */
        private String  returnNodeId;
    }

}
