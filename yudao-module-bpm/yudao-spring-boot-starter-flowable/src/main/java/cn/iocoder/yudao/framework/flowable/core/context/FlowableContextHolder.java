package cn.iocoder.yudao.framework.flowable.core.context;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 工作流--用户用到的上下文相关信息
 */
public class FlowableContextHolder {

    private static final ThreadLocal<Map<String, List<Long>>> ASSIGNEE = new TransmittableThreadLocal<>();

    /**
     * 通过流程任务的定义 key ，拿到提前选好的审批人
     * 此方法目的：首次创建流程实例时，数据库中还查询不到 assignee 字段，所以存入上下文中获取
     *
     * @param taskDefinitionKey 流程任务 key
     * @return 审批人 ID 集合
     */
    public static List<Long> getAssigneeByTaskDefinitionKey(String taskDefinitionKey) {
        if (CollUtil.isNotEmpty(ASSIGNEE.get())) {
            return ASSIGNEE.get().get(taskDefinitionKey);
        }
        return Collections.emptyList();
    }

    /**
     * 存入提前选好的审批人到上下文线程变量中
     *
     * @param assignee 流程任务 key -> 审批人 ID 炅和
     */
    public static void setAssignee(Map<String, List<Long>> assignee) {
        ASSIGNEE.set(assignee);
    }

}
