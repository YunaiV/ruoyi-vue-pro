package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotThinkModelFunctionPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodelfunction.IotThinkModelFunctionConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.productthingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 产品物模型")
@RestController
@RequestMapping("/iot/think-model-function")
@Validated
public class IotProductThingModelController {

    @Resource
    private IotThinkModelFunctionService thinkModelFunctionService;

    @PostMapping("/create")
    @Operation(summary = "创建产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:create')")
    public CommonResult<Long> createThinkModelFunction(@Valid @RequestBody IotThinkModelFunctionSaveReqVO createReqVO) {
        return success(thinkModelFunctionService.createThinkModelFunction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:update')")
    public CommonResult<Boolean> updateThinkModelFunction(@Valid @RequestBody IotThinkModelFunctionSaveReqVO updateReqVO) {
        thinkModelFunctionService.updateThinkModelFunction(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:delete')")
    public CommonResult<Boolean> deleteThinkModelFunction(@RequestParam("id") Long id) {
        thinkModelFunctionService.deleteThinkModelFunction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<IotThinkModelFunctionRespVO> getThinkModelFunction(@RequestParam("id") Long id) {
        IotProductThingModelDO function = thinkModelFunctionService.getThinkModelFunction(id);
        return success(IotThinkModelFunctionConvert.INSTANCE.convert(function));
    }

    @GetMapping("/list-by-product-id")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<List<IotThinkModelFunctionRespVO>> getThinkModelFunctionListByProductId(@RequestParam("productId") Long productId) {
        List<IotProductThingModelDO> list = thinkModelFunctionService.getThinkModelFunctionListByProductId(productId);
        return success(IotThinkModelFunctionConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品物模型分页")
    @PreAuthorize("@ss.hasPermission('iot:think-model-function:query')")
    public CommonResult<PageResult<IotThinkModelFunctionRespVO>> getThinkModelFunctionPage(@Valid IotThinkModelFunctionPageReqVO pageReqVO) {
        PageResult<IotProductThingModelDO> pageResult = thinkModelFunctionService.getThinkModelFunctionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotThinkModelFunctionRespVO.class));
    }

}
