package cn.iocoder.yudao.module.bpm.service.candidate;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateRuleVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskRuleScriptEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.impl.BpmTaskAssignLeaderX1Script;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.impl.BpmTaskAssignLeaderX2Script;
import cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor.BpmCandidateScriptApiSourceInfoProcessor;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Disabled // TODO 芋艿：临时禁用，暂时不修复，等重构后解决
@Import({BpmCandidateSourceInfoProcessorChain.class,
        BpmCandidateScriptApiSourceInfoProcessor.class, BpmTaskAssignLeaderX1Script.class,
        BpmTaskAssignLeaderX2Script.class})
public class BpmCandidateSourceInfoProcessorChainTest extends BaseDbUnitTest {
    @Resource
    private BpmCandidateSourceInfoProcessorChain processorChain;

    @MockBean
    private BpmUserGroupService userGroupService;
    @MockBean
    private DeptApi deptApi;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private PermissionApi permissionApi;
    @MockBean
    private RoleApi roleApi;
    @MockBean
    private PostApi postApi;
    @MockBean
    private DictDataApi dictDataApi;
    @Resource
    private BpmCandidateScriptApiSourceInfoProcessor bpmCandidateScriptApiSourceInfoProcessor;

    @Test
    public void testCalculateTaskCandidateUsers_Role() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.ROLE.getType());
        // mock 方法
        when(permissionApi.getUserRoleIdListByRoleIds(eq(rule.getOptions())))
                .thenReturn(asSet(11L, 22L));
        mockGetUserMap(asSet(11L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

    @Test
    public void testCalculateTaskCandidateUsers_DeptMember() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType());
        // mock 方法
        List<AdminUserRespDTO> users = CollectionUtils.convertList(asSet(11L, 22L),
                id -> new AdminUserRespDTO().setId(id));
        when(adminUserApi.getUserListByDeptIds(eq(rule.getOptions()))).thenReturn(users);
        mockGetUserMap(asSet(11L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

    @Test
    public void testCalculateTaskCandidateUsers_DeptLeader() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType());
        // mock 方法
        DeptRespDTO dept1 = randomPojo(DeptRespDTO.class, o -> o.setLeaderUserId(11L));
        DeptRespDTO dept2 = randomPojo(DeptRespDTO.class, o -> o.setLeaderUserId(22L));
        when(deptApi.getDeptList(eq(rule.getOptions()))).thenReturn(Arrays.asList(dept1, dept2));
        mockGetUserMap(asSet(11L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

    @Test
    public void testCalculateTaskCandidateUsers_Post() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.POST.getType());
        // mock 方法
        List<AdminUserRespDTO> users = CollectionUtils.convertList(asSet(11L, 22L),
                id -> new AdminUserRespDTO().setId(id));
        when(adminUserApi.getUserListByPostIds(eq(rule.getOptions()))).thenReturn(users);
        mockGetUserMap(asSet(11L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

    @Test
    public void testCalculateTaskCandidateUsers_User() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.USER.getType());
        // mock 方法
        mockGetUserMap(asSet(1L, 2L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(1L, 2L), results);
    }

    @Test
    public void testCalculateTaskCandidateUsers_UserGroup() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(1L, 2L))
                .setType(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType());
        // mock 方法
        BpmUserGroupDO userGroup1 = randomPojo(BpmUserGroupDO.class, o -> o.setMemberUserIds(asSet(11L, 12L)));
        BpmUserGroupDO userGroup2 = randomPojo(BpmUserGroupDO.class, o -> o.setMemberUserIds(asSet(21L, 22L)));
        when(userGroupService.getUserGroupList(eq(rule.getOptions()))).thenReturn(Arrays.asList(userGroup1, userGroup2));
        mockGetUserMap(asSet(11L, 12L, 21L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 12L, 21L, 22L), results);
    }

    private void mockGetUserMap(Set<Long> assigneeUserIds) {
        Map<Long, AdminUserRespDTO> userMap = CollectionUtils.convertMap(assigneeUserIds, id -> id,
                id -> new AdminUserRespDTO().setId(id).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(adminUserApi.getUserMap(eq(assigneeUserIds))).thenReturn(userMap);
    }

    @Test
    public void testCalculateTaskCandidateUsers_Script() {
        // 准备参数
        BpmTaskCandidateRuleVO rule = new BpmTaskCandidateRuleVO().setOptions(asSet(20L, 21L))
                .setType(BpmTaskAssignRuleTypeEnum.SCRIPT.getType());
        // mock 方法
        BpmTaskAssignScript script1 = new BpmTaskAssignScript() {

            @Override
            public Set<Long> calculateTaskCandidateUsers(DelegateExecution task) {
                return singleton(11L);
            }

            @Override
            public BpmTaskRuleScriptEnum getEnum() {
                return BpmTaskRuleScriptEnum.LEADER_X1;
            }
        };
        BpmTaskAssignScript script2 = new BpmTaskAssignScript() {

            @Override
            public Set<Long> calculateTaskCandidateUsers(DelegateExecution task) {
                return singleton(22L);
            }

            @Override
            public BpmTaskRuleScriptEnum getEnum() {
                return BpmTaskRuleScriptEnum.LEADER_X2;
            }
        };
        bpmCandidateScriptApiSourceInfoProcessor.setScripts(Arrays.asList(script1, script2));
        mockGetUserMap(asSet(11L, 22L));

        // 调用
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.addRule(rule);
        Set<Long> results = processorChain.calculateTaskCandidateUsers(null, sourceInfo);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

    @Test
    public void testRemoveDisableUsers() {
        // 准备参数. 1L 可以找到；2L 是禁用的；3L 找不到
        Set<Long> assigneeUserIds = asSet(1L, 2L, 3L);
        // mock 方法
        AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                .put(user2.getId(), user2).build();
        when(adminUserApi.getUserMap(eq(assigneeUserIds))).thenReturn(userMap);

        // 调用
        processorChain.removeDisableUsers(assigneeUserIds);
        // 断言
        assertEquals(asSet(1L), assigneeUserIds);
    }

}