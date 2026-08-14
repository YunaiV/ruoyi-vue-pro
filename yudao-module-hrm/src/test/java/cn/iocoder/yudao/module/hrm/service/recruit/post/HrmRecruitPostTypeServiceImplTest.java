package cn.iocoder.yudao.module.hrm.service.recruit.post;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostTypeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.post.HrmRecruitPostTypeMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_POST_TYPE_NOT_EXISTS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HrmRecruitPostTypeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitPostTypeServiceImpl.class)
public class HrmRecruitPostTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitPostTypeServiceImpl recruitPostTypeService;

    @Resource
    private HrmRecruitPostTypeMapper recruitPostTypeMapper;

    @Test
    public void testValidateRecruitPostTypeExists_null() {
        // 调用，无需断言
        recruitPostTypeService.validateRecruitPostTypeExists(null);
    }

    @Test
    public void testValidateRecruitPostTypeExists_success() {
        // mock 数据
        HrmRecruitPostTypeDO dbPostType = randomRecruitPostTypeDO();
        recruitPostTypeMapper.insert(dbPostType);

        // 调用，无需断言
        recruitPostTypeService.validateRecruitPostTypeExists(dbPostType.getId());
    }

    @Test
    public void testValidateRecruitPostTypeExists_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> recruitPostTypeService.validateRecruitPostTypeExists(id),
                RECRUIT_POST_TYPE_NOT_EXISTS);
    }

    @Test
    public void testGetRecruitPostTypeList() {
        // mock 数据
        HrmRecruitPostTypeDO secondPostType = randomRecruitPostTypeDO(o -> o
                .setSort(20).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitPostTypeMapper.insert(secondPostType);
        HrmRecruitPostTypeDO firstPostType = randomRecruitPostTypeDO(o -> o
                .setSort(10).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitPostTypeMapper.insert(firstPostType);
        // 测试 status 不匹配
        recruitPostTypeMapper.insert(randomRecruitPostTypeDO(o -> o
                .setSort(1).setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用
        List<HrmRecruitPostTypeDO> postTypes = recruitPostTypeService.getRecruitPostTypeList(
                CommonStatusEnum.ENABLE.getStatus());

        // 断言
        assertEquals(asList(firstPostType.getId(), secondPostType.getId()),
                convertList(postTypes, HrmRecruitPostTypeDO::getId));
    }

    @Test
    public void testGetRecruitPostTypeListByIds() {
        // mock 数据
        HrmRecruitPostTypeDO firstPostType = randomRecruitPostTypeDO();
        recruitPostTypeMapper.insert(firstPostType);
        HrmRecruitPostTypeDO secondPostType = randomRecruitPostTypeDO();
        recruitPostTypeMapper.insert(secondPostType);
        // 测试 id 不匹配
        recruitPostTypeMapper.insert(randomRecruitPostTypeDO());
        // 准备参数
        List<Long> ids = asList(firstPostType.getId(), secondPostType.getId());

        // 调用
        List<HrmRecruitPostTypeDO> postTypes = recruitPostTypeService.getRecruitPostTypeListByIds(ids);

        // 断言
        assertEquals(2, postTypes.size());
        assertEquals(new HashSet<>(ids), convertSet(postTypes, HrmRecruitPostTypeDO::getId));
    }

    @Test
    public void testGetRecruitPostTypeListByIds_empty() {
        // 调用
        List<HrmRecruitPostTypeDO> postTypes = recruitPostTypeService.getRecruitPostTypeListByIds(emptyList());

        // 断言
        assertTrue(postTypes.isEmpty());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmRecruitPostTypeDO randomRecruitPostTypeDO(
            Consumer<HrmRecruitPostTypeDO>... consumers) {
        Consumer<HrmRecruitPostTypeDO> consumer = o -> o.setName(randomString()).setParentId(0L)
                .setSort(randomInteger()).setStatus(randomCommonStatus());
        return randomPojo(HrmRecruitPostTypeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
