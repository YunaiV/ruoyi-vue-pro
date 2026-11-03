package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiChatMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiModelServiceImpl} 思考配置相关的单元测试类
 *
 * 重点：思考配置整体提交；null 或档位为空表示未配置，创建不写入、更新由 updateForSave 同一条 UPDATE 清空，非空整体替换；已配置时模型平台必须与 API 密钥平台一致
 * 档位格式、判重、默认档位、思考参数 null 由 {@link AiModelSaveReqVO} 校验；VO 与 DO 之间用 BeanUtils 转换
 *
 * @author 芋道源码
 */
public class AiModelServiceImplReasoningTest extends BaseMockitoUnitTest {

    private static final String LEVEL_PATTERN_INVALID = "思考档位值只能包含小写字母、数字、下划线、中划线，长度 1~32";
    private static final String DEFAULT_LEVEL_INVALID = "默认思考档位必须在档位数组中；清空时不能提交默认档位";

    @InjectMocks
    private AiModelServiceImpl modelService;

    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiChatMapper modelMapper;
    @Mock
    private AiModelFactory modelFactory;

    @BeforeAll
    public static void initTableInfo() {
        // LambdaUpdateWrapper#set 需要实体的表信息才能解析列名
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AiModelDO.class);
    }

    @BeforeEach
    public void setUp() {
        // updateForSave 是 Mapper 的默认方法，执行真实逻辑，便于断言最终的 update(entity, wrapper)
        lenient().doCallRealMethod().when(modelMapper).updateForSave(any(AiModelDO.class));
    }

    // ========== 创建 ==========

    @Test
    public void testCreateModel_reasoningNull() {
        // 准备参数：不提交思考配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(null);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.createModel(reqVO);
        // 断言：不写入
        assertNull(captureInsert().getReasoningConfig());
    }

    @Test
    public void testCreateModel_reasoningEmpty() {
        // 准备参数：档位为空视为未配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setReasoningConfig(buildEmptyReasoningConfig());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.createModel(reqVO);
        // 断言：空配置不留在实体上
        assertNull(captureInsert().getReasoningConfig());
    }

    @Test
    public void testCreateModel_reasoningConfigured() {
        // 准备参数
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(null);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.createModel(reqVO);
        // 断言：转换成 DO 整体写入，档位保持提交的顺序
        AiModelDO model = captureInsert();
        assertEquals(buildReasoningConfig(), model.getReasoningConfig());
        assertEquals(asList("off", "high"), levelNames(model.getReasoningConfig()));
    }

    @Test
    public void testCreateModel_reasoningPlatformMismatch() {
        // 准备参数：模型是 DeepSeek，API 密钥是 OpenAI
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(null);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.OPENAI);

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_REASONING_PLATFORM_MISMATCH,
                AiPlatformEnum.DEEP_SEEK.getPlatform(), AiPlatformEnum.OPENAI.getPlatform());
        verify(modelMapper, never()).insert(any(AiModelDO.class));
    }

    @Test
    public void testCreateModel_unconfiguredPlatformMismatchAllowed() {
        // 准备参数：未配置思考时，不做平台一致校验（历史数据保持现状）
        AiModelSaveReqVO reqVO = buildSaveReqVO(null);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.OPENAI);

        // 调用，并断言
        assertDoesNotThrow(() -> modelService.createModel(reqVO));
    }

    @Test
    public void testCreateModel_reasoningUnsupportedPlatform() {
        // 准备参数：Anthropic 一期不支持
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(null).setPlatform(AiPlatformEnum.ANTHROPIC.getPlatform());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.ANTHROPIC);

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_REASONING_PLATFORM_NOT_SUPPORTED,
                AiPlatformEnum.ANTHROPIC.getPlatform());
    }

    // ========== 更新 ==========

    @Test
    public void testUpdateModel_reasoningNull_clears() {
        // mock 数据：库里已配置思考
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(buildConfiguredModel(id));
        // 准备参数：不提交思考配置，与创建一致视为未配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(id).setName("改名");
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：普通字段与思考配置置空在同一条 UPDATE
        AiModelDO entity = assertUpdateForSave(1);
        assertEquals("改名", entity.getName());
        assertNull(entity.getReasoningConfig());
    }

    @Test
    public void testUpdateModel_reasoningEmpty_clears() {
        // mock 数据
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id));
        // 准备参数：档位为空表示清空思考配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(id).setReasoningConfig(buildEmptyReasoningConfig());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：实体上思考配置为 null，该列只由 wrapper 置空一次，普通字段保留
        AiModelDO entity = assertUpdateForSave(1);
        assertNull(entity.getReasoningConfig());
        assertEquals(reqVO.getName(), entity.getName());
    }

    @Test
    public void testUpdateModel_reasoningConfigured_replaces() {
        // mock 数据
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id));
        // 准备参数
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(id);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：思考配置随实体整体替换，wrapper 不置空
        AiModelDO entity = assertUpdateForSave(0);
        assertEquals(buildReasoningConfig(), entity.getReasoningConfig());
    }

    @Test
    public void testUpdateModel_reasoningReordered_replaces() {
        // mock 数据：库里已配置思考
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(buildConfiguredModel(id));
        // 准备参数：只调整档位顺序
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(id);
        Collections.reverse(reqVO.getReasoningConfig().getLevels());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：按新顺序整体替换
        AiModelDO entity = assertUpdateForSave(0);
        assertEquals(asList("high", "off"), levelNames(entity.getReasoningConfig()));
        assertEquals("high", entity.getReasoningConfig().getDefaultLevel());
    }

    @Test
    public void testUpdateModel_reasoningPlatformMismatch() {
        // mock 数据
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id));
        // 准备参数：提交思考配置，但换成另一个平台的 API 密钥
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(id);
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.ZHI_PU);

        // 调用，并断言异常：拒绝，不写库
        assertServiceException(() -> modelService.updateModel(reqVO), MODEL_REASONING_PLATFORM_MISMATCH,
                AiPlatformEnum.DEEP_SEEK.getPlatform(), AiPlatformEnum.ZHI_PU.getPlatform());
        verify(modelMapper, never()).updateForSave(any(AiModelDO.class));
    }

    @Test
    public void testUpdateModel_typeChangedWithoutReasoning_clears() {
        // mock 数据：库里已配置思考
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(buildConfiguredModel(id));
        // 准备参数：类型改成图片，不提交思考配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(id).setType(AiModelTypeEnum.IMAGE.getType());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：不再按库里的旧配置校验，直接清空
        assertNull(assertUpdateForSave(1).getReasoningConfig());
    }

    @Test
    public void testUpdateModel_reasoningEmptyPlatformNotSupported_clears() {
        // mock 数据：库里已配置思考
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(buildConfiguredModel(id));
        // 准备参数：同时改成不支持的平台，并提交空档位清空思考配置
        AiModelSaveReqVO reqVO = buildSaveReqVO(id).setPlatform(AiPlatformEnum.ANTHROPIC.getPlatform())
                .setReasoningConfig(buildEmptyReasoningConfig());
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.ANTHROPIC);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：清空时不做平台校验，走单条 UPDATE
        assertNull(assertUpdateForSave(1).getReasoningConfig());
    }

    // ========== 保存参数校验、VO 与 DO 转换 ==========

    @Test
    public void testSaveReqVO_reasoningValid() {
        // 准备参数：纯数字、与 JS 对象原型同名的档位值；不限制档位个数；多层对象、数组都不含 null
        Map<String, Object> thinking = new LinkedHashMap<>();
        thinking.put("type", "enabled");
        thinking.put("budget_tokens", 1024);
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("thinking", thinking);
        options.put("tags", asList("a", "b"));
        List<AiModelSaveReqVO.ReasoningLevel> levels = new ArrayList<>();
        for (String level : asList("1", "2", "__proto__", "constructor", "l4", "l5", "l6", "l7", "l8")) {
            levels.add(buildReasoningLevelReqVO(level, options));
        }
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setReasoningConfig(
                new AiModelSaveReqVO.ReasoningConfig().setLevels(levels).setDefaultLevel("2"));

        // 调用，并断言
        assertDoesNotThrow(() -> ValidationUtils.validate(reqVO));
        assertDoesNotThrow(() -> ValidationUtils.validate(buildConfiguredReqVO(null)));
    }

    @Test
    public void testSaveReqVO_reasoningClear() {
        // 调用，并断言：不提交、清空（默认档位为空）都通过
        assertDoesNotThrow(() -> ValidationUtils.validate(buildSaveReqVO(null)));
        assertDoesNotThrow(() -> ValidationUtils.validate(
                buildSaveReqVO(null).setReasoningConfig(buildEmptyReasoningConfig())));
        assertDoesNotThrow(() -> ValidationUtils.validate(
                buildSaveReqVO(null).setReasoningConfig(buildEmptyReasoningConfig().setDefaultLevel(""))));
        // 调用，并断言异常：清空时提交默认档位；不传档位数组
        assertReasoningInvalid(buildEmptyReasoningConfig().setDefaultLevel("high"), DEFAULT_LEVEL_INVALID);
        assertReasoningInvalid(new AiModelSaveReqVO.ReasoningConfig(), "思考档位数组不能为空，清空请传 []");
    }

    @Test
    public void testSaveReqVO_reasoningLevelInvalid() {
        // 调用，并断言异常：非法字符（含逗号、大写）、超长、空字符串
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off"),
                buildReasoningLevelReqVO("a,b")), LEVEL_PATTERN_INVALID);
        assertReasoningInvalid(buildReasoningConfigReqVO("High", buildReasoningLevelReqVO("High")), LEVEL_PATTERN_INVALID);
        String longLevel = StrUtil.repeat('a', 33);
        assertReasoningInvalid(buildReasoningConfigReqVO(longLevel, buildReasoningLevelReqVO(longLevel)), LEVEL_PATTERN_INVALID);
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off"),
                buildReasoningLevelReqVO("")), LEVEL_PATTERN_INVALID);
        // 调用，并断言异常：档位值为 null、档位为 null、重复
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off"),
                buildReasoningLevelReqVO(null)), "思考档位值不能为空");
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off"), null),
                "思考档位不能为空");
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off"),
                buildReasoningLevelReqVO("off")), "思考档位值不能重复");
    }

    @Test
    public void testSaveReqVO_reasoningDefaultInvalid() {
        // 调用，并断言异常：默认档位为空、不在档位数组中
        assertReasoningInvalid(buildReasoningConfigReqVO().setDefaultLevel(null), DEFAULT_LEVEL_INVALID);
        assertReasoningInvalid(buildReasoningConfigReqVO().setDefaultLevel(""), DEFAULT_LEVEL_INVALID);
        assertReasoningInvalid(buildReasoningConfigReqVO().setDefaultLevel("ultra"), DEFAULT_LEVEL_INVALID);
    }

    @Test
    public void testSaveReqVO_reasoningOptionsInvalid() {
        // 准备参数：顶层值为 null
        Map<String, Object> topNull = new HashMap<>();
        topNull.put("temperature", null);
        // 准备参数：嵌套对象的值为 null
        Map<String, Object> thinking = new HashMap<>();
        thinking.put("type", null);
        Map<String, Object> nestedNull = new HashMap<>();
        nestedNull.put("thinking", thinking);
        // 准备参数：数组元素为 null
        Map<String, Object> arrayNull = new HashMap<>();
        arrayNull.put("stop", asList("a", null));
        // 准备参数：数组内对象的值为 null
        Map<String, Object> item = new HashMap<>();
        item.put("budget", null);
        Map<String, Object> arrayItemNull = new HashMap<>();
        arrayItemNull.put("items", Collections.singletonList(item));

        // 调用，并断言异常
        assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off", null)), "思考参数不能为空");
        for (Map<String, Object> options : asList(topNull, nestedNull, arrayNull, arrayItemNull)) {
            assertReasoningInvalid(buildReasoningConfigReqVO("off", buildReasoningLevelReqVO("off", options)),
                    "思考参数不能包含 null");
        }
    }

    @Test
    public void testSaveReqVO_reasoningJsonNoHelperFields() {
        // 准备参数
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(null);

        // 调用
        JsonNode config = JsonUtils.parseTree(JsonUtils.toJsonString(reqVO)).get("reasoningConfig");
        // 断言：校验方法不会序列化
        assertEquals(new HashSet<>(asList("levels", "defaultLevel")), fieldNames(config));
        assertEquals(new HashSet<>(asList("level", "options")), fieldNames(config.get("levels").get(0)));
    }

    @Test
    public void testCreateModel_reasoningConvertedToDO() {
        // 准备参数：从请求 JSON 反序列化，思考参数含嵌套对象、数组、整数、布尔
        String json = "{\"keyId\":1,\"name\":\"DeepSeek 对话\",\"model\":\"deepseek-chat\",\"platform\":\""
                + AiPlatformEnum.DEEP_SEEK.getPlatform() + "\",\"type\":" + AiModelTypeEnum.CHAT.getType()
                + ",\"sort\":1,\"status\":" + CommonStatusEnum.ENABLE.getStatus() + ",\"reasoningConfig\":{\"levels\":["
                + "{\"level\":\"off\",\"options\":{\"thinking\":{\"type\":\"disabled\"}}},"
                + "{\"level\":\"1\",\"options\":{\"thinking\":{\"type\":\"enabled\",\"budget_tokens\":1024},"
                + "\"tags\":[\"a\",\"b\"],\"stream_reasoning\":true}}],\"defaultLevel\":\"1\"}}";
        AiModelSaveReqVO reqVO = JsonUtils.parseObject(json, AiModelSaveReqVO.class);
        assertDoesNotThrow(() -> ValidationUtils.validate(reqVO));
        // mock apiKeyService 的方法
        mockApiKey(reqVO.getKeyId(), AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.createModel(reqVO);
        // 断言：BeanUtils 把内部 VO 转换成 DO 的思考配置，档位顺序、默认档位、思考参数的结构和类型不变
        AiModelDO.ReasoningConfig config = captureInsert().getReasoningConfig();
        assertInstanceOf(AiModelDO.ReasoningLevel.class, config.getLevels().get(1));
        assertEquals(asList("off", "1"), levelNames(config));
        assertEquals("1", config.getDefaultLevel());
        Map<String, Object> options = config.getLevels().get(1).getOptions();
        assertEquals(JsonUtils.parseTree(json).get("reasoningConfig").get("levels").get(1).get("options"),
                JsonUtils.parseTree(JsonUtils.toJsonString(options)));
        assertEquals(1024, ((Map<?, ?>) options.get("thinking")).get("budget_tokens"));
        assertEquals(Boolean.TRUE, options.get("stream_reasoning"));
    }

    @Test
    public void testModelRespVO_reasoningConfig() {
        // 准备参数
        AiModelDO model = buildConfiguredModel(randomLongId());

        // 调用：与 AiModelController#getModel 一样，用 BeanUtils 转换
        AiModelRespVO respVO = BeanUtils.toBean(model, AiModelRespVO.class);
        // 断言：转换成 AiModelSaveReqVO 的嵌套类，JSON 结构仍是 {levels:[{level,options}],defaultLevel}，校验方法不输出
        assertInstanceOf(AiModelSaveReqVO.ReasoningLevel.class, respVO.getReasoningConfig().getLevels().get(0));
        assertEquals(JsonUtils.parseTree(JsonUtils.toJsonString(model.getReasoningConfig())),
                JsonUtils.parseTree(JsonUtils.toJsonString(respVO)).get("reasoningConfig"));
    }

    // ========== 发送 ==========

    @Test
    public void testValidateReasoningPlatform_unconfigured() {
        // 准备参数：未配置思考
        AiModelDO model = new AiModelDO().setId(randomLongId()).setKeyId(randomLongId())
                .setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform());

        // 调用
        AiPlatformEnum platform = modelService.validateReasoningPlatform(model);
        // 断言：不查 API 密钥
        assertEquals(AiPlatformEnum.DEEP_SEEK, platform);
        verify(apiKeyService, never()).validateApiKey(any());
    }

    @Test
    public void testValidateReasoningPlatform_mismatch() {
        // 准备参数：库里已有的不一致数据
        AiModelDO model = new AiModelDO().setId(randomLongId()).setKeyId(randomLongId())
                .setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform())
                .setReasoningConfig(buildReasoningConfig());
        // mock apiKeyService 的方法
        mockApiKey(model.getKeyId(), AiPlatformEnum.ZHI_PU);

        // 调用，并断言异常
        assertServiceException(() -> modelService.validateReasoningPlatform(model), MODEL_REASONING_PLATFORM_MISMATCH,
                AiPlatformEnum.DEEP_SEEK.getPlatform(), AiPlatformEnum.ZHI_PU.getPlatform());
    }

    @Test
    public void testGetChatModel_nullOptions() {
        // mock 数据
        Long id = randomLongId();
        Long keyId = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id).setKeyId(keyId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        // mock apiKeyService 的方法
        AiApiKeyDO apiKey = mockApiKey(keyId, AiPlatformEnum.DEEP_SEEK);

        // 调用
        modelService.getChatModel(id, null);
        // 断言：思考参数为 null 原样传给工厂，由工厂按平台、API KEY、URL 缓存
        verify(modelFactory).getOrCreateChatModel(eq(AiPlatformEnum.DEEP_SEEK), eq(apiKey.getApiKey()), eq(apiKey.getUrl()),
                isNull());
    }

    @Test
    public void testGetChatModel_withOptions() {
        // mock 数据
        Long id = randomLongId();
        Long keyId = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id).setKeyId(keyId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        // mock apiKeyService 的方法
        AiApiKeyDO apiKey = mockApiKey(keyId, AiPlatformEnum.DEEP_SEEK);
        // 准备参数
        Map<String, Object> levelOptions = Map.of("reasoning_effort", "high");

        // 调用
        modelService.getChatModel(id, levelOptions);
        // 断言
        verify(modelFactory).getOrCreateChatModel(eq(AiPlatformEnum.DEEP_SEEK), eq(apiKey.getApiKey()),
                eq(apiKey.getUrl()), same(levelOptions));
    }

    // ========== 对话返回的模型思考配置 ==========

    @Test
    public void testConversationRespVO_reasoningConfig() {
        // 准备参数：easy-trans 把模型的思考配置整体赋值到对话 VO
        AiModelDO.ReasoningConfig config = buildReasoningConfig();
        AiChatConversationRespVO respVO = new AiChatConversationRespVO().setModelId(randomLongId())
                .setModelReasoningConfig(config);

        // 调用
        JsonNode node = JsonUtils.parseTree(JsonUtils.toJsonString(respVO));
        // 断言：直接返回 {levels:[{level,options}],defaultLevel}，不再返回派生的档位数组、默认档位
        assertEquals(JsonUtils.parseTree(JsonUtils.toJsonString(config)), node.get("modelReasoningConfig"));
        assertEquals("off", node.get("modelReasoningConfig").get("levels").get(0).get("level").asText());
        assertEquals("high", node.get("modelReasoningConfig").get("defaultLevel").asText());
        assertFalse(node.has("modelReasoningLevels"));
        assertFalse(node.has("modelReasoningDefault"));
    }

    @Test
    public void testConversationRespVO_reasoningConfigReordered() {
        // 准备参数：档位顺序倒过来
        AiModelDO.ReasoningConfig config = buildReasoningConfig();
        Collections.reverse(config.getLevels());
        AiChatConversationRespVO respVO = new AiChatConversationRespVO().setModelReasoningConfig(config);

        // 调用
        JsonNode levels = JsonUtils.parseTree(JsonUtils.toJsonString(respVO)).get("modelReasoningConfig").get("levels");
        // 断言：按配置的顺序返回
        assertEquals("high", levels.get(0).get("level").asText());
        assertEquals("off", levels.get(1).get("level").asText());
    }

    @Test
    public void testConversationRespVO_reasoningConfigUnconfigured() {
        // 准备参数：模型未配置思考
        AiChatConversationRespVO respVO = new AiChatConversationRespVO().setModelId(randomLongId());

        // 调用
        JsonNode node = JsonUtils.parseTree(JsonUtils.toJsonString(respVO));
        // 断言：思考配置为空
        assertNull(respVO.getModelReasoningConfig());
        assertTrue(node.path("modelReasoningConfig").isNull() || node.path("modelReasoningConfig").isMissingNode());
    }

    // ========== 工具方法 ==========

    private AiModelDO captureInsert() {
        ArgumentCaptor<AiModelDO> captor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(modelMapper).insert(captor.capture());
        return captor.getValue();
    }

    private AiApiKeyDO mockApiKey(Long keyId, AiPlatformEnum platform) {
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(keyId).setPlatform(platform.getPlatform())
                .setApiKey("sk-test").setUrl("http://127.0.0.1:1");
        when(apiKeyService.validateApiKey(eq(keyId))).thenReturn(apiKey);
        return apiKey;
    }

    /**
     * 断言 updateForSave 恰好执行一次，最终只有一条 update(entity, wrapper)
     *
     * @param clearCount wrapper 中置空思考配置的次数
     * @return 更新的实体
     */
    @SuppressWarnings("unchecked")
    private AiModelDO assertUpdateForSave(int clearCount) {
        verify(modelMapper).updateForSave(any(AiModelDO.class));
        ArgumentCaptor<AiModelDO> entityCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        ArgumentCaptor<Wrapper<AiModelDO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(modelMapper).update(entityCaptor.capture(), wrapperCaptor.capture());
        verify(modelMapper, never()).updateById(any(AiModelDO.class));
        String sqlSet = ((LambdaUpdateWrapper<AiModelDO>) wrapperCaptor.getValue()).getSqlSet();
        assertEquals(clearCount, countOf(StrUtil.nullToEmpty(sqlSet), "reasoning_config="), sqlSet);
        // 置空时显式声明 jdbcType，避免 Oracle、达梦等数据库不支持 OTHER
        assertEquals(clearCount, countOf(StrUtil.nullToEmpty(sqlSet), BaseMapperX.JDBC_TYPE_VARCHAR), sqlSet);
        return entityCaptor.getValue();
    }

    private static int countOf(String text, String token) {
        int count = 0;
        for (int i = text.indexOf(token); i >= 0; i = text.indexOf(token, i + token.length())) {
            count++;
        }
        return count;
    }

    private static void assertReasoningInvalid(AiModelSaveReqVO.ReasoningConfig config, String message) {
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setReasoningConfig(config);
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class,
                () -> ValidationUtils.validate(reqVO));
        boolean matched = false;
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            matched = matched || message.equals(violation.getMessage());
        }
        assertTrue(matched, e.getMessage());
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static List<String> levelNames(AiModelDO.ReasoningConfig config) {
        List<String> names = new ArrayList<>();
        for (AiModelDO.ReasoningLevel level : config.getLevels()) {
            names.add(level.getLevel());
        }
        return names;
    }

    private static AiModelDO buildConfiguredModel(Long id) {
        AiModelSaveReqVO reqVO = buildConfiguredReqVO(id);
        return new AiModelDO().setId(id).setKeyId(reqVO.getKeyId()).setPlatform(reqVO.getPlatform())
                .setType(reqVO.getType()).setReasoningConfig(buildReasoningConfig());
    }

    private static AiModelSaveReqVO buildConfiguredReqVO(Long id) {
        return buildSaveReqVO(id).setReasoningConfig(buildReasoningConfigReqVO());
    }

    private static AiModelDO.ReasoningConfig buildReasoningConfig() {
        List<AiModelDO.ReasoningLevel> levels = new ArrayList<>();
        levels.add(new AiModelDO.ReasoningLevel("off", Map.of("thinking", Map.of("type", "disabled"))));
        levels.add(new AiModelDO.ReasoningLevel("high",
                Map.of("thinking", Map.of("type", "enabled"), "reasoning_effort", "high")));
        return new AiModelDO.ReasoningConfig(levels, "high");
    }

    private static AiModelSaveReqVO.ReasoningConfig buildReasoningConfigReqVO() {
        return buildReasoningConfigReqVO("high",
                buildReasoningLevelReqVO("off", Map.of("thinking", Map.of("type", "disabled"))),
                buildReasoningLevelReqVO("high", Map.of("thinking", Map.of("type", "enabled"), "reasoning_effort", "high")));
    }

    private static AiModelSaveReqVO.ReasoningConfig buildReasoningConfigReqVO(String defaultLevel,
                                                                              AiModelSaveReqVO.ReasoningLevel... levels) {
        return new AiModelSaveReqVO.ReasoningConfig().setLevels(new ArrayList<>(Arrays.asList(levels)))
                .setDefaultLevel(defaultLevel);
    }

    private static AiModelSaveReqVO.ReasoningLevel buildReasoningLevelReqVO(String level) {
        return buildReasoningLevelReqVO(level, new LinkedHashMap<>());
    }

    private static AiModelSaveReqVO.ReasoningLevel buildReasoningLevelReqVO(String level, Map<String, Object> options) {
        return new AiModelSaveReqVO.ReasoningLevel().setLevel(level).setOptions(options);
    }

    private static AiModelSaveReqVO.ReasoningConfig buildEmptyReasoningConfig() {
        return new AiModelSaveReqVO.ReasoningConfig().setLevels(new ArrayList<>());
    }

    private static AiModelSaveReqVO buildSaveReqVO(Long id) {
        AiModelSaveReqVO reqVO = new AiModelSaveReqVO();
        reqVO.setId(id);
        reqVO.setKeyId(randomLongId());
        reqVO.setName("DeepSeek 对话");
        reqVO.setModel("deepseek-chat");
        reqVO.setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform());
        reqVO.setType(AiModelTypeEnum.CHAT.getType());
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
