package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodel.IotProductThinkModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import cn.iocoder.yudao.module.iot.service.thinkmodel.IotProductThinkModelService;
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
@RequestMapping("/iot/product-think-model")
@Validated
public class IotProductThinkModelController {

    @Resource
    private IotProductThinkModelService thinkModelFunctionService;

    @PostMapping("/create")
    @Operation(summary = "创建产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:create')")
    public CommonResult<Long> createProductThinkModel(@Valid @RequestBody IotProductThinkModelSaveReqVO createReqVO) {
        return success(thinkModelFunctionService.createProductThinkModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:update')")
    public CommonResult<Boolean> updateProductThinkModel(@Valid @RequestBody IotProductThinkModelSaveReqVO updateReqVO) {
        thinkModelFunctionService.updateProductThinkModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:delete')")
    public CommonResult<Boolean> deleteProductThinkModel(@RequestParam("id") Long id) {
        thinkModelFunctionService.deleteProductThinkModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:query')")
    public CommonResult<IotProductThinkModelRespVO> getProductThinkModel(@RequestParam("id") Long id) {
        IotProductThinkModelDO function = thinkModelFunctionService.getProductThinkModel(id);
        return success(IotProductThinkModelConvert.INSTANCE.convert(function));
    }

    @GetMapping("/list-by-product-id")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:query')")
    public CommonResult<List<IotProductThinkModelRespVO>> getProductThinkModelListByProductId(@RequestParam("productId") Long productId) {
        List<IotProductThinkModelDO> list = thinkModelFunctionService.getProductThinkModelListByProductId(productId);
        return success(IotProductThinkModelConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品物模型分页")
    @PreAuthorize("@ss.hasPermission('iot:product-think-model:query')")
    public CommonResult<PageResult<IotProductThinkModelRespVO>> getProductThinkModelPage(@Valid IotProductThinkModelPageReqVO pageReqVO) {
        PageResult<IotProductThinkModelDO> pageResult = thinkModelFunctionService.getProductThinkModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotProductThinkModelRespVO.class));
    }

}
