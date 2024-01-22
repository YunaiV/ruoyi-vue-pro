package cn.iocoder.yudao.module.bpm.controller.admin.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageItemRespVO;
import cn.iocoder.yudao.module.bpm.service.task.cc.BpmProcessInstanceCopyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程实例抄送")
@RestController
@RequestMapping("/bpm/process-instance/cc")
@Validated
public class BpmProcessInstanceCopyController {

    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;

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
        return success(processInstanceCopyService.getMyProcessInstanceCopyPage(getLoginUserId(), pageReqVO));
    }

}
