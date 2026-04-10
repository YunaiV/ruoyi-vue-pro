package cn.iocoder.yudao.module.mes.controller.admin.wm.barcode;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 条码配置")
@RestController
@RequestMapping("/mes/wm/barcode-config")
@Validated
public class MesWmBarcodeConfigController {

    @Resource
    private MesWmBarcodeConfigService barcodeConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建条码配置")
    @PreAuthorize("@ss.hasPermission('mes:wm-barcode-config:create')")
    public CommonResult<Long> createBarcodeConfig(@Valid @RequestBody MesWmBarcodeConfigSaveReqVO createReqVO) {
        return success(barcodeConfigService.createBarcodeConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新条码配置")
    @PreAuthorize("@ss.hasPermission('mes:wm-barcode-config:update')")
    public CommonResult<Boolean> updateBarcodeConfig(@Valid @RequestBody MesWmBarcodeConfigSaveReqVO updateReqVO) {
        barcodeConfigService.updateBarcodeConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除条码配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-barcode-config:delete')")
    public CommonResult<Boolean> deleteBarcodeConfig(@RequestParam("id") Long id) {
        barcodeConfigService.deleteBarcodeConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得条码配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-barcode-config:query')")
    public CommonResult<MesWmBarcodeConfigRespVO> getBarcodeConfig(@RequestParam("id") Long id) {
        MesWmBarcodeConfigDO config = barcodeConfigService.getBarcodeConfig(id);
        return success(BeanUtils.toBean(config, MesWmBarcodeConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得条码配置分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-barcode-config:query')")
    public CommonResult<PageResult<MesWmBarcodeConfigRespVO>> getBarcodeConfigPage(
            @Valid MesWmBarcodeConfigPageReqVO pageReqVO) {
        PageResult<MesWmBarcodeConfigDO> pageResult = barcodeConfigService.getBarcodeConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesWmBarcodeConfigRespVO.class));
    }

}
