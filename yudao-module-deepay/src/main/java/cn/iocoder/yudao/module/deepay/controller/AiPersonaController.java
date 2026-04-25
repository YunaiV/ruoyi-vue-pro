package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.dal.dataobject.AiPersonaDO;
import cn.iocoder.yudao.module.deepay.service.AiPersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * AI 角色人设配置管理接口（运营后台）。
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>GET  /deepay/ai/persona        — 获取所有启用的 persona 列表</li>
 *   <li>GET  /deepay/ai/persona/{id}   — 获取单个 persona</li>
 *   <li>POST /deepay/ai/persona        — 新增 persona</li>
 *   <li>PUT  /deepay/ai/persona/{id}   — 更新 persona</li>
 *   <li>DELETE /deepay/ai/persona/{id} — 删除 persona</li>
 * </ul>
 */
@Tag(name = "Deepay - AI 角色人设配置管理")
@RestController
@RequestMapping("/deepay/ai/persona")
@Validated
@Slf4j
public class AiPersonaController {

    @Resource
    private AiPersonaService aiPersonaService;

    /**
     * 获取所有启用的 persona 列表。
     */
    @GetMapping
    @Operation(summary = "获取所有启用的 Persona 列表")
    public ResponseEntity<Map<String, Object>> list() {
        List<AiPersonaDO> list = aiPersonaService.listAll();
        return ResponseEntity.ok(success(list));
    }

    /**
     * 获取单个 persona。
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取单个 Persona")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        AiPersonaDO persona = aiPersonaService.getById(id);
        if (persona == null) {
            return ResponseEntity.ok(error(404, "Persona 不存在"));
        }
        return ResponseEntity.ok(success(persona));
    }

    /**
     * 新增 persona。
     */
    @PostMapping
    @Operation(summary = "新增 Persona")
    public ResponseEntity<Map<String, Object>> create(@RequestBody AiPersonaDO persona) {
        AiPersonaDO created = aiPersonaService.create(persona);
        log.info("[AiPersonaController] 新增 persona id={} module={}", created.getId(), created.getModule());
        return ResponseEntity.ok(success(created));
    }

    /**
     * 更新 persona。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新 Persona")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @RequestBody AiPersonaDO persona) {
        persona.setId(id);
        aiPersonaService.update(persona);
        log.info("[AiPersonaController] 更新 persona id={}", id);
        return ResponseEntity.ok(success(null));
    }

    /**
     * 删除 persona（物理删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除 Persona")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        aiPersonaService.delete(id);
        log.info("[AiPersonaController] 删除 persona id={}", id);
        return ResponseEntity.ok(success(null));
    }

    // ── helpers ────────────────────────────────────────────────

    private Map<String, Object> success(Object data) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", 0);
        m.put("data", data);
        return m;
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("message", message);
        return m;
    }
}
