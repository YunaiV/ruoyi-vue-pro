package cn.iocoder.yudao.module.trade.controller.admin.delivery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.*;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryExpressTemplateConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 快递运费模板")
@RestController
@RequestMapping("/trade/delivery/express-template")
@Validated
public class DeliveryExpressTemplateController {

    @Resource
    private DeliveryExpressTemplateService deliveryExpressTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建快递运费模板")
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:create')")
    public CommonResult<Long> createDeliveryExpressTemplate(@Valid @RequestBody DeliveryExpressTemplateCreateReqVO createReqVO) {
        return success(deliveryExpressTemplateService.createDeliveryExpressTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新快递运费模板")
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:update')")
    public CommonResult<Boolean> updateDeliveryExpressTemplate(@Valid @RequestBody DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        deliveryExpressTemplateService.updateDeliveryExpressTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除快递运费模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:delete')")
    public CommonResult<Boolean> deleteDeliveryExpressTemplate(@RequestParam("id") Long id) {
        deliveryExpressTemplateService.deleteDeliveryExpressTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得快递运费模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:query')")
    public CommonResult<DeliveryExpressTemplateDetailRespVO> getDeliveryExpressTemplate(@RequestParam("id") Long id) {
        return success(deliveryExpressTemplateService.getDeliveryExpressTemplate(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获得快递运费模板列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:query')")
    public CommonResult<List<DeliveryExpressTemplateRespVO>> getDeliveryExpressTemplateList(@RequestParam("ids") Collection<Long> ids) {
        List<DeliveryExpressTemplateDO> list = deliveryExpressTemplateService.getDeliveryExpressTemplateList(ids);
        return success(DeliveryExpressTemplateConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取快递模版精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<DeliveryExpressTemplateSimpleRespVO>> getSimpleTemplateList() {
        // 获取运费模版列表，只要开启状态的
        List<DeliveryExpressTemplateDO> list = deliveryExpressTemplateService.getDeliveryExpressTemplateList();
        // 排序后，返回给前端
        return success(DeliveryExpressTemplateConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得快递运费模板分页")
    @PreAuthorize("@ss.hasPermission('trade:delivery:express-template:query')")
    public CommonResult<PageResult<DeliveryExpressTemplateRespVO>> getDeliveryExpressTemplatePage(@Valid DeliveryExpressTemplatePageReqVO pageVO) {
        PageResult<DeliveryExpressTemplateDO> pageResult = deliveryExpressTemplateService.getDeliveryExpressTemplatePage(pageVO);
        return success(DeliveryExpressTemplateConvert.INSTANCE.convertPage(pageResult));
    }

}
