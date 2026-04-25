package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiMemoryItemDO;
import cn.iocoder.yudao.module.deepay.service.AiMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 长久记忆管理接口（用户可控 + 合规删除）。
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>GET    /deepay/memory/list   — 查询用户记忆列表</li>
 *   <li>POST   /deepay/memory/save   — 手动写入记忆</li>
 *   <li>DELETE /deepay/memory/clear  — 合规删除用户所有记忆</li>
 *   <li>GET    /deepay/memory/allowed-keys — 查询各板块允许记忆的字段白名单</li>
 * </ul>
 */
@Tag(name = "Deepay - AI 长久记忆管理")
@RestController
@RequestMapping("/deepay/memory")
@Validated
public class AiMemoryController {

    @Resource private AiMemoryService aiMemoryService;

    @GetMapping("/list")
    @Operation(summary = "查询用户记忆列表（按板块）")
    public CommonResult<List<DeepayAiMemoryItemDO>> list(
            @RequestParam               String customerId,
            @RequestParam(required = false, defaultValue = "") String module,
            @RequestParam(defaultValue = "0") Long tenantId) {
        List<DeepayAiMemoryItemDO> items = module.isEmpty()
                ? aiMemoryService.listAllMemory(tenantId, customerId)
                : aiMemoryService.listMemory(tenantId, customerId, module);
        return success(items);
    }

    @PostMapping("/save")
    @Operation(summary = "手动写入/更新一条记忆")
    public CommonResult<Boolean> save(@RequestBody SaveMemoryReqVO req) {
        aiMemoryService.saveMemory(
                req.getTenantId() != null ? req.getTenantId() : 0L,
                req.getCustomerId(),
                req.getModule(),
                req.getMemoryType(),
                req.getMemKey(),
                req.getMemValue(),
                req.getConfidence() != null ? req.getConfidence() : 0.9,
                req.getSourceSessionId()
        );
        return success(true);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "合规删除用户所有记忆（GDPR）")
    public CommonResult<Boolean> clear(
            @RequestParam               String customerId,
            @RequestParam(defaultValue = "0") Long tenantId) {
        aiMemoryService.deleteAllMemory(tenantId, customerId);
        return success(true);
    }

    @GetMapping("/allowed-keys")
    @Operation(summary = "查询各板块允许记忆的字段白名单")
    public CommonResult<Map<String, java.util.Set<String>>> allowedKeys() {
        return success(aiMemoryService.getAllowedKeys());
    }

    public static class SaveMemoryReqVO {
        private Long   tenantId;
        private String customerId;
        private String module;
        private String memoryType;
        private String memKey;
        private String memValue;
        private Double confidence;
        private String sourceSessionId;

        public Long   getTenantId()        { return tenantId; }
        public void   setTenantId(Long v)         { this.tenantId = v; }
        public String getCustomerId()      { return customerId; }
        public void   setCustomerId(String v)     { this.customerId = v; }
        public String getModule()          { return module; }
        public void   setModule(String v)         { this.module = v; }
        public String getMemoryType()      { return memoryType; }
        public void   setMemoryType(String v)     { this.memoryType = v; }
        public String getMemKey()          { return memKey; }
        public void   setMemKey(String v)         { this.memKey = v; }
        public String getMemValue()        { return memValue; }
        public void   setMemValue(String v)       { this.memValue = v; }
        public Double getConfidence()      { return confidence; }
        public void   setConfidence(Double v)     { this.confidence = v; }
        public String getSourceSessionId() { return sourceSessionId; }
        public void   setSourceSessionId(String v){ this.sourceSessionId = v; }
    }
}
