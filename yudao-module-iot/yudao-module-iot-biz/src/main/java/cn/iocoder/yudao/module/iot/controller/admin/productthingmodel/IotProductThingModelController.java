package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.productthingmodel.IotProductThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.productthingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.service.productthingmodel.IotProductThingModelService;
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
@RequestMapping("/iot/product-thing-model")
@Validated
public class IotProductThingModelController {

    @Resource
    private IotProductThingModelService thinkModelFunctionService;

    @PostMapping("/create")
    @Operation(summary = "创建产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:create')")
    public CommonResult<Long> createProductThingModel(@Valid @RequestBody IotProductThingModelSaveReqVO createReqVO) {
        return success(thinkModelFunctionService.createProductThingModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:update')")
    public CommonResult<Boolean> updateProductThingModel(@Valid @RequestBody IotProductThingModelSaveReqVO updateReqVO) {
        thinkModelFunctionService.updateProductThingModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:delete')")
    public CommonResult<Boolean> deleteProductThingModel(@RequestParam("id") Long id) {
        thinkModelFunctionService.deleteProductThingModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:query')")
    public CommonResult<IotProductThingModelRespVO> getProductThingModel(@RequestParam("id") Long id) {
        IotProductThingModelDO function = thinkModelFunctionService.getProductThingModel(id);
        return success(IotProductThingModelConvert.INSTANCE.convert(function));
    }

    @GetMapping("/list-by-product-id")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:query')")
    public CommonResult<List<IotProductThingModelRespVO>> getProductThingModelListByProductId(@RequestParam("productId") Long productId) {
        List<IotProductThingModelDO> list = thinkModelFunctionService.getProductThingModelListByProductId(productId);
        return success(IotProductThingModelConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品物模型分页")
    @PreAuthorize("@ss.hasPermission('iot:product-thing-model:query')")
    public CommonResult<PageResult<IotProductThingModelRespVO>> getProductThingModelPage(@Valid IotProductThingModelPageReqVO pageReqVO) {
        PageResult<IotProductThingModelDO> pageResult = thinkModelFunctionService.getProductThingModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotProductThingModelRespVO.class));
    }

}
