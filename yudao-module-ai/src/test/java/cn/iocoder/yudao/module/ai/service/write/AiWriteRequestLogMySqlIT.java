package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.mybatis.core.handler.DefaultDBFieldHandler;
import cn.iocoder.yudao.framework.mybatis.core.injector.YudaoSqlInjector;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.requestlog.AiRequestLogMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogServiceImpl;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.MockedStatic;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AiWriteServiceImpl} 请求日志的真实 MySQL 集成测试
 *
 * 模型完全 Mock，不调用厂商；Mapper、字段填充、多租户插件均为真实组件。
 * 需设置 AI_REQUEST_LOG_MYSQL_IT=true，以及 AI_REQUEST_LOG_IT_DB_URL、AI_REQUEST_LOG_IT_DB_USER、AI_REQUEST_LOG_IT_DB_PASSWORD
 *
 * @author 芋道源码
 */
@EnabledIfEnvironmentVariable(named = "AI_REQUEST_LOG_MYSQL_IT", matches = "true")
public class AiWriteRequestLogMySqlIT {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 1L;
    /**
     * 不存在的模型编号，仅作为快照写入，不新增真实模型
     */
    private static final Long MODEL_ID = -20261101L;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    /**
     * 业务事务回滚 case 中主动抛出的预期异常信息
     */
    private static final String BUSINESS_ROLLBACK_MESSAGE = "it-business-rollback";

    private static HikariDataSource dataSource;
    private static AiWriteMapper writeMapper;
    private static AiRequestLogMapper requestLogMapper;

    private AiWriteServiceImpl writeService;
    private ChatModel chatModel;
    private MockedStatic<DictFrameworkUtils> dictFrameworkUtilsMock;
    /**
     * 本 case 唯一的模型标识，用于定位、清理本次创建的数据
     */
    private String modelMarker;

    @BeforeAll
    public static void initDatabase() {
        String url = System.getenv("AI_REQUEST_LOG_IT_DB_URL");
        String username = System.getenv("AI_REQUEST_LOG_IT_DB_USER");
        assumeTrue(StrUtil.isNotBlank(url) && StrUtil.isNotBlank(username),
                "未设置 AI_REQUEST_LOG_IT_DB_URL、AI_REQUEST_LOG_IT_DB_USER，跳过");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(System.getenv("AI_REQUEST_LOG_IT_DB_PASSWORD"));
        config.setMaximumPoolSize(2);
        dataSource = new HikariDataSource(config);

        // 与项目一致：自增主键、字段自动填充、SQL 注入器、多租户插件
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("ai-request-log-it",
                new SpringManagedTransactionFactory(), dataSource));
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setBanner(false);
        globalConfig.setMetaObjectHandler(new DefaultDBFieldHandler());
        globalConfig.setSqlInjector(new YudaoSqlInjector());
        globalConfig.getDbConfig().setIdType(IdType.AUTO);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                new TenantDatabaseInterceptor(new TenantProperties())));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(AiWriteMapper.class);
        configuration.addMapper(AiRequestLogMapper.class);
        SqlSessionFactory sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        writeMapper = sqlSessionTemplate.getMapper(AiWriteMapper.class);
        requestLogMapper = sqlSessionTemplate.getMapper(AiRequestLogMapper.class);
    }

    @AfterAll
    public static void closeDatabase() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    public void setUp() {
        modelMarker = "it" + IdUtil.fastSimpleUUID();
        TenantContextHolder.setTenantId(TENANT_ID);
        // 构建 prompt 时的字典翻译，不依赖 Spring 容器
        dictFrameworkUtilsMock = mockStatic(DictFrameworkUtils.class);

        // 模型：仅 Mock，不调用厂商
        AiChatRoleService chatRoleService = mock(AiChatRoleService.class);
        when(chatRoleService.getChatRoleListByName(any())).thenReturn(Collections.emptyList());
        AiModelService modelService = mock(AiModelService.class);
        AiModelDO model = AiModelDO.builder().id(MODEL_ID).platform(AiPlatformEnum.OPENAI.getPlatform())
                .model(modelMarker).type(AiModelTypeEnum.CHAT.getType()).build();
        when(modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(model);
        chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(MODEL_ID)).thenReturn(chatModel);

        // Service：注入真实 Mapper
        AiRequestLogServiceImpl requestLogService = new AiRequestLogServiceImpl();
        ReflectionTestUtils.setField(requestLogService, "requestLogMapper", requestLogMapper);
        writeService = new AiWriteServiceImpl();
        ReflectionTestUtils.setField(writeService, "modalService", modelService);
        ReflectionTestUtils.setField(writeService, "chatRoleService", chatRoleService);
        ReflectionTestUtils.setField(writeService, "writeMapper", writeMapper);
        ReflectionTestUtils.setField(writeService, "requestLogService", requestLogService);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try {
            cleanUp();
        } finally {
            if (dictFrameworkUtilsMock != null) {
                dictFrameworkUtilsMock.close();
            }
            TenantContextHolder.clear();
        }
    }

    @Test
    public void testGenerate_success() {
        // 准备：贴近 OpenAI 流式帧，文本帧不带用量，随后无 generation 的帧携带用量，末尾 EmptyUsage 不能覆盖已有用量
        Flux<ChatResponse> stream = Flux.just(chunk("Hello ", new EmptyUsage()), chunk("World", new EmptyUsage()),
                usageChunk(new DefaultUsage(10, 5, 15)), emptyChunk());

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(stream);

        // 断言：正文
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals("Hello World", joinContent(results));
        // 断言：业务快照与日志
        Map<String, Object> write = selectWrite();
        Map<String, Object> requestLog = selectRequestLog();
        assertEquals("Hello World", write.get("generated_content"));
        assertNull(write.get("error_message"));
        assertRequestLog(requestLog, write, 20, 10L, 5L, 15L);
        assertNull(requestLog.get("error_message"));
        // 断言：每帧返回当前用量快照；仅用量的帧返回空内容；EmptyUsage 不覆盖已有用量，不逐帧累加；最后一帧与业务、日志一致
        assertEquals(4, results.size());
        assertFrame(results.get(0), write, "Hello ", null, null, null);
        assertFrame(results.get(1), write, "World", null, null, null);
        assertFrame(results.get(2), write, "", 10L, 5L, 15L);
        assertFrame(results.get(3), write, "", 10L, 5L, 15L);
        // 断言：metadata 经 JacksonTypeHandler 读回，usage 为有效用量
        AiRequestLogDO requestLogDO = requestLogMapper.selectById(toLong(requestLog.get("id")));
        assertNotNull(requestLogDO.getResponseMetadata());
        Object usage = requestLogDO.getResponseMetadata().get("usage");
        assertInstanceOf(Map.class, usage);
        assertEquals(10L, toLong(((Map<?, ?>) usage).get("promptTokens")));
        assertEquals(5L, toLong(((Map<?, ?>) usage).get("completionTokens")));

        // 删除业务记录后，日志仍存在
        writeService.deleteWrite(toLong(write.get("id")));
        assertEquals(Boolean.TRUE, toBoolean(selectWrite().get("deleted")));
        Map<String, Object> requestLogAfterDelete = selectRequestLog();
        assertEquals(requestLog.get("request_id"), requestLogAfterDelete.get("request_id"));
        assertEquals(Boolean.FALSE, toBoolean(requestLogAfterDelete.get("deleted")));
    }

    @Test
    public void testGenerate_unknownUsage() {
        // 调用：全程只有 EmptyUsage
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(Flux.just(chunk("unknown", new EmptyUsage()), emptyChunk()));

        // 断言：未知为 NULL，不补 0
        assertEquals("unknown", joinContent(results));
        Map<String, Object> write = selectWrite();
        assertEquals("unknown", write.get("generated_content"));
        assertRequestLog(selectRequestLog(), write, 20, null, null, null);
        // 断言：流式返回的用量同样为 null，不补 0
        assertEquals(2, results.size());
        assertFrame(results.get(0), write, "unknown", null, null, null);
        assertFrame(results.get(1), write, "", null, null, null);
    }

    @Test
    public void testGenerate_realZeroUsage() {
        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(Flux.just(chunk("zero", new DefaultUsage(0, 0, 0))));

        // 断言：真实 0 存 0
        Map<String, Object> write = selectWrite();
        assertEquals("zero", write.get("generated_content"));
        assertRequestLog(selectRequestLog(), write, 20, 0L, 0L, 0L);
        // 断言：流式返回同样保留真实 0
        assertEquals(1, results.size());
        assertFrame(results.get(0), write, "zero", 0L, 0L, 0L);
    }

    @Test
    public void testGenerate_streamErrorKeepKnownUsage() {
        // 准备：已收到有效用量后出错
        Flux<ChatResponse> stream = Flux.concat(Flux.just(chunk("partial", new DefaultUsage(7, 3, 10))),
                Flux.error(new RuntimeException("it-stream-error")));

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(stream);

        // 断言：错误流沿用原 error CommonResult
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals("partial", results.get(0).getData().getContent());
        assertFalse(results.get(1).isSuccess());
        assertEquals(ErrorCodeConstants.WRITE_STREAM_ERROR.getCode(), results.get(1).getCode());
        // 断言：保留已知用量
        Map<String, Object> write = selectWrite();
        Map<String, Object> requestLog = selectRequestLog();
        assertNull(write.get("generated_content"));
        assertEquals("it-stream-error", write.get("error_message"));
        assertRequestLog(requestLog, write, 30, 7L, 3L, 10L);
        assertEquals("it-stream-error", requestLog.get("error_message"));
        // 断言：出错前的帧返回已知用量，与业务、日志一致
        assertFrame(results.get(0), write, "partial", 7L, 3L, 10L);
    }

    @Test
    public void testGenerate_cancel() {
        // 准备：收到用量后流不结束，由订阅方取消
        when(chatModel.stream(any(Prompt.class))).thenReturn(
                Flux.concat(Flux.just(chunk("cancel", new DefaultUsage(4, 2, 6))), Flux.never()));

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = writeService.generateWriteContent(buildReqVO(), USER_ID)
                .take(1).collectList().block(TIMEOUT);

        // 断言
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("cancel", results.get(0).getData().getContent());
        Map<String, Object> write = selectWrite();
        Map<String, Object> requestLog = selectRequestLog();
        assertNull(write.get("generated_content"));
        assertNull(write.get("error_message"));
        assertRequestLog(requestLog, write, 40, 4L, 2L, 6L);
        assertNull(requestLog.get("error_message"));
        // 断言：取消前的帧返回已知用量，与业务、日志一致
        assertFrame(results.get(0), write, "cancel", 4L, 2L, 6L);
    }

    @Test
    public void testGenerate_streamFactoryThrow() {
        // 准备：stream 工厂同步抛错
        when(chatModel.stream(any(Prompt.class))).thenThrow(new IllegalStateException("it-sync-error"));

        // 调用：原异常抛出
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> writeService.generateWriteContent(buildReqVO(), USER_ID));
        assertEquals("it-sync-error", ex.getMessage());

        // 断言
        Map<String, Object> write = selectWrite();
        Map<String, Object> requestLog = selectRequestLog();
        assertNull(write.get("generated_content"));
        assertEquals("it-sync-error", write.get("error_message"));
        assertRequestLog(requestLog, write, 30, null, null, null);
        assertEquals("it-sync-error", requestLog.get("error_message"));
        assertNull(requestLog.get("response_metadata"));
    }

    @Test
    public void testGenerate_requestLogStoreFail() {
        // 准备：日志存储抛错；构建、更新快照仍为真实逻辑
        AiRequestLogService failedRequestLogService = spy(new AiRequestLogServiceImpl());
        doThrow(new RuntimeException("it-store-error")).when(failedRequestLogService).createRequestLog(any());
        ReflectionTestUtils.setField(writeService, "requestLogService", failedRequestLogService);

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(Flux.just(chunk("kept", new DefaultUsage(2, 1, 3))));

        // 断言：正文与业务快照不受影响，没有日志行
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals("kept", joinContent(results));
        Map<String, Object> write = selectWrite();
        assertEquals("kept", write.get("generated_content"));
        assertEquals(2L, toLong(write.get("input_tokens")));
        assertEquals(1L, toLong(write.get("output_tokens")));
        assertEquals(3L, toLong(write.get("total_tokens")));
        assertFrame(results.get(0), write, "kept", 2L, 1L, 3L);
        assertEquals(0L, count("SELECT COUNT(*) FROM ai_request_log WHERE tenant_id = ? AND request_id = ?",
                TENANT_ID, write.get("request_id")));
        verify(failedRequestLogService).createRequestLog(any());
    }

    @Test
    public void testGenerate_requestLogCommitWhenBusinessRollback() {
        // 准备：真实请求日志 Service 的事务代理，与业务共用同一 DataSource 与事务管理器
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        AiRequestLogService transactionalRequestLogService = buildTransactionalRequestLogService(transactionManager);
        assertTrue(AopUtils.isAopProxy(transactionalRequestLogService));
        ReflectionTestUtils.setField(writeService, "requestLogService", transactionalRequestLogService);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        AtomicReference<AiWriteDO> writeRef = new AtomicReference<>();

        // 调用：外层业务事务内完成生成，再主动抛出预期异常，使业务事务回滚
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {
                    assertTrue(status.isNewTransaction());
                    List<CommonResult<AiWriteGenerateRespVO>> results = generate(Flux.just(chunk("rollback", new DefaultUsage(10, 5, 15))));
                    assertTrue(results.stream().allMatch(CommonResult::isSuccess));
                    assertEquals("rollback", joinContent(results));
                    // 通过外层事务自身的连接读取未提交的写作记录，不额外借用连接
                    writeRef.set(writeMapper.selectOne(new QueryWrapper<AiWriteDO>()
                            .eq("user_id", USER_ID).eq("model", modelMarker)));
                    // 断言：流式返回的用量与写作一致
                    AiWriteGenerateRespVO frame = results.get(0).getData();
                    assertEquals(writeRef.get().getId(), frame.getId());
                    assertEquals(10L, frame.getInputTokens());
                    assertEquals(5L, frame.getOutputTokens());
                    assertEquals(15L, frame.getTotalTokens());
                    throw new IllegalStateException(BUSINESS_ROLLBACK_MESSAGE);
                }));
        assertEquals(BUSINESS_ROLLBACK_MESSAGE, ex.getMessage());

        // 断言：外层事务内写作记录已插入并更新
        AiWriteDO write = writeRef.get();
        assertNotNull(write);
        assertEquals("rollback", write.getGeneratedContent());
        assertEquals(10L, write.getInputTokens());
        assertEquals(5L, write.getOutputTokens());
        assertEquals(15L, write.getTotalTokens());
        // 断言：外层结束后，业务写作已回滚
        assertEquals(0L, count("SELECT COUNT(*) FROM ai_write WHERE tenant_id = ? AND user_id = ? AND model = ?",
                TENANT_ID, USER_ID, modelMarker));
        assertEquals(0L, count("SELECT COUNT(*) FROM ai_write WHERE id = ?", write.getId()));
        // 断言：请求日志独立提交，有且只有 1 行
        assertEquals(1L, count("SELECT COUNT(*) FROM ai_request_log WHERE tenant_id = ? AND request_id = ?",
                TENANT_ID, write.getRequestId()));
        Map<String, Object> requestLog = selectRequestLog();
        assertEquals(36, StrUtil.length(write.getRequestId()));
        assertEquals(write.getRequestId(), requestLog.get("request_id"));
        assertEquals(2L, toLong(requestLog.get("biz_type")));
        assertEquals(write.getId(), toLong(requestLog.get("biz_id")));
        assertEquals(20L, toLong(requestLog.get("status")));
        assertEquals(USER_ID, toLong(requestLog.get("user_id")));
        assertEquals(TENANT_ID, toLong(requestLog.get("tenant_id")));
        assertEquals(modelMarker, requestLog.get("model"));
        assertEquals(10L, toLong(requestLog.get("input_tokens")));
        assertEquals(5L, toLong(requestLog.get("output_tokens")));
        assertEquals(15L, toLong(requestLog.get("total_tokens")));
        assertNull(requestLog.get("error_message"));
        assertEquals(Boolean.FALSE, toBoolean(requestLog.get("deleted")));
    }

    /**
     * 构建真实请求日志 Service 的事务代理：按其 @Transactional 注解解析事务属性
     */
    private static AiRequestLogService buildTransactionalRequestLogService(TransactionManager transactionManager) {
        AiRequestLogServiceImpl target = new AiRequestLogServiceImpl();
        ReflectionTestUtils.setField(target, "requestLogMapper", requestLogMapper);
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addInterface(AiRequestLogService.class);
        proxyFactory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        return (AiRequestLogService) proxyFactory.getProxy();
    }

    // ========== 调用 ==========

    private List<CommonResult<AiWriteGenerateRespVO>> generate(Flux<ChatResponse> stream) {
        when(chatModel.stream(any(Prompt.class))).thenReturn(stream);
        List<CommonResult<AiWriteGenerateRespVO>> results = writeService.generateWriteContent(buildReqVO(), USER_ID)
                .collectList().block(TIMEOUT);
        assertNotNull(results);
        return results;
    }

    private AiWriteGenerateReqVO buildReqVO() {
        AiWriteGenerateReqVO reqVO = new AiWriteGenerateReqVO();
        reqVO.setType(AiWriteTypeEnum.WRITING.getType());
        reqVO.setPrompt("AI 请求日志集成测试");
        reqVO.setLength(1);
        reqVO.setFormat(1);
        reqVO.setTone(1);
        reqVO.setLanguage(1);
        return reqVO;
    }

    private ChatResponse chunk(String text, Usage usage) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("it-response").model(modelMarker)
                .usage(usage).build();
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))), metadata);
    }

    /**
     * 无 generation、仅携带用量的帧，例如 OpenAI 开启 stream_options.include_usage 后的最后一帧
     */
    private ChatResponse usageChunk(Usage usage) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("it-response").model(modelMarker)
                .usage(usage).build();
        return new ChatResponse(List.of(), metadata);
    }

    private ChatResponse emptyChunk() {
        return new ChatResponse(List.of(), ChatResponseMetadata.builder().usage(new EmptyUsage()).build());
    }

    private static String joinContent(List<CommonResult<AiWriteGenerateRespVO>> results) {
        return results.stream().map(result -> result.getData().getContent()).collect(Collectors.joining());
    }

    // ========== 断言 ==========

    /**
     * 断言流式返回的一帧：写作编号、本次内容片段、当前用量快照
     */
    private static void assertFrame(CommonResult<AiWriteGenerateRespVO> result, Map<String, Object> write,
                                    String content, Long inputTokens, Long outputTokens, Long totalTokens) {
        assertTrue(result.isSuccess());
        AiWriteGenerateRespVO frame = result.getData();
        assertEquals(toLong(write.get("id")), frame.getId());
        assertEquals(content, frame.getContent());
        assertEquals(inputTokens, frame.getInputTokens());
        assertEquals(outputTokens, frame.getOutputTokens());
        assertEquals(totalTokens, frame.getTotalTokens());
    }

    private void assertRequestLog(Map<String, Object> requestLog, Map<String, Object> write, int status,
                                  Long inputTokens, Long outputTokens, Long totalTokens) {
        // 关联：同一 requestId，业务编号为写作编号
        String requestId = (String) requestLog.get("request_id");
        assertEquals(36, StrUtil.length(requestId));
        assertEquals(requestId, write.get("request_id"));
        assertEquals(2L, toLong(requestLog.get("biz_type")));
        assertEquals(toLong(write.get("id")), toLong(requestLog.get("biz_id")));
        assertEquals((long) status, toLong(requestLog.get("status")));
        // 快照：用户、租户、模型
        assertEquals(USER_ID, toLong(requestLog.get("user_id")));
        assertEquals(TENANT_ID, toLong(requestLog.get("tenant_id")));
        assertEquals(TENANT_ID, toLong(write.get("tenant_id")));
        assertEquals(USER_ID.toString(), requestLog.get("creator"));
        assertEquals(AiPlatformEnum.OPENAI.getPlatform(), requestLog.get("platform"));
        assertEquals(MODEL_ID, toLong(requestLog.get("model_id")));
        assertEquals(modelMarker, requestLog.get("model"));
        // 时间
        LocalDateTime beginTime = (LocalDateTime) requestLog.get("begin_time");
        LocalDateTime endTime = (LocalDateTime) requestLog.get("end_time");
        assertNotNull(beginTime);
        assertNotNull(endTime);
        assertFalse(endTime.isBefore(beginTime));
        assertTrue(toLong(requestLog.get("duration")) >= 0);
        // 用量：日志与业务快照一致，null 与 0 区分
        assertEquals(inputTokens, toLong(requestLog.get("input_tokens")));
        assertEquals(outputTokens, toLong(requestLog.get("output_tokens")));
        assertEquals(totalTokens, toLong(requestLog.get("total_tokens")));
        assertEquals(inputTokens, toLong(write.get("input_tokens")));
        assertEquals(outputTokens, toLong(write.get("output_tokens")));
        assertEquals(totalTokens, toLong(write.get("total_tokens")));
        assertEquals(Boolean.FALSE, toBoolean(requestLog.get("deleted")));
    }

    private static Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return (Boolean) value;
    }

    // ========== JDBC ==========

    /**
     * 查询本 case 唯一的写作记录（包括已软删）
     */
    private Map<String, Object> selectWrite() {
        return selectOne("SELECT * FROM ai_write WHERE tenant_id = ? AND user_id = ? AND model = ?",
                TENANT_ID, USER_ID, modelMarker);
    }

    /**
     * 查询本 case 唯一的请求日志；多于一行说明终态重复写入
     */
    private Map<String, Object> selectRequestLog() {
        return selectOne("SELECT * FROM ai_request_log WHERE tenant_id = ? AND user_id = ? AND model = ?",
                TENANT_ID, USER_ID, modelMarker);
    }

    /**
     * 按租户、用户、唯一模型标识与捕获的 requestId 精确物理清理，并确认无残留
     */
    private void cleanUp() throws SQLException {
        if (dataSource == null || modelMarker == null) {
            return;
        }
        Set<String> requestIds = new LinkedHashSet<>();
        for (String table : List.of("ai_write", "ai_request_log")) {
            selectList("SELECT request_id FROM " + table + " WHERE tenant_id = ? AND user_id = ? AND model = ?",
                    TENANT_ID, USER_ID, modelMarker).forEach(row -> {
                if (row.get("request_id") != null) {
                    requestIds.add((String) row.get("request_id"));
                }
            });
        }
        for (String requestId : requestIds) {
            update("DELETE FROM ai_request_log WHERE tenant_id = ? AND user_id = ? AND request_id = ?",
                    TENANT_ID, USER_ID, requestId);
        }
        update("DELETE FROM ai_request_log WHERE tenant_id = ? AND user_id = ? AND model = ?",
                TENANT_ID, USER_ID, modelMarker);
        update("DELETE FROM ai_write WHERE tenant_id = ? AND user_id = ? AND model = ?",
                TENANT_ID, USER_ID, modelMarker);
        // 确认无残留
        assertEquals(0L, count("SELECT COUNT(*) FROM ai_request_log WHERE model = ?", modelMarker));
        assertEquals(0L, count("SELECT COUNT(*) FROM ai_write WHERE model = ?", modelMarker));
        for (String requestId : requestIds) {
            assertEquals(0L, count("SELECT COUNT(*) FROM ai_request_log WHERE request_id = ?", requestId));
            assertEquals(0L, count("SELECT COUNT(*) FROM ai_write WHERE request_id = ?", requestId));
        }
    }

    private Map<String, Object> selectOne(String sql, Object... args) {
        List<Map<String, Object>> rows = selectList(sql, args);
        assertEquals(1, rows.size(), "期望一行：" + sql);
        return rows.get(0);
    }

    private List<Map<String, Object>> selectList(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = prepare(connection, sql, args);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    Object value = resultSet.getObject(i);
                    if (value instanceof java.sql.Timestamp timestamp) {
                        value = timestamp.toLocalDateTime();
                    }
                    row.put(metaData.getColumnLabel(i).toLowerCase(), value);
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private long count(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = prepare(connection, sql, args);
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void update(String sql, Object... args) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = prepare(connection, sql, args)) {
            statement.executeUpdate();
        }
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement;
    }

}
