package cn.iocoder.yudao.module.ai1.harness.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Disabled;
import org.redisson.Redisson;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamReadArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Stream 阻塞读的集成测试
 *
 * 默认禁用，手动测试时移除 @Disabled，连接本机 127.0.0.1:6379
 *
 * @author 芋道源码
 */
@Disabled
public class Ai1RedisBlockingReadIntegrationTest {

    @Test
    @Timeout(40)
    public void testIdleBlockingReads() {
        // 准备参数
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 断言默认命令超时
        assertEquals(3000, config.useSingleServer().getTimeout());
        RedissonClient client = Redisson.create(config);
        RStream<String, String> tasks = client.getStream("ai1:p3-test:tasks:" + UUID.randomUUID(), StringCodec.INSTANCE);
        RStream<String, String> results = client.getStream("ai1:p3-test:results:" + UUID.randomUUID(), StringCodec.INSTANCE);
        try {
            // mock 数据
            tasks.createGroup(StreamCreateGroupArgs.name("test-workers").makeStream());
            for (int i = 0; i < 2; i++) {
                // 调用，并断言 worker 空闲阻塞读
                long start = System.nanoTime();
                assertTrue(tasks.readGroup("test-workers", "test-consumer", StreamReadGroupArgs.neverDelivered()
                        .count(1).timeout(Duration.ofSeconds(5))).isEmpty());
                long workerMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
                assertTrue(workerMillis >= 4500, "worker 未等待 5 秒：" + workerMillis);
                // 调用，并断言 SSE 空闲阻塞读
                start = System.nanoTime();
                assertTrue(results.read(StreamReadArgs.greaterThan(new StreamMessageId(0, 0))
                        .count(64).timeout(Duration.ofSeconds(5))).isEmpty());
                long sseMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
                assertTrue(sseMillis >= 4500, "SSE 未等待 5 秒：" + sseMillis);
                System.out.println("SESS-22 round=" + i + " workerMs=" + workerMillis + " sseMs=" + sseMillis);
            }
        } finally {
            // 清理数据
            tasks.delete();
            results.delete();
            client.shutdown();
        }
    }

}
