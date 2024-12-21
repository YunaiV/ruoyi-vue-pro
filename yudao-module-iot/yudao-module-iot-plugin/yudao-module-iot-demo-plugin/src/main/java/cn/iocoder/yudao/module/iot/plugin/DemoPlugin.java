package cn.iocoder.yudao.module.iot.plugin;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * 一个启动 HTTP 服务器的简单插件。
 */
@Slf4j
public class DemoPlugin extends Plugin {

    private HttpServer server;

    public DemoPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Demo 插件启动");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            log.info("DemoPlugin in DEVELOPMENT mode");
        }
        startDemoServer();
    }

    @Override
    public void stop() {
        log.info("Demo 插件停止");
        stopDemoServer();
    }

    private void startDemoServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(9081), 0);
            server.createContext("/", exchange -> {
                String response = "Hello from DemoPlugin";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            log.info("HTTP 服务器启动成功，端口为 9081");
            log.info("访问地址为 http://127.0.0.1:9081/");
        } catch (IOException e) {
            log.error("HTTP 服务器启动失败", e);
        }
    }

    private void stopDemoServer() {
        if (server != null) {
            server.stop(0);
            log.info("HTTP 服务器停止成功");
        }
    }

//    @Extension
//    public static class WelcomeGreeting implements Greeting {
//
//        @Override
//        public String getGreeting() {
//            return "Welcome to DemoPlugin";
//        }
//
//    }

}