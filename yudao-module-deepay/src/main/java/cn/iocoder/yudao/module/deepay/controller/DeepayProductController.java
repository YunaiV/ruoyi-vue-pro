package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.orchestrator.ChainOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 商品生成接口。
 *
 * <p>POST /api/create-product —— 接收一句话 prompt，触发完整的
 * 「设计 → 选图 → 链码落库 → 支付 IBAN」流水线，返回可售卖链接。</p>
 */
@Tag(name = "Deepay - 商品生成")
@RestController
@RequestMapping("/api")
@Validated
public class DeepayProductController {

    @Resource
    private ChainOrchestrator chainOrchestrator;

    @PostMapping("/create-product")
    @Operation(summary = "一句话生成商品（含链码+图片+标题+描述+价格+链接）")
    public CommonResult<Map<String, Object>> createProduct(@Valid @RequestBody ReqVO reqVO) {
        Context ctx = chainOrchestrator.run(reqVO.getPrompt());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode", ctx.chainCode);
        resp.put("image", ctx.selectedImage);
        resp.put("title", ctx.title);
        resp.put("description", ctx.description);
        resp.put("price", ctx.price);
        resp.put("link", ctx.link);
        return success(resp);
    }

    // -------------------------------- 内部请求 VO --------------------------------

    /** 创建商品请求体 */
    public static class ReqVO {

        @NotBlank(message = "prompt 不能为空")
        private String prompt;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

}
