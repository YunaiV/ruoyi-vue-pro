package cn.iocoder.yudao.module.system.service.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sensitiveword.SensitiveWordDO;
import cn.iocoder.yudao.module.system.dal.mysql.sensitiveword.SensitiveWordMapper;
import cn.iocoder.yudao.module.system.mq.producer.sensitiveword.SensitiveWordProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.max;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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

    @MockBean
    private SensitiveWordProducer sensitiveWordProducer;

    @Test
    public void testInitLocalCache() {
        SensitiveWordDO wordDO1 = randomPojo(SensitiveWordDO.class, o -> o.setName("傻瓜")
                .setTags(singletonList("论坛")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO1);
        SensitiveWordDO wordDO2 = randomPojo(SensitiveWordDO.class, o -> o.setName("笨蛋")
                .setTags(singletonList("蔬菜")).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        sensitiveWordMapper.insert(wordDO2);

        // 调用
        sensitiveWordService.initLocalCache();
        // 断言 maxUpdateTime 缓存
        assertEquals(max(wordDO1.getUpdateTime(), wordDO2.getUpdateTime()), sensitiveWordService.getMaxUpdateTime());
        // 断言 sensitiveWordTagsCache 缓存
        assertEquals(SetUtils.asSet("论坛", "蔬菜"), sensitiveWordService.getSensitiveWordTags());
        // 断言 tagSensitiveWordTries 缓存
        assertNotNull(sensitiveWordService.getDefaultSensitiveWordTrie());
        assertEquals(2, sensitiveWordService.getTagSensitiveWordTries().size());
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("论坛"));
        assertNotNull(sensitiveWordService.getTagSensitiveWordTries().get("蔬菜"));
    }

    @Test
    public void testCreateSensitiveWord_success() {
        // 准备参数
        SensitiveWordCreateReqVO reqVO = randomPojo(SensitiveWordCreateReqVO.class);

        // 调用
        Long sensitiveWordId = sensitiveWordService.createSensitiveWord(reqVO);
        // 断言
        assertNotNull(sensitiveWordId);
        // 校验记录的属性是否正确
        SensitiveWordDO sensitiveWord = sensitiveWordMapper.selectById(sensitiveWordId);
        assertPojoEquals(reqVO, sensitiveWord);
        verify(sensitiveWordProducer).sendSensitiveWordRefreshMessage();
    }

    @Test
    public void testUpdateSensitiveWord_success() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class);
        sensitiveWordMapper.insert(dbSensitiveWord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SensitiveWordUpdateReqVO reqVO = randomPojo(SensitiveWordUpdateReqVO.class, o -> {
            o.setId(dbSensitiveWord.getId()); // 设置更新的 ID
        });

        // 调用
        sensitiveWordService.updateSensitiveWord(reqVO);
        // 校验是否更新正确
        SensitiveWordDO sensitiveWord = sensitiveWordMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, sensitiveWord);
        verify(sensitiveWordProducer).sendSensitiveWordRefreshMessage();
    }

    @Test
    public void testUpdateSensitiveWord_notExists() {
        // 准备参数
        SensitiveWordUpdateReqVO reqVO = randomPojo(SensitiveWordUpdateReqVO.class);

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
        verify(sensitiveWordProducer).sendSensitiveWordRefreshMessage();
    }

    @Test
    public void testDeleteSensitiveWord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> sensitiveWordService.deleteSensitiveWord(id), SENSITIVE_WORD_NOT_EXISTS);
    }

    @Test
    public void testGetSensitiveWordPage() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class, o -> { // 等会查询到
            o.setName("笨蛋");
            o.setTags(Arrays.asList("论坛", "蔬菜"));
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(DateUtils.buildTime(2022, 2, 8));
        });
        sensitiveWordMapper.insert(dbSensitiveWord);
        // 测试 name 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setName("傻瓜")));
        // 测试 tags 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setTags(Arrays.asList("短信", "日用品"))));
        // 测试 createTime 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setCreateTime(DateUtils.buildTime(2022, 2, 16))));
        // 准备参数
        SensitiveWordPageReqVO reqVO = new SensitiveWordPageReqVO();
        reqVO.setName("笨");
        reqVO.setTag("论坛");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2022, 2, 1),buildTime(2022, 2, 12)}));

        // 调用
        PageResult<SensitiveWordDO> pageResult = sensitiveWordService.getSensitiveWordPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSensitiveWord, pageResult.getList().get(0));
    }

    @Test
    public void testGetSensitiveWordList() {
        // mock 数据
        SensitiveWordDO dbSensitiveWord = randomPojo(SensitiveWordDO.class, o -> { // 等会查询到
            o.setName("笨蛋");
            o.setTags(Arrays.asList("论坛", "蔬菜"));
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(DateUtils.buildTime(2022, 2, 8));
        });
        sensitiveWordMapper.insert(dbSensitiveWord);
        // 测试 name 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setName("傻瓜")));
        // 测试 tags 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setTags(Arrays.asList("短信", "日用品"))));
        // 测试 createTime 不匹配
        sensitiveWordMapper.insert(cloneIgnoreId(dbSensitiveWord, o -> o.setCreateTime(DateUtils.buildTime(2022, 2, 16))));
        // 准备参数
        SensitiveWordExportReqVO reqVO = new SensitiveWordExportReqVO();
        reqVO.setName("笨");
        reqVO.setTag("论坛");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2022, 2, 1),buildTime(2022, 2, 12)}));

        // 调用
        List<SensitiveWordDO> list = sensitiveWordService.getSensitiveWordList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbSensitiveWord, list.get(0));
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
    }

    @Test
    public void testIsTestValid_noTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";

        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text, null));
    }

    @Test
    public void testIsTestValid_hasTag() {
        testInitLocalCache();
        // 准备参数
        String text = "你是傻瓜，你是笨蛋";

        // 调用，断言
        assertFalse(sensitiveWordService.isTextValid(text, singletonList("论坛")));
    }

}
