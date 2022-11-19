package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.convert.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.service.seckillactivity.SeckillActivityService;

@Api(tags = "管理后台 - 秒杀活动")
@RestController
@RequestMapping("/promotion/seckill-activity")
@Validated
public class SeckillActivityController {

    @Resource
    private SeckillActivityService seckillActivityService;

    @PostMapping("/create")
    @ApiOperation("创建秒杀活动")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:create')")
    public CommonResult<Long> createSeckillActivity(@Valid @RequestBody SeckillActivityCreateReqVO createReqVO) {
        return success(seckillActivityService.createSeckillActivity(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新秒杀活动")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:update')")
    public CommonResult<Boolean> updateSeckillActivity(@Valid @RequestBody SeckillActivityUpdateReqVO updateReqVO) {
        seckillActivityService.updateSeckillActivity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除秒杀活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:delete')")
    public CommonResult<Boolean> deleteSeckillActivity(@RequestParam("id") Long id) {
        seckillActivityService.deleteSeckillActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得秒杀活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:query')")
    public CommonResult<SeckillActivityRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        SeckillActivityDO seckillActivity = seckillActivityService.getSeckillActivity(id);
        return success(SeckillActivityConvert.INSTANCE.convert(seckillActivity));
    }

    @GetMapping("/list")
    @ApiOperation("获得秒杀活动列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:query')")
    public CommonResult<List<SeckillActivityRespVO>> getSeckillActivityList(@RequestParam("ids") Collection<Long> ids) {
        List<SeckillActivityDO> list = seckillActivityService.getSeckillActivityList(ids);
        return success(SeckillActivityConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得秒杀活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:query')")
    public CommonResult<PageResult<SeckillActivityRespVO>> getSeckillActivityPage(@Valid SeckillActivityPageReqVO pageVO) {
        PageResult<SeckillActivityDO> pageResult = seckillActivityService.getSeckillActivityPage(pageVO);
        return success(SeckillActivityConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出秒杀活动 Excel")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:export')")
    @OperateLog(type = EXPORT)
    public void exportSeckillActivityExcel(@Valid SeckillActivityExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SeckillActivityDO> list = seckillActivityService.getSeckillActivityList(exportReqVO);
        // 导出 Excel
        List<SeckillActivityExcelVO> datas = SeckillActivityConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "秒杀活动.xls", "数据", SeckillActivityExcelVO.class, datas);
    }

}
