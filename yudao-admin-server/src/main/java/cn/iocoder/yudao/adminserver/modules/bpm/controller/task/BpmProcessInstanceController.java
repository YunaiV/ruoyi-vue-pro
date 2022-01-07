package cn.iocoder.yudao.adminserver.modules.bpm.controller.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceMyPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstancePageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "流程实例") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/process-instance")
@Validated
public class BpmProcessInstanceController {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    // TODO 芋艿：权限

    @PostMapping("/create")
    @ApiOperation("新建流程实例")
    public CommonResult<String> createProcessInstance(@Valid @RequestBody BpmProcessInstanceCreateReqVO createReqVO) {
        return success(processInstanceService.createProcessInstance(getLoginUserId(), createReqVO));
    }

    @GetMapping("/my-page")
    @ApiOperation(value = "获得我的实例分页列表", notes = "在【我的流程】菜单中，进行调用")
    public CommonResult<PageResult<BpmProcessInstancePageItemRespVO>> getMyProcessInstancePage(
            @Valid BpmProcessInstanceMyPageReqVO pageReqVO) {
        return success(processInstanceService.getMyProcessInstancePage(getLoginUserId(), pageReqVO));
    }

}
