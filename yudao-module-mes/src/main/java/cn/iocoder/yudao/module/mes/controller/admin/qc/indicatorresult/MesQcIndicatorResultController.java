package cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcLineDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import cn.iocoder.yudao.module.mes.service.qc.indicatorresult.MesQcIndicatorResultService;
import cn.iocoder.yudao.module.mes.service.qc.ipqc.MesQcIpqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.rqc.MesQcRqcLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 检验结果")
@RestController
@RequestMapping("/mes/qc/indicator-result")
@Validated
public class MesQcIndicatorResultController {

    @Resource
    private MesQcIndicatorResultService resultService;
    @Resource
    private MesQcIqcLineService iqcLineService;
    @Resource
    private MesQcIpqcLineService ipqcLineService;
    @Resource
    private MesQcOqcLineService oqcLineService;
    @Resource
    private MesQcRqcLineService rqcLineService;
    @Resource
    private MesQcIndicatorService indicatorService;

    @PostMapping("/create")
    @Operation(summary = "创建检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:create')")
    public CommonResult<Long> createIndicatorResult(@Valid @RequestBody MesQcIndicatorResultSaveReqVO createReqVO) {
        return success(resultService.createIndicatorResult(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:update')")
    public CommonResult<Boolean> updateIndicatorResult(@Valid @RequestBody MesQcIndicatorResultSaveReqVO updateReqVO) {
        resultService.updateIndicatorResult(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:delete')")
    public CommonResult<Boolean> deleteIndicatorResult(@RequestParam("id") Long id) {
        resultService.deleteIndicatorResult(id);
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得检验结果明细（含检测项模板）")
    @Parameters({
            @Parameter(name = "id", description = "检验结果编号（编辑时传入；新增时不传）", example = "1024"),
            @Parameter(name = "qcId", description = "质检单ID", required = true, example = "1"),
            @Parameter(name = "qcType", description = "质检类型", required = true, example = "1")
    })
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:query')")
    public CommonResult<MesQcIndicatorResultRespVO> getIndicatorResultDetail(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("qcId") Long qcId,
            @RequestParam("qcType") Integer qcType) {
        MesQcIndicatorResultRespVO respVO;
        List<MesQcIndicatorResultDetailDO> details;
        if (id != null) {
            // 编辑：加载已有结果主表 + 已有明细
            MesQcIndicatorResultDO result = resultService.getIndicatorResult(id);
            respVO = BeanUtils.toBean(result, MesQcIndicatorResultRespVO.class);
            details = resultService.getIndicatorResultDetailListByResultId(id);
        } else {
            // 新增：返回空壳 RespVO，仅填充检测项模板
            respVO = new MesQcIndicatorResultRespVO();
            details = Collections.emptyList();
        }
        respVO.setItems(buildDetailItemList(details, qcId, qcType));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得检验结果分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:query')")
    public CommonResult<PageResult<MesQcIndicatorResultRespVO>> getIndicatorResultPage(@Valid MesQcIndicatorResultPageReqVO pageReqVO) {
        PageResult<MesQcIndicatorResultDO> pageResult = resultService.getIndicatorResultPage(pageReqVO);
        // 前端列表只展示 code、sn、remark，无需关联查询
        PageResult<MesQcIndicatorResultRespVO> voPageResult = BeanUtils.toBean(pageResult, MesQcIndicatorResultRespVO.class);
        return success(voPageResult);
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建明细 Item 列表
     *
     * <p>前端表单仅需：indicatorId、indicatorName、valueType、valueSpecification + 已有结果（id、value、remark）</p>
     *
     * @param details 已有结果明细（空列表表示获取空值模板）
     * @param qcId    质检单 ID
     * @param qcType  质检类型
     */
    private List<MesQcIndicatorResultRespVO.Item> buildDetailItemList(
            List<MesQcIndicatorResultDetailDO> details, Long qcId, Integer qcType) {
        // 1.1 获取检验单行的指标 ID 列表（知道有哪些检测指标）
        List<Long> indicatorIds;
        if (Objects.equals(qcType, MesQcTypeEnum.IQC.getType())) {
            List<MesQcIqcLineDO> lines = iqcLineService.getIqcLineListByIqcId(qcId);
            indicatorIds = CollUtil.isEmpty(lines) ? Collections.emptyList()
                    : new ArrayList<>(convertSet(lines, MesQcIqcLineDO::getIndicatorId));
        } else if (Objects.equals(qcType, MesQcTypeEnum.IPQC.getType())) {
            List<MesQcIpqcLineDO> lines = ipqcLineService.getIpqcLineListByIpqcId(qcId);
            indicatorIds = CollUtil.isEmpty(lines) ? Collections.emptyList()
                    : new ArrayList<>(convertSet(lines, MesQcIpqcLineDO::getIndicatorId));
        } else if (Objects.equals(qcType, MesQcTypeEnum.OQC.getType())) {
            List<MesQcOqcLineDO> lines = oqcLineService.getOqcLineListByOqcId(qcId);
            indicatorIds = CollUtil.isEmpty(lines) ? Collections.emptyList()
                    : new ArrayList<>(convertSet(lines, MesQcOqcLineDO::getIndicatorId));
        } else if (Objects.equals(qcType, MesQcTypeEnum.RQC.getType())) {
            List<MesQcRqcLineDO> lines = rqcLineService.getRqcLineListByRqcId(qcId);
            indicatorIds = CollUtil.isEmpty(lines) ? Collections.emptyList()
                    : new ArrayList<>(convertSet(lines, MesQcRqcLineDO::getIndicatorId));
        } else {
            throw new IllegalArgumentException("暂不支持 qcType=" + qcType);
        }
        if (CollUtil.isEmpty(indicatorIds)) {
            return Collections.emptyList();
        }

        // 1.2 批量查询检测指标
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(new HashSet<>(indicatorIds));
        // 1.3 构建已有明细 Map（按 indicatorId 索引）
        Map<Long, MesQcIndicatorResultDetailDO> detailMap = CollUtil.isEmpty(details)
                ? Collections.emptyMap()
                : convertMap(details, MesQcIndicatorResultDetailDO::getIndicatorId);

        // 2. 遍历指标 ID，组装 VO
        List<MesQcIndicatorResultRespVO.Item> voList = new ArrayList<>(indicatorIds.size());
        for (Long indicatorId : indicatorIds) {
            MesQcIndicatorResultRespVO.Item vo = new MesQcIndicatorResultRespVO.Item()
                    .setIndicatorId(indicatorId);
            // 来自 indicator
            findAndThen(indicatorMap, indicatorId, indicator -> vo
                    .setIndicatorName(indicator.getName())
                    .setValueType(indicator.getResultType())
                    .setValueSpecification(indicator.getResultSpecification()));
            // 来自已有结果明细（如有）
            findAndThen(detailMap, indicatorId, detail -> vo
                    .setId(detail.getId()).setResultId(detail.getResultId())
                    .setValue(detail.getValue()).setRemark(detail.getRemark()));
            voList.add(vo);
        }
        return voList;
    }

}
