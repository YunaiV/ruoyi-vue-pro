package cn.iocoder.yudao.module.iot.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductScriptDO;
import cn.iocoder.yudao.module.iot.script.example.ProductScriptSamples;
import cn.iocoder.yudao.module.iot.service.product.IotProductScriptService;
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

@Tag(name = "管理后台 - IoT 产品脚本信息")
@RestController
@RequestMapping("/iot/product-script")
@Validated
public class IotProductScriptController {

    @Resource
    private IotProductScriptService productScriptService;

    @Resource
    private ProductScriptSamples scriptSamples;

    @PostMapping("/create")
    @Operation(summary = "创建产品脚本")
    @PreAuthorize("@ss.hasPermission('iot:product-script:create')")
    public CommonResult<Long> createProductScript(@Valid @RequestBody IotProductScriptSaveReqVO createReqVO) {
        return success(productScriptService.createProductScript(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品脚本")
    @PreAuthorize("@ss.hasPermission('iot:product-script:update')")
    public CommonResult<Boolean> updateProductScript(@Valid @RequestBody IotProductScriptSaveReqVO updateReqVO) {
        productScriptService.updateProductScript(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品脚本")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-script:delete')")
    public CommonResult<Boolean> deleteProductScript(@RequestParam("id") Long id) {
        productScriptService.deleteProductScript(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品脚本详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-script:query')")
    public CommonResult<IotProductScriptRespVO> getProductScript(@RequestParam("id") Long id) {
        IotProductScriptDO productScript = productScriptService.getProductScript(id);
        return success(BeanUtils.toBean(productScript, IotProductScriptRespVO.class));
    }

    @GetMapping("/list-by-product")
    @Operation(summary = "获得产品的脚本列表")
    @Parameter(name = "productId", description = "产品编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-script:query')")
    public CommonResult<List<IotProductScriptRespVO>> getProductScriptListByProductId(
            @RequestParam("productId") Long productId) {
        List<IotProductScriptDO> list = productScriptService.getProductScriptListByProductId(productId);
        return success(BeanUtils.toBean(list, IotProductScriptRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品脚本分页")
    @PreAuthorize("@ss.hasPermission('iot:product-script:query')")
    public CommonResult<PageResult<IotProductScriptRespVO>> getProductScriptPage(
            @Valid IotProductScriptPageReqVO pageReqVO) {
        PageResult<IotProductScriptDO> pageResult = productScriptService.getProductScriptPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotProductScriptRespVO.class));
    }

    @PostMapping("/test")
    @Operation(summary = "测试产品脚本")
    @PreAuthorize("@ss.hasPermission('iot:product-script:test')")
    public CommonResult<IotProductScriptTestRespVO> testProductScript(
            @Valid @RequestBody IotProductScriptTestReqVO testReqVO) {
        return success(productScriptService.testProductScript(testReqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新产品脚本状态")
    @PreAuthorize("@ss.hasPermission('iot:product-script:update')")
    public CommonResult<Boolean> updateProductScriptStatus(
            @Valid @RequestBody IotProductScriptUpdateStatusReqVO updateStatusReqVO) {
        productScriptService.updateProductScriptStatus(updateStatusReqVO.getId(), updateStatusReqVO.getStatus());
        return success(true);
    }

    @GetMapping("/sample")
    @Operation(summary = "获取示例脚本")
    @Parameter(name = "type", description = "脚本类型(1=属性解析, 2=事件解析, 3=命令编码)", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:product-script:query')")
    public CommonResult<String> getSampleScript(@RequestParam("type") Integer type) {
        String sample;
        switch (type) {
            case 1:
                sample = scriptSamples.getPropertyParserSample();
                break;
            case 2:
                sample = scriptSamples.getEventParserSample();
                break;
            case 3:
                sample = scriptSamples.getCommandEncoderSample();
                break;
            default:
                sample = "// 不支持的脚本类型";
        }
        return success(sample);
    }
}