package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import cn.iocoder.yudao.module.mes.service.wm.outsourceissue.MesWmOutsourceIssueLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * MES 外协发料单行 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - MES 外协发料单行")
@RestController
@RequestMapping("/mes/wm/outsource-issue-line")
@Validated
public class MesWmOutsourceIssueLineController {

    @Resource
    private MesWmOutsourceIssueLineService outsourceIssueLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private MesWmBatchService batchService;

    @PostMapping("/create")
    @Operation(summary = "创建外协发料单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:create')")
    public CommonResult<Long> createOutsourceIssueLine(@Valid @RequestBody MesWmOutsourceIssueLineSaveReqVO createReqVO) {
        return success(outsourceIssueLineService.createOutsourceIssueLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改外协发料单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:update')")
    public CommonResult<Boolean> updateOutsourceIssueLine(@Valid @RequestBody MesWmOutsourceIssueLineSaveReqVO updateReqVO) {
        outsourceIssueLineService.updateOutsourceIssueLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外协发料单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:delete')")
    public CommonResult<Boolean> deleteOutsourceIssueLine(@RequestParam("id") Long id) {
        outsourceIssueLineService.deleteOutsourceIssueLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得外协发料单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:query')")
    public CommonResult<MesWmOutsourceIssueLineRespVO> getOutsourceIssueLine(@RequestParam("id") Long id) {
        MesWmOutsourceIssueLineDO line = outsourceIssueLineService.getOutsourceIssueLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得外协发料单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:query')")
    public CommonResult<PageResult<MesWmOutsourceIssueLineRespVO>> getOutsourceIssueLinePage(
            @Valid MesWmOutsourceIssueLinePageReqVO pageReqVO) {
        PageResult<MesWmOutsourceIssueLineDO> pageResult = outsourceIssueLineService.getOutsourceIssueLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list-by-issue-id")
    @Operation(summary = "获得外协发料单行列表")
    @Parameter(name = "issueId", description = "发料单ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-issue:query')")
    public CommonResult<List<MesWmOutsourceIssueLineRespVO>> getOutsourceIssueLineListByIssueId(
            @RequestParam("issueId") Long issueId) {
        List<MesWmOutsourceIssueLineDO> list = outsourceIssueLineService.getOutsourceIssueLineListByIssueId(issueId);
        return success(buildRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmOutsourceIssueLineRespVO> buildRespVOList(List<MesWmOutsourceIssueLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmOutsourceIssueLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 1.2 获得批次数据
        Set<Long> batchIds = convertSet(list, MesWmOutsourceIssueLineDO::getBatchId);
        batchIds.remove(null);
        Map<Long, MesWmBatchDO> batchMap = CollUtil.isEmpty(batchIds) ? Collections.emptyMap()
                : convertMap(batchIds, id -> id, id -> batchService.getBatch(id));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmOutsourceIssueLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(batchMap, vo.getBatchId(), batch -> vo.setBatchCode(batch.getCode()));
        });
    }

}
