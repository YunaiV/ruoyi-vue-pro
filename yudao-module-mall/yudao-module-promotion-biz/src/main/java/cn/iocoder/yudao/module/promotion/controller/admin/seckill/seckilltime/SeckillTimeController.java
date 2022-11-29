package cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime.vo.SeckillTimeCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime.vo.SeckillTimeRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckilltime.vo.SeckillTimeUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckilltime.SeckillTimeConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.module.promotion.service.seckill.seckilltime.SeckillTimeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 秒杀时段")
@RestController
@RequestMapping("/promotion/seckill-time")
@Validated
public class SeckillTimeController {

    @Resource
    private SeckillTimeService seckillTimeService;

    @PostMapping("/create")
    @ApiOperation("创建秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:create')")
    public CommonResult<Long> createSeckillTime(@Valid @RequestBody SeckillTimeCreateReqVO createReqVO) {
        return success(seckillTimeService.createSeckillTime(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:update')")
    public CommonResult<Boolean> updateSeckillTime(@Valid @RequestBody SeckillTimeUpdateReqVO updateReqVO) {
        seckillTimeService.updateSeckillTime(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除秒杀时段")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:delete')")
    public CommonResult<Boolean> deleteSeckillTime(@RequestParam("id") Long id) {
        seckillTimeService.deleteSeckillTime(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得秒杀时段")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:query')")
    public CommonResult<SeckillTimeRespVO> getSeckillTime(@RequestParam("id") Long id) {
        SeckillTimeDO seckillTime = seckillTimeService.getSeckillTime(id);
        return success(SeckillTimeConvert.INSTANCE.convert(seckillTime));
    }

    @GetMapping("/list")
    @ApiOperation("获得所有秒杀时段列表")
//    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:query')")
    public CommonResult<List<SeckillTimeRespVO>> getSeckillTimeList() {
        List<SeckillTimeDO> list = seckillTimeService.getSeckillTimeList();
        return success(SeckillTimeConvert.INSTANCE.convertList(list));
    }

//    @GetMapping("/page")
//    @ApiOperation("获得秒杀时段分页")
//    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:query')")
//    public CommonResult<PageResult<SeckillTimeRespVO>> getSeckillTimePage(@Valid SeckillTimePageReqVO pageVO) {
//        PageResult<SeckillTimeDO> pageResult = seckillTimeService.getSeckillTimePage(pageVO);
//        return success(SeckillTimeConvert.INSTANCE.convertPage(pageResult));
//    }

}
