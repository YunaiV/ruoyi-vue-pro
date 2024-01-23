package cn.iocoder.yudao.module.bpm.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageItemRespVO;
import cn.iocoder.yudao.module.bpm.convert.cc.BpmProcessInstanceCopyConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.service.task.cc.BpmProcessInstanceCopyService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程实例抄送")
@RestController
@RequestMapping("/bpm/process-instance/cc")
@Validated
public class BpmProcessInstanceCopyController {

    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    private BpmProcessInstanceService bpmProcessInstanceService;
    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private BpmTaskService bpmTaskService;

    @PostMapping("/create")
    @Operation(summary = "抄送流程")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance-cc:create')")
    public CommonResult<Void> createProcessInstanceCC(@Valid @RequestBody BpmProcessInstanceCopyCreateReqVO createReqVO) {
        return success(processInstanceCopyService.createProcessInstanceCopy(getLoginUserId(), createReqVO));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得抄送流程分页列表")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance-cc:query')")
    public CommonResult<PageResult<BpmProcessInstanceCopyPageItemRespVO>> getProcessInstanceCCPage(
            @Valid BpmProcessInstanceCopyMyPageReqVO pageReqVO) {
        PageResult<BpmProcessInstanceCopyDO> pageResult = processInstanceCopyService.getMyProcessInstanceCopyPage(getLoginUserId(), pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        Map<String, String> taskNameByTaskIds = bpmTaskService.getTaskNameByTaskIds(convertSet(pageResult.getList(), BpmProcessInstanceCopyDO::getTaskId));
        Map<String, String> processInstanceNameByProcessInstanceIds = bpmTaskService.getProcessInstanceNameByProcessInstanceIds(convertSet(pageResult.getList(), BpmProcessInstanceCopyDO::getProcessInstanceId));

        Set<Long/* userId */> userIds = new HashSet<>();
        for (BpmProcessInstanceCopyDO doItem : pageResult.getList()) {
            userIds.add(doItem.getStartUserId());
            Long userId = Long.valueOf(doItem.getCreator());
            userIds.add(userId);
        }
        Map<Long, String> userMap = adminUserApi.getUserList(userIds).stream().collect(Collectors.toMap(
                AdminUserRespDTO::getId, AdminUserRespDTO::getNickname));

        // 转换返回
        return success(BpmProcessInstanceCopyConvert.INSTANCE.convertPage(pageResult, taskNameByTaskIds, processInstanceNameByProcessInstanceIds, userMap));
    }

}
