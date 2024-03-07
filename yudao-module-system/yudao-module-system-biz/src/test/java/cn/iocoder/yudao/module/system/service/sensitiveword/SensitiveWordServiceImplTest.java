package cn.iocoder.yudao.module.system.service.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordSaveVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sensitiveword.SensitiveWordDO;
import cn.iocoder.yudao.module.system.dal.mysql.sensitiveword.SensitiveWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SensitiveWordServiceImpl} 的单元测试类
 *
 * @author 永不言败
 */
@Import(SensitiveWordServiceImpl.class)
public class SensitiveWordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SensitiveWordServiceImpl sensitiveWordService;

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @BeforeEach
    public void setUp() {
        SensitiveWordServiceImpl.ENABLED = true;
    }

    @Test
    public void testInitLocalCache() {
        SensitiveWordDO wordDO1 = randomPojo(SensitiveWordDO.class, o -> o.setName("傻瓜")
                .setTags(singletonList("论坛")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO1);
        SensitiveWordDO wordDO2 = randomPojo(SensitiveWordDO.class, o -> o.setName("笨蛋")
                .setTags(singletonList("蔬菜")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO2);
        SensitiveWordDO wordDO3 = randomPojo(SensitiveWordDO.class, o -> o.setName("白")
                .setTags(singletonList("测试")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO3);
        SensitiveWordDO wordDO4 = randomPojo(SensitiveWordDO.class, o -> o.setName("白痴")
                .setTags(singletonList("测试")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO4);

        // 调用
        sensitiveWordService.initLocalCache();
        // 断言 sensitiveWordTagsCache 缓存
        assertEquals(SetUtils.asSet("论坛", "蔬菜", "测试"), sensitiveWordService.getSensitiveWordTagSet());
        // 断言 sensitiveWordCache
        assertEquals(4, sensitiveWordService.getSensitiveWordCache().size());
        assertPojoEquals(wordDO1, sensitiveWordService.getSensitiveWordCache().get(0));
        assertPojoEquals(wordDO2, sensitiveWordService.getSensitiveWordCache().get(1));
        assertPojoEquals(wordDO3, sensitiveWordService.getSensitiveWordCache().get(2));
        // 断言 tagSensitiveWordTries 缓存
        assertNotNull(sensitiveWordService.getDefaultSensitiveWordTrie());
        assertEquals(3, sensitiveWordService.getTagSensitiveWordTries().size());
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("论坛"));
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("蔬菜"));
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("测试"));
    }

    @Test
    public void testRefreshLocalCache() {
        // mock 数据
        SensitiveWordDO wordDO1 = randomPojo(SensitiveWordDO.class, o -> o.setName("傻瓜")
                .setTags(singletonList("论坛")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        wordDO1.setUpdateTime(LocalDateTime.now());
        sensitiveWordMapper.insert(wordDO1);
        sensitiveWordService.initLocalCache();
        // mock 数据 ②
        SensitiveWordDO wordDO2 = randomPojo(SensitiveWordDO.class, o -> o.setName("笨蛋")
                .setTags(singletonList("蔬菜")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        wordDO2.setUpdateTime(LocalDateTimeUtils.addTime(Duration.ofMinutes(1))); // 避免时间相同
        sensitiveWordMapper.insert(wordDO2);

        // 调用
        sensitiveWordService.refreshLocalCache();
        // 断言 sensitiveWordTagsCache 缓存
        assertEquals(SetUtils.asSet("论坛", "蔬菜"), sensitiveWordService.getSensitiveWordTagSet());
        // 断言 sensitiveWordCache
        assertEquals(2, sensitiveWordService.getSensitiveWordCache().size());
        assertPojoEquals(wordDO1, sensitiveWordService.getSensitiveWordCache().get(0));
        assertPojoEquals(wordDO2, sensitiveWordService.getSensitiveWordCache().get(1));
        // 断言 tagSensitiveWordTries 缓存
        assertNotNull(sensitiveWordService.getDefaultSensitiveWordTrie());
        assertEquals(2, sensitiveWordService.getTagSensitiveWordTries().size());
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("论坛"));
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("蔬菜"));
    }

    @Test
    public void testCreateSensitiveWord_success() {
        // 准备参数
        SensitiveWordSaveVO reqVO = randomPojo(SensitiveWordSaveVO.class)
                .setId(null); // 防止 id 被赋值

        // 调用
        Long sensitiveWordId = sensitiveWordService.createSensitiveWord(reqVO);
        // 断言
        assertNotNull(sensitiveWordId);
        // 校验记录的属性是否正确
        SensitiveWordDO sensitiveWord = sensitiveWordMapper.selectById(sensitiveWordId);
        assertPojoEquals(reqVO, sensitiveWord, "id");
    }

    @Test
    public void testUpdateSensitiveWord_success() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(dbSensitiveWord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SensitiveWordSaveVO reqVO = randomPojo(SensitiveWordSaveVO.class, o -> {
            o.setId(dbSensitiveWord.getId()); // 设置更新的 ID
        });

        // 调用
        sensitiveWordService.updateSensitiveWord(reqVO);
        // 校验是否更新正确
        SensitiveWordDO sensitiveWord = sensitiveWordMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, sensitiveWord);
    }

    @Test
    public void testUpdateSensitiveWord_notExists() {
        // 准备参数
        SensitiveWordSaveVO reqVO = randomPojo(SensitiveWordSaveVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> sensitiveWordService.updateSensitiveWord(reqVO), SENSITIVE_WORD_NOT_EXISTS);
    }

    @Test
    public void testDeleteSensitiveWord_success() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(dbSensitiveWord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSensitiveWord.getId();

        // 调用
        sensitiveWordService.deleteSensitiveWord(id);
        // 校验数据不存在了
        assertNull(sensitiveWordMapper.selectById(id));
    }

    @Test
    public void testDeleteSensitiveWord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> sensitiveWordService.deleteSensitiveWord(id), SENSITIVE_WORD_NOT_EXISTS);
    }

    @Test
    public void testGetSensitiveWord() {
        // mock 数据
        SensitiveWordDO sensitiveWord = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(sensitiveWord);
        // 准备参数
        Long id = sensitiveWord.getId();

        // 调用
        SensitiveWordDO dbSensitiveWord = sensitiveWordService.getSensitiveWord(id);
        // 断言
        assertPojoEquals(sensitiveWord, dbSensitiveWord);
    }

    @Test
    public void testGetSensitiveWordList() {
        // mock 数据
        SensitiveWordDO sensitiveWord01 = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(sensitiveWord01);
        SensitiveWordDO sensitiveWord02 = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(sensitiveWord02);

        // 调用
        List<SensitiveWordDO> list = sensitiveWordService.getSensitiveWordList();
        // 断言
        assertEquals(2, list.size());
        assertEquals(sensitiveWord01, list.get(0));
        assertEquals(sensitiveWord02, list.get(1));
    }

    @Test
    public void testGetSensitiveWordPage() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class, o -> { // 等会查询到
            o.setName("笨蛋");
            o.setTags(Arrays.asList("论坛", "蔬菜"));
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2022, 2, 8));
        });
        sensitiveWordMapper.insert(dbSensitiveWord);
        // 测试 name 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setName("傻瓜")));
        // 测试 tags 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setTags(Arrays.asList("短信", "日用品"))));
        // 测试 createTime 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setCreateTime(buildTime(2022, 2, 16))));
        // 准备参数
        SensitiveWordPageReqVO reqVO = new SensitiveWordPageReqVO();
        reqVO.setName("笨");
        reqVO.setTag("论坛");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2022, 2, 1, 2022, 2, 12));

        // 调用
        PageResult<SensitiveWordDO> pageResult = sensitiveWordService.getSensitiveWordPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSensitiveWord, pageResult.getList().get(0));
    }

    @Test
    public void testValidateText_noTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";
        // 调用
        List<String> result = sensitiveWordService.validateText(text, null);
        // 断言
        assertEquals(Arrays.asList("傻瓜", "笨蛋"), result);

        // 准备参数
        String text2 = "你是傻瓜，你是笨蛋，你是白";
        // 调用
        List<String> result2 = sensitiveWordService.validateText(text2, null);
        // 断言
        assertEquals(Arrays.asList("傻瓜", "笨蛋","白"), result2);
    }

    @Test
    public void testValidateText_hasTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";
        // 调用
        List<String> result = sensitiveWordService.validateText(text, singletonList("论坛"));
        // 断言
        assertEquals(singletonList("傻瓜"), result);


        // 准备参数
        String text2 = "你是白";
        // 调用
        List<String> result2 = sensitiveWordService.validateText(text2, singletonList("测试"));
        // 断言
        assertEquals(singletonList("白"), result2);
    }

    @Test
    public void testIsTestValid_noTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";
        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text, null));

        // 准备参数
        String text2 = "你是白";
        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text2, null));
    }

    @Test
    public void testIsTestValid_hasTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";
        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text, singletonList("论坛")));

        // 准备参数
        String text2 = "你是白";
        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text2, singletonList("测试")));
    }

}
