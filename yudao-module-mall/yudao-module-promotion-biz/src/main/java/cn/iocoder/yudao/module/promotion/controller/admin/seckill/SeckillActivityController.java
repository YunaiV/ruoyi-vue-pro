package cn.iocoder.yudao.module.promotion.controller.admin.seckill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.*;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.service.seckill.seckillactivity.SeckillActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

    @PutMapping("/close")
    @ApiOperation("关闭秒杀活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        seckillActivityService.closeSeckillActivity(id);
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
    public CommonResult<SeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        SeckillActivityDO seckillActivity = seckillActivityService.getSeckillActivity(id);
        if (seckillActivity == null) {
            return success(null);
        }
        List<SeckillProductDO> seckillProducts =  seckillActivityService.getSeckillProductListByActivityId(id);
        return success(SeckillActivityConvert.INSTANCE.convert(seckillActivity,seckillProducts));
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

}
