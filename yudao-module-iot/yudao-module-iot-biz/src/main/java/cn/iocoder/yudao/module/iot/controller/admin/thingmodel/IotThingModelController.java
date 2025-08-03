package cn.iocoder.yudao.module.iot.controller.admin.thingmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 产品物模型")
@RestController
@RequestMapping("/iot/thing-model")
@Validated
public class IotThingModelController {

    @Resource
    private IotThingModelService thingModelService;

    @PostMapping("/create")
    @Operation(summary = "创建产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:create')")
    public CommonResult<Long> createThingModel(@Valid @RequestBody IotThingModelSaveReqVO createReqVO) {
        return success(thingModelService.createThingModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:update')")
    public CommonResult<Boolean> updateThingModel(@Valid @RequestBody IotThingModelSaveReqVO updateReqVO) {
        thingModelService.updateThingModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:thing-model:delete')")
    public CommonResult<Boolean> deleteThingModel(@RequestParam("id") Long id) {
        thingModelService.deleteThingModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<IotThingModelRespVO> getThingModel(@RequestParam("id") Long id) {
        IotThingModelDO thingModel = thingModelService.getThingModel(id);
        return success(BeanUtils.toBean(thingModel, IotThingModelRespVO.class));
    }

    @GetMapping("/list-by-product-id")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<List<IotThingModelRespVO>> getThingModelListByProductId(@RequestParam("productId") Long productId) {
        List<IotThingModelDO> list = thingModelService.getThingModelListByProductId(productId);
        return success(BeanUtils.toBean(list, IotThingModelRespVO.class));
    }

    // TODO @puhui @super：getThingModelListByProductId 和 getThingModelListByProductId 可以融合么？
    @GetMapping("/list")
    @Operation(summary = "获得产品物模型列表")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<List<IotThingModelRespVO>> getThingModelListByProductId(@Valid IotThingModelListReqVO reqVO) {
        List<IotThingModelDO> list = thingModelService.getThingModelList(reqVO);
        return success(BeanUtils.toBean(list, IotThingModelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品物模型分页")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<PageResult<IotThingModelRespVO>> getThingModelPage(@Valid IotThingModelPageReqVO pageReqVO) {
        PageResult<IotThingModelDO> pageResult = thingModelService.getProductThingModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotThingModelRespVO.class));
    }

}
