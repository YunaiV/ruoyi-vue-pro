package cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo.MesQcDefectRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo.MesQcDefectRecordRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo.MesQcDefectRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 质检缺陷记录")
@RestController
@RequestMapping("/mes/qc/defect-record")
@Validated
public class MesQcDefectRecordController {

    @Resource
    private MesQcDefectRecordService defectRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建质检缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:create')")
    public CommonResult<Long> createDefectRecord(@Valid @RequestBody MesQcDefectRecordSaveReqVO createReqVO) {
        return success(defectRecordService.createDefectRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新质检缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:update')")
    public CommonResult<Boolean> updateDefectRecord(@Valid @RequestBody MesQcDefectRecordSaveReqVO updateReqVO) {
        defectRecordService.updateDefectRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除质检缺陷记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:update')")
    public CommonResult<Boolean> deleteDefectRecord(@RequestParam("id") Long id) {
        defectRecordService.deleteDefectRecord(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得质检缺陷记录分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:query')")
    public CommonResult<PageResult<MesQcDefectRecordRespVO>> getDefectRecordPage(@Valid MesQcDefectRecordPageReqVO pageReqVO) {
        PageResult<MesQcDefectRecordDO> pageResult = defectRecordService.getDefectRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesQcDefectRecordRespVO.class));
    }

}
