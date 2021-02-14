package cn.iocoder.dashboard.modules.infra.controller.job;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.*;
import cn.iocoder.dashboard.modules.infra.convert.job.InfJobConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.dashboard.modules.infra.service.job.InfJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "定时任务")
@RestController
@RequestMapping("/infra/job")
@Validated
public class InfJobController {

    @Resource
    private InfJobService jobService;

    @PostMapping("/create")
    @ApiOperation("创建定时任务")
    @PreAuthorize("@ss.hasPermission('infra:job:create')")
    public CommonResult<Long> createJob(@Valid @RequestBody InfJobCreateReqVO createReqVO) {
        return success(jobService.createJob(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新定时任务")
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> updateJob(@Valid @RequestBody InfJobUpdateReqVO updateReqVO) {
        jobService.updateJob(updateReqVO);
        return success(true);
    }

	@DeleteMapping("/delete")
    @ApiOperation("删除定时任务")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
	@PreAuthorize("@ss.hasPermission('infra:job:delete')")
    public CommonResult<Boolean> deleteJob(@RequestParam("id") Long id) {
        jobService.deleteJob(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得定时任务")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<InfJobRespVO> getJob(@RequestParam("id") Long id) {
        InfJobDO job = jobService.getJob(id);
        return success(InfJobConvert.INSTANCE.convert(job));
    }

    @GetMapping("/list")
    @ApiOperation("获得定时任务列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<InfJobRespVO>> getJobList(@RequestParam("ids") Collection<Long> ids) {
        List<InfJobDO> list = jobService.getJobList(ids);
        return success(InfJobConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得定时任务分页")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<PageResult<InfJobRespVO>> getJobPage(@Valid InfJobPageReqVO pageVO) {
        PageResult<InfJobDO> pageResult = jobService.getJobPage(pageVO);
        return success(InfJobConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出定时任务 Excel")
    @PreAuthorize("@ss.hasPermission('infra:job:export')")
    @OperateLog(type = EXPORT)
    public void exportJobExcel(@Valid InfJobExportReqVO exportReqVO,
                               HttpServletResponse response) throws IOException {
        List<InfJobDO> list = jobService.getJobList(exportReqVO);
        // 导出 Excel
        List<InfJobExcelVO> datas = InfJobConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "定时任务.xls", "数据", InfJobExcelVO.class, datas);
    }

}
