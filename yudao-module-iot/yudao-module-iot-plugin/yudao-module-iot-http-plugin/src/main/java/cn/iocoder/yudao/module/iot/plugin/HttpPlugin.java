package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.Plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class HttpPlugin extends Plugin {

    private static final int PORT = 8092;

    private final ExecutorService executorService;
    private DeviceDataApi deviceDataApi;

    public HttpPlugin(PluginWrapper wrapper) {
        super(wrapper);
        // 创建单线程池
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        log.info("HttpPlugin.start()");

        // 从 ServiceRegistry 中获取主程序暴露的 DeviceDataApi 接口实例
        deviceDataApi = ServiceRegistry.getService(DeviceDataApi.class);
        if (deviceDataApi == null) {
            log.error("未能从 ServiceRegistry 获取 DeviceDataApi 实例，请确保主程序已正确注册！");
            return;
        }

        // 异步启动 Netty 服务器
        executorService.submit(this::startHttpServer);
    }

    @Override
    public void stop() {
        log.info("HttpPlugin.stop()");
        // 停止线程池
        executorService.shutdownNow();
    }

    /**
     * 启动 HTTP 服务
     */
    private void startHttpServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {

                        @Override
                        protected void initChannel(Channel channel) {
                            channel.pipeline().addLast(new HttpServerCodec());
                            channel.pipeline().addLast(new HttpObjectAggregator(65536));
                            // 将从 ServiceRegistry 获取的 deviceDataApi 传入处理器
                            channel.pipeline().addLast(new HttpHandler(deviceDataApi));
                        }

                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(PORT).sync();
            log.info("HTTP 服务器启动成功，端口为: {}", PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("HTTP 服务启动中断", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
