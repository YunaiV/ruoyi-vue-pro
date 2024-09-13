package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodelfunction.IotThinkModelFunctionConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
        thinkModelFunctionService.updateThinkModelFunction(updateReqVO);
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

    @GetMapping("/get-by-product-key")
    @Operation(summary = "获得IoT 产品物模型")
    @Parameter(name = "productKey", description = "产品Key", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<IotThinkModelFunctionRespVO> getThinkModelFunctionByProductKey(@RequestParam("productKey") String productKey) {
        IotThinkModelFunctionDO thinkModelFunction = thinkModelFunctionService.getThinkModelFunctionByProductKey(productKey);
        IotThinkModelFunctionRespVO respVO = IotThinkModelFunctionConvert.INSTANCE.convert(thinkModelFunction);
        return success(respVO);
    }

    @GetMapping("/get-by-product-id")
    @Operation(summary = "获得IoT 产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<IotThinkModelFunctionRespVO> getThinkModelFunctionByProductId(@RequestParam("productId") Long productId) {
        IotThinkModelFunctionDO thinkModelFunction = thinkModelFunctionService.getThinkModelFunctionByProductId(productId);
        IotThinkModelFunctionRespVO respVO = IotThinkModelFunctionConvert.INSTANCE.convert(thinkModelFunction);
        return success(respVO);
    }
}
