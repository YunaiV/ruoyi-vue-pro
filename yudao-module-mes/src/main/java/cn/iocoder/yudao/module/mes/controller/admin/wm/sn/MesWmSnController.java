package cn.iocoder.yudao.module.mes.controller.admin.wm.sn;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGroupRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.sn.MesWmSnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES SN 码")
@RestController
@RequestMapping("/mes/wm/sn")
@Validated
public class MesWmSnController {

    @Resource
    private MesWmSnService snService;
    @Resource
    private MesMdItemService itemService;

    @PostMapping("/generate")
    @Operation(summary = "生成 SN 码")
    @PreAuthorize("@ss.hasPermission('mes:wm-sn:create')")
    public CommonResult<Boolean> generateSnCodes(@Valid @RequestBody MesWmSnGenerateReqVO reqVO) {
        snService.generateSnCodes(reqVO);
        return success(true);
    }

    @GetMapping("/group-page")
    @Operation(summary = "获得 SN 码分组分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-sn:query')")
    public CommonResult<PageResult<MesWmSnGroupRespVO>> getSnGroupPage(@Valid MesWmSnPageReqVO reqVO) {
        PageResult<MesWmSnGroupRespVO> pageResult = snService.getSnGroupPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        buildGroupItemInfo(pageResult.getList());
        return success(pageResult);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除 SN 码（按批次 UUID）")
    @Parameter(name = "uuid", description = "批次 UUID", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-sn:delete')")
    public CommonResult<Boolean> deleteSnBatch(@RequestParam("uuid") @NotBlank(message = "批次 UUID 不能为空") String uuid) {
        snService.deleteSnByUuid(uuid);
        return success(true);
    }

    @GetMapping("/group-export-excel")
    @Operation(summary = "导出 SN 码分组 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-sn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSnGroupExcel(@Valid MesWmSnPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesWmSnGroupRespVO> list = snService.getSnGroupPage(reqVO).getList();
        buildGroupItemInfo(list);
        ExcelUtils.write(response, "SN码分组.xls", "数据", MesWmSnGroupRespVO.class, list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出批次 SN 码明细 Excel")
    @Parameter(name = "uuid", description = "批次 UUID", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-sn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSnExcel(@RequestParam("uuid") String uuid, HttpServletResponse response) throws IOException {
        List<MesWmSnRespVO> list = BeanUtils.toBean(snService.getSnListByUuid(uuid), MesWmSnRespVO.class);
        buildItemInfo(list);
        ExcelUtils.write(response, "SN码明细.xls", "数据", MesWmSnRespVO.class, list);
    }

    // ==================== 拼接 VO ====================

    private void buildGroupItemInfo(List<MesWmSnGroupRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(convertSet(list, MesWmSnGroupRespVO::getItemId));
        list.forEach(vo -> MapUtils.findAndThen(itemMap, vo.getItemId(), item ->
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification())));
    }

    private void buildItemInfo(List<MesWmSnRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(convertSet(list, MesWmSnRespVO::getItemId));
        list.forEach(vo -> MapUtils.findAndThen(itemMap, vo.getItemId(), item ->
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification())));
    }

}
