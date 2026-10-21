package cn.iocoder.yudao.module.ai1.enums.mcp;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI1 MCP 传输方式的枚举
 *
 * 取值与 MCP 配置 JSON 中的 transport 字段一致
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1McpTransportEnum implements ArrayValuable<String> {

    HTTP("http", "远程（Streamable HTTP）"),
    STDIO("stdio", "本地进程（stdio）");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Ai1McpTransportEnum::getTransport).toArray(String[]::new);

    /**
     * 传输方式
     */
    private final String transport;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isStdio(String transport) {
        return ObjUtil.equal(STDIO.transport, transport);
    }

}
