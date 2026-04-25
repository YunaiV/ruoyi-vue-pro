package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiModelUsageDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiModelUsageRepository;
import cn.iocoder.yudao.module.deepay.service.gateway.GatewayRequest;
import cn.iocoder.yudao.module.deepay.service.gateway.GatewayResponse;
import cn.iocoder.yudao.module.deepay.service.gateway.ModelGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * AI 模型网关接口。
 *
 * <p>提供：统一 chat 入口、用量查询、成本统计。</p>
 */
@Tag(name = "Deepay - AI 模型网关")
@RestController
@RequestMapping("/deepay/gateway")
@Validated
public class ModelGatewayController {

    @Resource private ModelGatewayService  gatewayService;
    @Resource private AiModelUsageRepository usageRepo;

    // =========================================================================
    // 统一 Chat 入口
    // =========================================================================

    @PostMapping("/chat")
    @Operation(summary = "统一模型 Chat 入口（支持 fallback + 用量统计）")
    public GatewayResponse chat(@RequestBody @Validated ChatReq req) {
        GatewayRequest gatewayReq = GatewayRequest.builder()
                .tenantId(req.getTenantId())
                .customerId(req.getCustomerId())
                .sessionId(req.getSessionId())
                .module(req.getModule())
                .model(req.getModel())
                .messages(req.getMessages())
                .temperature(req.getTemperature() > 0 ? req.getTemperature() : 0.7)
                .maxTokens(req.getMaxTokens())
                .build();
        return gatewayService.chat(gatewayReq);
    }

    // =========================================================================
    // 用量统计
    // =========================================================================

    @GetMapping("/usage")
    @Operation(summary = "查询租户模型用量记录")
    public Page<AiModelUsageDocument> listUsage(
            @RequestParam @NotNull Long tenantId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return usageRepo.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(page, size));
    }

    @GetMapping("/usage/customer")
    @Operation(summary = "查询某客户的模型用量记录")
    public Page<AiModelUsageDocument> listUsageByCustomer(
            @RequestParam @NotNull Long tenantId,
            @RequestParam @NotNull Long customerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return usageRepo.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(
                tenantId, customerId, PageRequest.of(page, size));
    }

    // =========================================================================
    // DTO
    // =========================================================================

    @Data
    public static class ChatReq {
        @NotNull  private Long                    tenantId;
        private   Long                            customerId;
        private   String                          sessionId;
        private   String                          module;
        private   String                          model;
        @NotEmpty private List<Map<String, Object>> messages;
        private   double                          temperature;
        private   int                             maxTokens;
    }

}
