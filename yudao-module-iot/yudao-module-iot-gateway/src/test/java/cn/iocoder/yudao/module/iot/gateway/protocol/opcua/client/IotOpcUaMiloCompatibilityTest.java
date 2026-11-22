package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;

import io.netty.util.Version;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 芋道 Milo（yudao-milo 1.2.0-RC3-preview-20261123）与芋道 BOM（Netty 4.2、BouncyCastle 1.87）的依赖闸门
 * <p>
 * 安全通道、Read、Subscription 在芋道 BOM 依赖版本下的同进程兼容性，由 {@link IotOpcUaClientProtocolIntegrationTest} 覆盖
 *
 * @author 芋道源码
 */
public class IotOpcUaMiloCompatibilityTest {

    @Test
    public void testDependencyVersions() {
        // 断言：运行时使用的是芋道 BOM 管理的版本，而不是 Milo 构建时的 4.1.138 / 1.86
        String nettyVersion = Version.identify().get("netty-common").artifactVersion();
        assertTrue(nettyVersion.startsWith("4.2."), "netty-common: " + nettyVersion);
        String bcVersion = BouncyCastleProvider.class.getPackage().getImplementationVersion();
        assertTrue(bcVersion == null || bcVersion.startsWith("1.87"), "bcprov: " + bcVersion);
    }

    static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}
