package cn.iocoder.yudao.module.mes.controller.admin.qc.oqc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 出货检验单（OQC）")
@RestController
@RequestMapping("/mes/qc/oqc")
@Validated
public class MesQcOqcController {

    @Resource
    private MesQcOqcService oqcService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建出货检验单")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:create')")
    public CommonResult<Long> createOqc(@Valid @RequestBody MesQcOqcSaveReqVO createReqVO) {
        return success(oqcService.createOqc(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出货检验单")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:update')")
    public CommonResult<Boolean> updateOqc(@Valid @RequestBody MesQcOqcSaveReqVO updateReqVO) {
        oqcService.updateOqc(updateReqVO);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成出货检验单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:finish')")
    public CommonResult<Boolean> finishOqc(@RequestParam("id") Long id) {
        oqcService.finishOqc(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出货检验单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:delete')")
    public CommonResult<Boolean> deleteOqc(@RequestParam("id") Long id) {
        oqcService.deleteOqc(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出货检验单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:query')")
    public CommonResult<MesQcOqcRespVO> getOqc(@RequestParam("id") Long id) {
        MesQcOqcDO oqc = oqcService.getOqc(id);
        return success(buildOqcRespVOList(Collections.singletonList(oqc)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出货检验单分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:query')")
    public CommonResult<PageResult<MesQcOqcRespVO>> getOqcPage(@Valid MesQcOqcPageReqVO pageReqVO) {
        PageResult<MesQcOqcDO> pageResult = oqcService.getOqcPage(pageReqVO);
        return success(new PageResult<>(buildOqcRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出货检验单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOqcExcel(@Valid MesQcOqcPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesQcOqcDO> list = oqcService.getOqcPage(pageReqVO).getList();
        List<MesQcOqcRespVO> voList = buildOqcRespVOList(list);
        ExcelUtils.write(response, "出货检验单.xls", "数据", MesQcOqcRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesQcOqcRespVO> buildOqcRespVOList(List<MesQcOqcDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询客户
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesQcOqcDO::getClientId));
        // 批量查询物料
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesQcOqcDO::getItemId));
        // 批量查询计量单位
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 批量查询检测人员
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesQcOqcDO::getInspectorUserId));
        // 拼装 VO
        return BeanUtils.toBean(list, MesQcOqcRespVO.class, vo -> {
            findAndThen(clientMap, vo.getClientId(),
                    client -> vo.setClientNickname(client.getNickname()));
            findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setItemSpecification(item.getSpecification());
                findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unit -> vo.setUnitName(unit.getName()));
            });
            findAndThen(userMap, vo.getInspectorUserId(),
                    user -> vo.setInspectorNickname(user.getNickname()));
        });
    }

}
