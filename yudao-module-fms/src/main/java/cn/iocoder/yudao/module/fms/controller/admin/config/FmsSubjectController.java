package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectDeleteReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectStatusReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectUsageRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 会计科目")
@RestController
@RequestMapping("/fms/config/subject")
@Validated
public class FmsSubjectController {

    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建科目")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:create')")
    public CommonResult<Long> createSubject(@Valid @RequestBody FmsSubjectSaveReqVO createReqVO) {
        return success(subjectService.createSubject(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新科目")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:update')")
    public CommonResult<Boolean> updateSubject(@Valid @RequestBody FmsSubjectSaveReqVO updateReqVO) {
        subjectService.updateSubject(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "删除科目")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:delete')")
    public CommonResult<Boolean> deleteSubjectList(@Valid @RequestBody FmsSubjectDeleteReqVO deleteReqVO) {
        subjectService.deleteSubjectList(deleteReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新科目状态")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:update')")
    public CommonResult<Boolean> updateSubjectStatus(@Valid @RequestBody FmsSubjectStatusReqVO statusReqVO) {
        subjectService.updateSubjectStatus(statusReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得科目")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "科目编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:subject:query')")
    public CommonResult<FmsSubjectRespVO> getSubject(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                     @RequestParam("id") @NotNull Long id) {
        FmsSubjectDO subject = subjectService.getSubject(accountSetId, id, getLoginUserId());
        return success(buildSubjectRespVO(subject));
    }

    @GetMapping("/get-usage")
    @Operation(summary = "获得科目使用情况")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "科目编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:subject:query')")
    public CommonResult<FmsSubjectUsageRespVO> getSubjectUsage(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("id") @NotNull Long id) {
        return success(subjectService.getSubjectUsage(accountSetId, id, getLoginUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得科目列表")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:query')")
    public CommonResult<List<FmsSubjectRespVO>> getSubjectList(@Valid FmsSubjectListReqVO listReqVO) {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO, getLoginUserId());
        return success(buildSubjectRespVOList(subjects));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得科目精简列表")
    public CommonResult<List<FmsSubjectRespVO>> getSubjectSimpleList(@Valid FmsSubjectListReqVO listReqVO) {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO, getLoginUserId());
        return success(convertList(subjects, subject -> new FmsSubjectRespVO()
                .setId(subject.getId()).setParentId(subject.getParentId())
                .setCode(subject.getCode()).setName(subject.getName()).setType(subject.getType())
                .setBalanceDirection(subject.getBalanceDirection()).setStatus(subject.getStatus())
                .setLevel(subject.getLevel()).setAuxiliaryTypeIds(subject.getAuxiliaryTypeIds())
                .setQuantityAccounting(subject.getQuantityAccounting()).setQuantityUnit(subject.getQuantityUnit())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出科目")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportSubject(@Valid FmsSubjectListReqVO listReqVO,
            HttpServletResponse response) throws IOException {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO, getLoginUserId());
        ExcelUtils.write(response, "科目设置.xlsx", "科目设置", FmsSubjectRespVO.class,
                buildSubjectRespVOList(subjects));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得科目导入模板")
    @PreAuthorize("@ss.hasPermission('fms:config:subject:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getSubjectImportTemplate(HttpServletResponse response) throws IOException {
        List<FmsSubjectImportExcelVO> list = Arrays.asList(
                FmsSubjectImportExcelVO.builder().code("1001").name("库存现金")
                        .parentSubjectCode("0").balanceDirection("借").categoryName("流动资产").build(),
                FmsSubjectImportExcelVO.builder().code("100101").name("人民币现金")
                        .parentSubjectCode("1001").balanceDirection("借").categoryName("流动资产")
                        .auxiliaryNames("部门/职员").build());
        ExcelUtils.write(response, "科目导入模板.xlsx", "科目列表", FmsSubjectImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入科目")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('fms:config:subject:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<FmsSubjectImportRespVO> importSubject(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("file") MultipartFile file) throws IOException {
        List<FmsSubjectImportExcelVO> importSubjects = ExcelUtils.read(file, FmsSubjectImportExcelVO.class);
        return success(subjectService.importSubjectList(accountSetId, importSubjects, getLoginUserId()));
    }

    // ==================== 拼接 VO ====================

    private List<FmsSubjectRespVO> buildSubjectRespVOList(List<FmsSubjectDO> subjects) {
        Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap = auxiliaryTypeService.getAuxiliaryTypeMap(
                convertSetByFlatMap(subjects, FmsSubjectDO::getAuxiliaryTypeIds, List::stream));
        return convertList(subjects, subject -> {
            FmsSubjectRespVO subjectVO = BeanUtils.toBean(subject, FmsSubjectRespVO.class)
                    .setCategoryDictValue(subject.getType() + "-" + subject.getCategory());
            subjectVO.setAuxiliaryTypeNames(convertList(subject.getAuxiliaryTypeIds(),
                    auxiliaryTypeId -> auxiliaryTypeMap.get(auxiliaryTypeId).getName()));
            if (Boolean.FALSE.equals(subject.getQuantityAccounting())) {
                subjectVO.setQuantityUnit("");
            }
            return subjectVO;
        });
    }

    private FmsSubjectRespVO buildSubjectRespVO(FmsSubjectDO subject) {
        return subject == null ? null
                : CollUtil.getFirst(buildSubjectRespVOList(Collections.singletonList(subject)));
    }

}
