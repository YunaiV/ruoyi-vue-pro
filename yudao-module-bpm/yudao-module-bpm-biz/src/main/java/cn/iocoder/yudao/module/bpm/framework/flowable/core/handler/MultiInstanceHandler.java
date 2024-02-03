package cn.iocoder.yudao.module.bpm.framework.flowable.core.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.flowable.core.enums.ProcessConstants;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 多实例处理类
 */
@AllArgsConstructor
@Component("multiInstanceHandler")
public class MultiInstanceHandler {

    @Resource
    private AdminUserApi userApi;

    @Resource
    private PermissionApi permissionApi;

    /**
     * 流程发起人那种情况不需要处理，
     * 由 flowable 完成
     *
     * @param execution flowable的执行对象
     * @return 用户ID
     */
    public Set<String> getUserIds(DelegateExecution execution) {
        Set<String> candidateUserIds = new LinkedHashSet<>();
        FlowElement flowElement = execution.getCurrentFlowElement();
        if (ObjectUtil.isNotEmpty(flowElement) && flowElement instanceof UserTask userTask) {
            String dataType = userTask.getAttributeValue(ProcessConstants.NAMESPASE, ProcessConstants.PROCESS_CUSTOM_DATA_TYPE);
            if ("USERS".equals(dataType) && CollUtil.isNotEmpty(userTask.getCandidateUsers())) {
                // 添加候选用户id
                candidateUserIds.addAll(userTask.getCandidateUsers());
            } else if (CollUtil.isNotEmpty(userTask.getCandidateGroups())) {
                // 获取组的ID，角色ID集合或部门ID集合
                List<Long> groups = userTask.getCandidateGroups().stream()
                        // 例如部门DEPT100，100才是部门id
                        .map(item -> Long.parseLong(item.substring(4)))
                        .collect(Collectors.toList());
                List<Long> userIds = new ArrayList<>();
                if ("ROLES".equals(dataType)) {
                    // 通过角色id，获取所有用户id集合
                    Set<Long> userRoleIdListByRoleIds = permissionApi.getUserRoleIdListByRoleIds(groups);
                    userIds = new ArrayList<>(userRoleIdListByRoleIds);
                } else if ("DEPTS".equals(dataType)) {
                    // 通过部门id，获取所有用户id集合
                    List<AdminUserRespDTO> userListByDeptIds = userApi.getUserListByDeptIds(groups);
                    userIds = convertList(userListByDeptIds, AdminUserRespDTO::getId);
                }
                // 添加候选用户id
                userIds.forEach(id -> candidateUserIds.add(String.valueOf(id)));
            }
        }
        return candidateUserIds;
    }
}
