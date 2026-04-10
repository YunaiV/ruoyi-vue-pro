package cn.iocoder.yudao.module.mes.controller.admin.md.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.batchconfig.MesMdItemBatchConfigRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.batchconfig.MesMdItemBatchConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemBatchConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 物料批次属性配置")
@RestController
@RequestMapping("/mes/md/item-batch-config")
@Validated
public class MesMdItemBatchConfigController {

    @Resource
    private MesMdItemBatchConfigService itemBatchConfigService;

    @GetMapping("/get-by-item-id")
    @Operation(summary = "根据物料编号获取批次属性配置")
    @Parameter(name = "itemId", description = "物料编号", required = true, example = "69")
    @PreAuthorize("@ss.hasPermission('mes:md-item:query')")
    public CommonResult<MesMdItemBatchConfigRespVO> getItemBatchConfigByItemId(
            @RequestParam("itemId") Long itemId) {
        MesMdItemBatchConfigDO config = itemBatchConfigService.getItemBatchConfigByItemId(itemId);
        return success(BeanUtils.toBean(config, MesMdItemBatchConfigRespVO.class));
    }

    @PostMapping("/save")
    @Operation(summary = "保存批次属性配置（新增或更新）")
    @PreAuthorize("@ss.hasPermission('mes:md-item:update')")
    public CommonResult<Long> saveItemBatchConfig(@Valid @RequestBody MesMdItemBatchConfigSaveReqVO saveReqVO) {
        return success(itemBatchConfigService.saveItemBatchConfig(saveReqVO));
    }

}
