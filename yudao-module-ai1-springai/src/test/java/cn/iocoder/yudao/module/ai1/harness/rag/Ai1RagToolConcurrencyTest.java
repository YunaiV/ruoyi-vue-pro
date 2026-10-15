package cn.iocoder.yudao.module.ai1.harness.rag;

import cn.hutool.core.lang.loader.LazyFunLoader;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import io.milvus.client.MilvusServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1RagTool} 的并发单元测试
 *
 * @author 芋道源码
 */
public class Ai1RagToolConcurrencyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private Ai1RagTool ragTool;

    @Mock
    private Ai1ModelService modelService;
    @Mock
    private Ai1LlmModelFactory llmModelFactory;
    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private MilvusServiceClient milvusClient;

    @BeforeEach
    public void before() {
        ReflectionTestUtils.setField(ragTool, "ai1Properties", new YudaoAi1Properties());
        ReflectionTestUtils.setField(ragTool, "milvusClientLoader", LazyFunLoader.on(() -> milvusClient));
        when(modelService.getModelRespBO(any(), any())).thenReturn(new Ai1ModelRespBO()
                .setModelType(Ai1ModelTypeEnum.EMBEDDING.getType()));
        when(llmModelFactory.getOrCreateEmbeddingModel(any())).thenReturn(embeddingModel);
    }

    @Test
    public void testGetOrCreateVectorStore_sameKnowledgeBaseInitializesOnce() throws Exception {
        // mock 数据
        MilvusVectorStore firstStore = mock(MilvusVectorStore.class);
        MilvusVectorStore secondStore = mock(MilvusVectorStore.class);
        CountDownLatch initializing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> secondThread = new AtomicReference<>();
        // mock firstStore 的方法
        doAnswer(invocation -> {
            initializing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return null;
        }).when(firstStore).afterPropertiesSet();
        // 准备参数
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(1L)
                .setEmbeddingProviderId(1L).setEmbeddingModelId(1L);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 调用
            Future<MilvusVectorStore> first = executor.submit(() -> getOrCreateVectorStore(knowledgeBase, firstStore));
            assertTrue(initializing.await(5, TimeUnit.SECONDS));
            Future<MilvusVectorStore> second = executor.submit(() -> {
                secondThread.set(Thread.currentThread());
                return getOrCreateVectorStore(knowledgeBase, secondStore);
            });
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                while (secondThread.get() == null || secondThread.get().getState() != Thread.State.BLOCKED
                        || Arrays.stream(secondThread.get().getStackTrace()).noneMatch(frame ->
                                frame.getClassName().equals(Ai1RagTool.class.getName())
                                        && frame.getMethodName().equals("getOrCreateVectorStore"))) {
                    Thread.sleep(5);
                }
            });
            release.countDown();
            // 断言
            assertSame(firstStore, first.get(5, TimeUnit.SECONDS));
            assertSame(firstStore, second.get(5, TimeUnit.SECONDS));
            verify(firstStore).afterPropertiesSet();
            verifyNoInteractions(secondStore);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            ragTool.destroy();
        }
    }

    @Test
    public void testGetOrCreateVectorStore_differentKnowledgeBasesInitializeIndependently() throws Exception {
        // mock 数据
        MilvusVectorStore firstStore = mock(MilvusVectorStore.class);
        MilvusVectorStore secondStore = mock(MilvusVectorStore.class);
        CountDownLatch initializing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        // mock firstStore 的方法
        doAnswer(invocation -> {
            initializing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return null;
        }).when(firstStore).afterPropertiesSet();
        // 准备参数
        Ai1KnowledgeBaseDO firstBase = new Ai1KnowledgeBaseDO().setId(1L)
                .setEmbeddingProviderId(1L).setEmbeddingModelId(1L);
        Ai1KnowledgeBaseDO secondBase = new Ai1KnowledgeBaseDO().setId(2L)
                .setEmbeddingProviderId(1L).setEmbeddingModelId(1L);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 调用
            Future<MilvusVectorStore> first = executor.submit(() -> getOrCreateVectorStore(firstBase, firstStore));
            assertTrue(initializing.await(5, TimeUnit.SECONDS));
            Future<MilvusVectorStore> second = executor.submit(() -> getOrCreateVectorStore(secondBase, secondStore));
            // 断言
            assertSame(secondStore, second.get(2, TimeUnit.SECONDS));
            assertFalse(first.isDone());
            release.countDown();
            assertSame(firstStore, first.get(5, TimeUnit.SECONDS));
            verify(firstStore).afterPropertiesSet();
            verify(secondStore).afterPropertiesSet();
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            ragTool.destroy();
        }
    }

    @Test
    public void testGetOrCreateVectorStore_initializationFailureCanRetry() throws Exception {
        // mock 数据
        MilvusVectorStore failedStore = mock(MilvusVectorStore.class);
        MilvusVectorStore readyStore = mock(MilvusVectorStore.class);
        // mock failedStore 的方法
        doThrow(new IllegalStateException("初始化失败")).when(failedStore).afterPropertiesSet();
        // 准备参数
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(1L)
                .setEmbeddingProviderId(1L).setEmbeddingModelId(1L);

        // 调用，并断言异常
        assertThrows(IllegalStateException.class, () -> getOrCreateVectorStore(knowledgeBase, failedStore));
        // 调用
        MilvusVectorStore result = getOrCreateVectorStore(knowledgeBase, readyStore);
        // 断言
        assertSame(readyStore, result);
        verify(failedStore).afterPropertiesSet();
        verify(readyStore).afterPropertiesSet();
    }

    @Test
    public void testGetOrCreateVectorStore_embeddingModelChanged() throws Exception {
        // mock 数据
        MilvusVectorStore firstStore = mock(MilvusVectorStore.class);
        MilvusVectorStore secondStore = mock(MilvusVectorStore.class);
        EmbeddingModel nextModel = mock(EmbeddingModel.class);
        // mock llmModelFactory 的方法
        when(llmModelFactory.getOrCreateEmbeddingModel(any())).thenReturn(embeddingModel, nextModel);
        // 准备参数
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(1L)
                .setEmbeddingProviderId(1L).setEmbeddingModelId(1L);

        // 调用
        MilvusVectorStore first = getOrCreateVectorStore(knowledgeBase, firstStore);
        MilvusVectorStore second = getOrCreateVectorStore(knowledgeBase, secondStore);
        // 断言
        assertSame(firstStore, first);
        assertSame(secondStore, second);
        verify(firstStore).afterPropertiesSet();
        verify(secondStore).afterPropertiesSet();
    }

    private MilvusVectorStore getOrCreateVectorStore(Ai1KnowledgeBaseDO knowledgeBase, MilvusVectorStore store) {
        MilvusVectorStore.Builder builder = mock(MilvusVectorStore.Builder.class, RETURNS_SELF);
        when(builder.build()).thenReturn(store);
        try (MockedStatic<MilvusVectorStore> factory = mockStatic(MilvusVectorStore.class)) {
            factory.when(() -> MilvusVectorStore.builder(any(MilvusServiceClient.class), any(EmbeddingModel.class)))
                    .thenReturn(builder);
            return ReflectionTestUtils.invokeMethod(ragTool, "getOrCreateVectorStore", knowledgeBase);
        }
    }

}
