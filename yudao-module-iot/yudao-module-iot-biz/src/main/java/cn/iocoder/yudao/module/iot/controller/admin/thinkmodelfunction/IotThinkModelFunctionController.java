package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;

@Tag(name = "管理后台 - IoT 产品物模型")
@RestController
@RequestMapping("/iot/think-model-function")
@Validated
public class IotThinkModelFunctionController {

    @Resource
    private IotThinkModelFunctionService thinkModelFunctionService;

    @PostMapping("/create")
    @Operation(summary = "创建IoT 产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:create')")
    public CommonResult<Long> createThinkModelFunction(@Valid @RequestBody IotThinkModelFunctionSaveReqVO createReqVO) {
        return success(thinkModelFunctionService.createThinkModelFunction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新IoT 产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:update')")
    public CommonResult<Boolean> updateThinkModelFunction(@Valid @RequestBody IotThinkModelFunctionSaveReqVO updateReqVO) {
        thinkModelFunctionService.updateThinkModelFunctionByProductKey(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IoT 产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:delete')")
    public CommonResult<Boolean> deleteThinkModelFunction(@RequestParam("id") Long id) {
        thinkModelFunctionService.deleteThinkModelFunction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得IoT 产品物模型")
    @Parameter(name = "productKey", description = "产品Key", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<IotThinkModelFunctionRespVO> getThinkModelFunctionByProductKey(@RequestParam("productKey")  String productKey) {
        IotThinkModelFunctionDO thinkModelFunction = thinkModelFunctionService.getThinkModelFunctionByProductKey(productKey);
        return success(BeanUtils.toBean(thinkModelFunction, IotThinkModelFunctionRespVO.class));
    }

}