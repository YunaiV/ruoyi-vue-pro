package cn.iocoder.yudao.module.hrm.controller.admin.insurance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.HrmInsuranceMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.HrmInsuranceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 月度社保表")
@RestController
@RequestMapping("/hrm/insurance/month-record")
@Validated
public class HrmInsuranceMonthRecordController {

    @Resource
    private HrmInsuranceMonthRecordService insuranceMonthRecordService;

    @PostMapping("/create-first")
    @Operation(summary = "创建首月社保表")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:create')")
    public CommonResult<Long> createFirstMonthRecord(
            @Valid @RequestBody HrmInsuranceMonthRecordCreateReqVO reqVO) {
        return success(insuranceMonthRecordService.createFirstMonthRecord(reqVO));
    }

    @PostMapping("/create-next")
    @Operation(summary = "创建下月社保表")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:create')")
    public CommonResult<Long> createNextMonthRecord() {
        return success(insuranceMonthRecordService.createNextMonthRecord());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除月度社保表")
    @Parameter(name = "id", description = "社保表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:delete')")
    public CommonResult<Boolean> deleteMonthRecord(@RequestParam("id") Long id) {
        insuranceMonthRecordService.deleteMonthRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得月度社保表")
    @Parameter(name = "id", description = "月度社保表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<HrmInsuranceMonthRecordRespVO> getMonthRecord(@RequestParam("id") Long id) {
        HrmInsuranceMonthRecordDO monthRecord = insuranceMonthRecordService.getMonthRecord(id);
        return success(BeanUtils.toBean(monthRecord, HrmInsuranceMonthRecordRespVO.class));
    }

    @GetMapping("/last")
    @Operation(summary = "获得最近月度社保表")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<HrmInsuranceMonthRecordRespVO> getLastMonthRecord() {
        HrmInsuranceMonthRecordDO monthRecord = insuranceMonthRecordService.getLastMonthRecord();
        return success(BeanUtils.toBean(monthRecord, HrmInsuranceMonthRecordRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得月度社保表列表")
    @Parameter(name = "year", description = "年份", example = "2026")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<List<HrmInsuranceMonthRecordRespVO>> getMonthRecordList(
            @RequestParam(value = "year", required = false) Integer year) {
        List<HrmInsuranceMonthRecordDO> monthRecords = insuranceMonthRecordService.getMonthRecordList(year);
        return success(BeanUtils.toBean(monthRecords, HrmInsuranceMonthRecordRespVO.class));
    }

}
