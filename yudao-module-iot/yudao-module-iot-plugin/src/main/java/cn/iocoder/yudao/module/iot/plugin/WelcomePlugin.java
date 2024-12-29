/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.Greeting;
import org.apache.commons.lang.StringUtils;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * 打招呼 测试用例
 */
public class WelcomePlugin extends Plugin {

    private HttpServer server;

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("WelcomePlugin.start()");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            System.out.println(StringUtils.upperCase("WelcomePlugin"));
        }
        startHttpServer();
    }

    @Override
    public void stop() {
        System.out.println("WelcomePlugin.stop()");
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
            System.out.println("HTTP server started on port 9081");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopHttpServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP server stopped");
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