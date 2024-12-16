package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.Greeting;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.pf4j.Extension;
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
public class HttpPlugin extends Plugin {

    private HttpServer server;

    public HttpPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("HttpPlugin.start()");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            log.info("HttpPlugin in DEVELOPMENT mode");
        }
        startHttpServer();
    }

    @Override
    public void stop() {
        log.info("HttpPlugin.stop()");
        stopHttpServer();
    }

    private void startHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(9081), 0);
            server.createContext("/", exchange -> {
                String response = "Welcome to PF4J HTTP Server";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            log.info("HTTP server started on port 9081");
        } catch (IOException e) {
            log.error("Error starting HTTP server", e);
        }
    }

    private void stopHttpServer() {
        if (server != null) {
            server.stop(0);
            log.info("HTTP server stopped");
        }
    }

    @Extension
    public static class WelcomeGreeting implements Greeting {

        @Override
        public String getGreeting() {
            return "Welcome to PF4J";
        }

    }

}