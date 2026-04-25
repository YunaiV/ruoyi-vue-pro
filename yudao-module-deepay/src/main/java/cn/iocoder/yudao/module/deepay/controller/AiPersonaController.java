package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiPersonaDO;
import cn.iocoder.yudao.module.deepay.service.AiPersonaService;
import cn.iocoder.yudao.module.deepay.service.AiRateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI Persona（角色/提示词）管理接口。
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>GET  /deepay/persona/list              — 查询所有 persona</li>
 *   <li>GET  /deepay/persona/preview?module=xx — 预览指定模块的 system prompt</li>
 *   <li>POST /deepay/persona/create            — 新增 persona</li>
 *   <li>PUT  /deepay/persona/update            — 更新 persona</li>
 *   <li>DELETE /deepay/persona/{id}            — 删除 persona</li>
 *   <li>GET  /deepay/persona/quota             — 查询当前用户剩余配额</li>
 * </ul>
 */
@Tag(name = "Deepay - AI Persona 管理")
@RestController
@RequestMapping("/deepay/persona")
@Validated
public class AiPersonaController {

    @Resource private AiPersonaService   aiPersonaService;
    @Resource private AiRateLimitService aiRateLimitService;

    // ====================================================================
    // 查询
    // ====================================================================

    @GetMapping("/list")
    @Operation(summary = "查询所有启用的 persona 配置")
    public CommonResult<List<DeepayAiPersonaDO>> list() {
        return success(aiPersonaService.listAll());
    }

    @GetMapping("/preview")
    @Operation(summary = "预览指定模块的 system prompt（含租户优先级）")
    public CommonResult<Map<String, String>> preview(
            @RequestParam(defaultValue = "selection") String module,
            @RequestParam(defaultValue = "0")         Long   tenantId) {
        String prompt = aiPersonaService.getSystemPrompt(tenantId, module);
        return success(java.util.Map.of(
                "module",       module,
                "tenantId",     String.valueOf(tenantId),
                "systemPrompt", prompt
        ));
    }

    // ====================================================================
    // CRUD
    // ====================================================================

    @PostMapping("/create")
    @Operation(summary = "新增 persona 配置")
    public CommonResult<DeepayAiPersonaDO> create(@RequestBody DeepayAiPersonaDO persona) {
        return success(aiPersonaService.create(persona));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 persona 配置")
    public CommonResult<Boolean> update(@RequestBody DeepayAiPersonaDO persona) {
        return success(aiPersonaService.update(persona));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 persona 配置（软删除）")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiPersonaService.delete(id));
    }

    // ====================================================================
    // 配额查询
    // ====================================================================

    @GetMapping("/quota")
    @Operation(summary = "查询当前用户 AI 调用剩余配额")
    public CommonResult<Map<String, Object>> quota(
            @RequestParam(required = false)   String userId,
            @RequestParam(defaultValue = "0") Long   tenantId) {
        return success(aiRateLimitService.getRemainingQuota(tenantId, userId));
    }

}
