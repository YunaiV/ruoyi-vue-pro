package cn.iocoder.yudao.module.bpm.controller.admin.definition;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageReqVO;
import cn.iocoder.yudao.module.bpm.service.definition.FlowableModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 流程模型")
@RestController
@RequestMapping("/bpm/model")
@Validated
public class FlowableModelController {

    @Resource
    private FlowableModelService modelService;

    @GetMapping("/page")
    @ApiOperation(value = "获得模型分页")
    public CommonResult<PageResult<BpmModelPageItemRespVO>> getModelPage(BpmModelPageReqVO pageVO) {
        return success(modelService.getModelPage(pageVO));
    }
}
