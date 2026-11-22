package cn.iocoder.yudao.module.infra.controller.admin.sse;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.sse.config.SseProperties;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import cn.iocoder.yudao.framework.websocket.config.WebSocketProperties;
import cn.iocoder.yudao.module.infra.controller.admin.sse.vo.SseConfigRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.sse.vo.SseDemoSendReqVO;
import cn.iocoder.yudao.module.infra.websocket.message.DemoReceiveMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - SSE 配置与演示
 *
 * 与 starter 中的 {@link cn.iocoder.yudao.framework.sse.core.controller.SseController} 职责不同：
 * 1. starter 的 SseController：裸路径 /infra/sse/connect、/infra/sse/close，负责建立和关闭连接，管理端与用户端共用
 * 2. 本类：/admin-api/infra/sse/get-config、/admin-api/infra/sse/send-demo，提供前端配置与演示消息发送
 */
@Tag(name = "管理后台 - SSE")
@RestController("infraSseController")
@RequestMapping("/infra/sse")
@Validated
public class SseController {

    /**
     * 演示消息的类型
     */
    public static final String DEMO_MESSAGE_TYPE = "demo-sse-test";

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.websocket.enable 配置项，可以关闭 WebSocket 的功能，所以这里只能不强制注入
    private WebSocketProperties webSocketProperties;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.sse.enable 配置项默认关闭 SSE 的功能，所以这里只能不强制注入
    private SseProperties sseProperties;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private SseMessageSender sseMessageSender;

    @GetMapping("/get-config")
    @Operation(summary = "获得 SSE 配置", description = "SSE 页面建立连接使用；通知推送弹窗也通过 websocketEnabled、sseEnabled 判断可选的推送方式")
    public CommonResult<SseConfigRespVO> getSseConfig() {
        SseConfigRespVO respVO = new SseConfigRespVO();
        respVO.setWebsocketEnabled(webSocketProperties != null);
        respVO.setSseEnabled(sseMessageSender != null);
        if (sseProperties != null) {
            respVO.setSsePath(sseProperties.getPath());
            respVO.setSseHeartbeatInterval(sseProperties.getHeartbeatInterval().toMillis());
            respVO.setSseRetryInterval(sseProperties.getRetryInterval().toMillis());
        }
        return success(respVO);
    }

    @PostMapping("/send-demo")
    @Operation(summary = "发送 SSE 演示消息", description = "SSE 没有上行通道，通过该接口发布测试事件")
    @PreAuthorize("@ss.hasPermission('infra:sse:send')")
    public CommonResult<String> sendDemoMessage(@Valid @RequestBody SseDemoSendReqVO reqVO) {
        if (sseMessageSender == null) {
            throw exception0(NOT_IMPLEMENTED.getCode(), "SSE 未开启，请配置 yudao.sse.enable=true");
        }
        // 未指定接收人时，发送给所有管理员
        DemoReceiveMessage content = new DemoReceiveMessage().setFromUserId(getLoginUserId())
                .setText(reqVO.getText()).setSingle(reqVO.getToUserId() != null);
        return success(sseMessageSender.send(null, UserTypeEnum.ADMIN.getValue(), reqVO.getToUserId(),
                DEMO_MESSAGE_TYPE, JsonUtils.toJsonString(content)));
    }

}
