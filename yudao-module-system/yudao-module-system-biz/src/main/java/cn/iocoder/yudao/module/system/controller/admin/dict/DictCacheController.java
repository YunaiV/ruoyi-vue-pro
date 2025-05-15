package cn.iocoder.yudao.module.system.controller.admin.dict;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import java.util.Arrays;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.cache.CaffeineDictCache;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.DictCacheReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 管理后台 - 字典缓存
 */
@Tag(name = "管理后台 - 字典缓存")
@RestController
@RequestMapping("/system/dict-cache")
@Validated
public class DictCacheController {

    @GetMapping("/stats")
    @Operation(summary = "获取字典缓存统计信息")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<String> getDictCacheStats() {
        return success(CaffeineDictCache.getCacheStats());
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空所有字典缓存")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> clearDictCache() {
        CaffeineDictCache.invalidateAll();
        return success(true);
    }

    @DeleteMapping("/type")
    @Operation(summary = "清空指定字典类型的缓存")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> clearDictCacheByType(DictCacheReqVO reqVO) {
        CaffeineDictCache.invalidateCache(reqVO.getDictType(), reqVO.getSql());
        return success(true);
    }

    @DeleteMapping("/types")
    @Operation(summary = "清空多个字典类型的缓存")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> clearDictCacheByTypes(DictCacheReqVO reqVO) {
        CaffeineDictCache.invalidateCaches(Arrays.asList(reqVO.getDictTypes()), reqVO.getSql());
        return success(true);
    }

    @PostMapping("/warmup")
    @Operation(summary = "预热指定字典类型的缓存")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> warmupDictCache(DictCacheReqVO reqVO) {
        CaffeineDictCache.warmup(Arrays.asList(reqVO.getDictTypes()), reqVO.getSql());
        return success(true);
    }
}
