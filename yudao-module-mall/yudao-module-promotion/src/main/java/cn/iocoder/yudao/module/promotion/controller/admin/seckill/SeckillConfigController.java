package cn.iocoder.yudao.module.promotion.controller.admin.seckill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.*;
import cn.iocoder.yudao.module.promotion.convert.seckill.SeckillConfigConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 秒杀时段")
@RestController
@RequestMapping("/promotion/seckill-config")
@Validated
public class SeckillConfigController {

    @Resource
    private SeckillConfigService seckillConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:create')")
    public CommonResult<Long> createSeckillConfig(@Valid @RequestBody SeckillConfigCreateReqVO createReqVO) {
        return success(seckillConfigService.createSeckillConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新秒杀时段")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:update')")
    public CommonResult<Boolean> updateSeckillConfig(@Valid @RequestBody SeckillConfigUpdateReqVO updateReqVO) {
        seckillConfigService.updateSeckillConfig(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改时段配置状态")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:update')")
    public CommonResult<Boolean> updateSeckillConfigStatus(@Valid @RequestBody SeckillConfigUpdateStatusReqVo reqVO) {
        seckillConfigService.updateSeckillConfigStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除秒杀时段")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:delete')")
    public CommonResult<Boolean> deleteSeckillConfig(@RequestParam("id") Long id) {
        seckillConfigService.deleteSeckillConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得秒杀时段")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:query')")
    public CommonResult<SeckillConfigRespVO> getSeckillConfig(@RequestParam("id") Long id) {
        SeckillConfigDO seckillConfig = seckillConfigService.getSeckillConfig(id);
        return success(SeckillConfigConvert.INSTANCE.convert(seckillConfig));
    }

    @GetMapping("/list")
    @Operation(summary = "获得所有秒杀时段列表")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:query')")
    public CommonResult<List<SeckillConfigRespVO>> getSeckillConfigList() {
        List<SeckillConfigDO> list = seckillConfigService.getSeckillConfigList();
        return success(SeckillConfigConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得所有开启状态的秒杀时段精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<SeckillConfigSimpleRespVO>> getSeckillConfigSimpleList() {
        List<SeckillConfigDO> list = seckillConfigService.getSeckillConfigListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(SeckillConfigConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀时间段分页")
    @PreAuthorize("@ss.hasPermission('promotion:seckill-config:query')")
    public CommonResult<PageResult<SeckillConfigRespVO>> getSeckillActivityPage(@Valid SeckillConfigPageReqVO pageVO) {
        PageResult<SeckillConfigDO> pageResult = seckillConfigService.getSeckillConfigPage(pageVO);
        return success(SeckillConfigConvert.INSTANCE.convertPage(pageResult));
    }

}
