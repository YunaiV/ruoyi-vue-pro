package cn.iocoder.yudao.module.infra.controller.admin.job;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogExcelVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogRespVO;
import cn.iocoder.yudao.module.infra.convert.job.InfJobLogConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.InfJobLogDO;
import cn.iocoder.yudao.module.infra.service.job.InfJobLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "定时任务日志")
@RestController
@RequestMapping("/infra/job-log")
@Validated
public class InfJobLogController {

    @Resource
    private InfJobLogService jobLogService;

    @GetMapping("/get")
    @ApiOperation("获得定时任务日志")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<InfJobLogRespVO> getJobLog(@RequestParam("id") Long id) {
        InfJobLogDO jobLog = jobLogService.getJobLog(id);
        return success(InfJobLogConvert.INSTANCE.convert(jobLog));
    }

    @GetMapping("/list")
    @ApiOperation("获得定时任务日志列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<InfJobLogRespVO>> getJobLogList(@RequestParam("ids") Collection<Long> ids) {
        List<InfJobLogDO> list = jobLogService.getJobLogList(ids);
        return success(InfJobLogConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得定时任务日志分页")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<PageResult<InfJobLogRespVO>> getJobLogPage(@Valid InfJobLogPageReqVO pageVO) {
        PageResult<InfJobLogDO> pageResult = jobLogService.getJobLogPage(pageVO);
        return success(InfJobLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出定时任务日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:job:export')")
    @OperateLog(type = EXPORT)
    public void exportJobLogExcel(@Valid InfJobLogExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<InfJobLogDO> list = jobLogService.getJobLogList(exportReqVO);
        // 导出 Excel
        List<InfJobLogExcelVO> datas = InfJobLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "任务日志.xls", "数据", InfJobLogExcelVO.class, datas);
    }

}
