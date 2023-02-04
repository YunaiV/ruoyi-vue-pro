package cn.iocoder.yudao.module.promotion.controller.admin.seckill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckilltime.SeckillTimeConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.module.promotion.service.seckill.seckilltime.SeckillTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 秒杀时段")
@RestController
@RequestMapping("/promotion/seckill-time")
@Validated
public class SeckillTimeController {

    @Resource
    private SeckillTimeService seckillTimeService;

    @PostMapping("/create")
    @Operation(summary = "创建秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:create')")
    public CommonResult<Long> createSeckillTime(@Valid @RequestBody SeckillTimeCreateReqVO createReqVO) {
        return success(seckillTimeService.createSeckillTime(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:update')")
    public CommonResult<Boolean> updateSeckillTime(@Valid @RequestBody SeckillTimeUpdateReqVO updateReqVO) {
        seckillTimeService.updateSeckillTime(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除秒杀时段")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:delete')")
    public CommonResult<Boolean> deleteSeckillTime(@RequestParam("id") Long id) {
        seckillTimeService.deleteSeckillTime(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得秒杀时段")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:query')")
    public CommonResult<SeckillTimeRespVO> getSeckillTime(@RequestParam("id") Long id) {
        SeckillTimeDO seckillTime = seckillTimeService.getSeckillTime(id);
        return success(SeckillTimeConvert.INSTANCE.convert(seckillTime));
    }

    @GetMapping("/list")
    @Operation(summary = "获得所有秒杀时段列表")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-time:query')")
    public CommonResult<List<SeckillTimeRespVO>> getSeckillTimeList() {
        List<SeckillTimeDO> list = seckillTimeService.getSeckillTimeList();
        return success(SeckillTimeConvert.INSTANCE.convertList(list));
    }
}
