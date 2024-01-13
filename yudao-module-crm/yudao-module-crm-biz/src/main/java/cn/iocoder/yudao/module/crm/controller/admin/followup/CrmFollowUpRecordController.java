package cn.iocoder.yudao.module.crm.controller.admin.followup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.service.followup.CrmFollowUpRecordService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;


@Tag(name = "管理后台 - 跟进记录")
@RestController
@RequestMapping("/crm/follow-up-record")
@Validated
public class CrmFollowUpRecordController {

    @Resource
    private CrmFollowUpRecordService crmFollowUpRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建跟进记录")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:create')")
    public CommonResult<Long> createFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO createReqVO) {
        return success(crmFollowUpRecordService.createFollowUpRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新跟进记录")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:update')")
    public CommonResult<Boolean> updateFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO updateReqVO) {
        crmFollowUpRecordService.updateFollowUpRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除跟进记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:delete')")
    public CommonResult<Boolean> deleteFollowUpRecord(@RequestParam("id") Long id) {
        crmFollowUpRecordService.deleteFollowUpRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得跟进记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<CrmFollowUpRecordRespVO> getFollowUpRecord(@RequestParam("id") Long id) {
        CrmFollowUpRecordDO followUpRecord = crmFollowUpRecordService.getFollowUpRecord(id);
        return success(BeanUtils.toBean(followUpRecord, CrmFollowUpRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得跟进记录分页")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<PageResult<CrmFollowUpRecordRespVO>> getFollowUpRecordPage(@Valid CrmFollowUpRecordPageReqVO pageReqVO) {
        PageResult<CrmFollowUpRecordDO> pageResult = crmFollowUpRecordService.getFollowUpRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CrmFollowUpRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出跟进记录 Excel")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:export')")
    @OperateLog(type = EXPORT)
    public void exportFollowUpRecordExcel(@Valid CrmFollowUpRecordPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CrmFollowUpRecordDO> list = crmFollowUpRecordService.getFollowUpRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "跟进记录.xls", "数据", CrmFollowUpRecordRespVO.class,
                BeanUtils.toBean(list, CrmFollowUpRecordRespVO.class));
    }

}