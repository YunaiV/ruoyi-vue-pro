package cn.iocoder.yudao.module.infra.sentineldemo.controller.admin;

import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - Sentinel 限流熔断示例
 *
 * 1. 每个接口通过 @SentinelResource 定义一个资源，资源名与 sentinel 目录下 JSON 规则中的 resource 一一对应
 * 2. 被限流、熔断时，调用同一个类中的 blockHandler 方法，返回 CommonResult 错误码，与 @RateLimiter 被限流时的返回格式一致
 *
 * 放在 controller.admin 包下，才会和项目其它管理后台接口一样，添加 /admin-api 前缀
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - Sentinel 示例")
@RestController
@RequestMapping("/infra/sentinel-demo")
public class DemoSentinelController {

    @GetMapping("/flow")
    @Operation(summary = "限流示例", description = "资源 demo-sentinel-flow，每秒最多通过 2 个请求")
    @SentinelResource(value = "demo-sentinel-flow", blockHandler = "flowBlockHandler")
    public CommonResult<String> flow() {
        return success("flow");
    }

    @GetMapping("/degrade")
    @Operation(summary = "熔断示例", description = "资源 demo-sentinel-degrade，慢调用比例超过阈值时熔断 10 秒")
    @Parameter(name = "slow", description = "是否模拟慢调用", example = "false")
    @SentinelResource(value = "demo-sentinel-degrade", blockHandler = "degradeBlockHandler",
            exceptionsToIgnore = ServiceException.class) // 业务异常不计入异常比例、异常数的熔断统计，由全局异常处理器照常返回错误码
    public CommonResult<String> degrade(@RequestParam(value = "slow", defaultValue = "false") Boolean slow) {
        if (Boolean.TRUE.equals(slow)) {
            ThreadUtil.sleep(500); // 模拟慢调用：超过熔断规则中 200 毫秒的最大响应时间
            return success("slow");
        }
        return success("ok");
    }

    // ========== blockHandler：被 Sentinel 阻断时调用。参数为原方法的参数 + BlockException，返回值类型与原方法相同 ==========

    public CommonResult<String> flowBlockHandler(BlockException ex) {
        return buildBlockResult(ex);
    }

    public CommonResult<String> degradeBlockHandler(Boolean slow, BlockException ex) {
        return buildBlockResult(ex);
    }

    /**
     * 将 BlockException 转换成 CommonResult：熔断返回 503，限流等其它阻断返回 429
     */
    private static <T> CommonResult<T> buildBlockResult(BlockException ex) {
        if (ex instanceof DegradeException) {
            return CommonResult.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "服务暂时不可用，请稍后重试");
        }
        return CommonResult.error(TOO_MANY_REQUESTS);
    }

}
