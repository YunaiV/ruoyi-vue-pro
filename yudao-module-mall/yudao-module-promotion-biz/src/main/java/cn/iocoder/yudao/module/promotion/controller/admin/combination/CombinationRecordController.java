package cn.iocoder.yudao.module.promotion.controller.admin.combination;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordReqPageVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordRespVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.Duration;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;

@Tag(name = "管理后台 - 拼团记录")
@RestController
@RequestMapping("/promotion/combination-record")
@Validated
public class CombinationRecordController {

    @Resource
    private CombinationRecordService combinationRecordService;

    // TODO  @puhui999:这个缓存不用做哈；主要管理后台，对性能要求不高；不像前段；
    /**
     * {@link  Map} 缓存，通过它异步刷新 {@link #getCombinationRecordSummary0()} 所要的拼团记录统计数据
     */
    private final LoadingCache<String, Map<String, Long>> combinationRecordSummary = buildAsyncReloadingCache(Duration.ofSeconds(60L),
            new CacheLoader<String, Map<String, Long>>() {

                @Override
                public Map<String, Long> load(String key) {
                    return getCombinationRecordSummary0();
                }

            });

    @GetMapping("/page")
    @Operation(summary = "获得拼团记录分页")
    @PreAuthorize("@ss.hasPermission('promotion:combination-record:query')")
    public CommonResult<PageResult<CombinationRecordRespVO>> getBargainRecordPage(@Valid CombinationRecordReqPageVO pageVO) {
        return success(CombinationActivityConvert.INSTANCE.convert(
                combinationRecordService.getCombinationRecordPage(pageVO)));
    }

    // TODO @puhui999：Map 改成对象，尽量避免 Map 返回结果哈；然后 getCombinationRecordCount、getCombinationRecordsSuccessCount、getRecordsVirtualGroupCount 三个方法，可以合并成一个方法哈，返回这个 vo
    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于拼团记录页面展示")
    @PreAuthorize("@ss.hasPermission('promotion:combination-record:query')")
    public CommonResult<Map<String, Long>> getCombinationRecordSummary() {
        return success(combinationRecordSummary.getUnchecked("")); // 缓存
    }

    private Map<String, Long> getCombinationRecordSummary0() {
        Map<String, Long> hashMap = MapUtil.newHashMap(3); // TODO @puhui999：Maps.newHashMapWithExpectedSize()
        hashMap.put("userCount", combinationRecordService.getCombinationRecordCount()); // 获取所有拼团记录
        hashMap.put("successCount", combinationRecordService.getCombinationRecordsSuccessCount()); // 获取成团记录
        hashMap.put("virtualGroupCount", combinationRecordService.getRecordsVirtualGroupCount()); // 获取虚拟成团记录
        return hashMap;
    }

}
