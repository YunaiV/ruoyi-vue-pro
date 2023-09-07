package cn.iocoder.yudao.module.member.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.module.member.dal.mysql.tag.MemberTagMapper;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.TAG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：完全 review 完，在去 review 单测
/**
 * {@link MemberTagServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(MemberTagServiceImpl.class)
public class MemberTagServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MemberTagServiceImpl tagService;

    @Resource
    private MemberTagMapper tagMapper;

    @MockBean
    private MemberUserService memberUserService;

    @Test
    public void testCreateTag_success() {
        // 准备参数
        MemberTagCreateReqVO reqVO = randomPojo(MemberTagCreateReqVO.class);

        // 调用
        Long tagId = tagService.createTag(reqVO);
        // 断言
        assertNotNull(tagId);
        // 校验记录的属性是否正确
        MemberTagDO tag = tagMapper.selectById(tagId);
        assertPojoEquals(reqVO, tag);
    }

    @Test
    public void testUpdateTag_success() {
        // mock 数据
        MemberTagDO dbTag = randomPojo(MemberTagDO.class);
        tagMapper.insert(dbTag);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MemberTagUpdateReqVO reqVO = randomPojo(MemberTagUpdateReqVO.class, o -> {
            o.setId(dbTag.getId()); // 设置更新的 ID
        });

        // 调用
        tagService.updateTag(reqVO);
        // 校验是否更新正确
        MemberTagDO tag = tagMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, tag);
    }

    @Test
    public void testUpdateTag_notExists() {
        // 准备参数
        MemberTagUpdateReqVO reqVO = randomPojo(MemberTagUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> tagService.updateTag(reqVO), TAG_NOT_EXISTS);
    }

    @Test
    public void testDeleteTag_success() {
        // mock 数据
        MemberTagDO dbTag = randomPojo(MemberTagDO.class);
        tagMapper.insert(dbTag);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTag.getId();

        // 调用
        tagService.deleteTag(id);
        // 校验数据不存在了
        assertNull(tagMapper.selectById(id));
    }

    @Test
    public void testDeleteTag_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> tagService.deleteTag(id), TAG_NOT_EXISTS);
    }

    @Test
    public void testGetTagPage() {
        // mock 数据
        MemberTagDO dbTag = randomPojo(MemberTagDO.class, o -> { // 等会查询到
            o.setName("test");
            o.setCreateTime(buildTime(2023, 2, 18));
        });
        tagMapper.insert(dbTag);
        // 测试 name 不匹配
        tagMapper.insert(cloneIgnoreId(dbTag, o -> o.setName("ne")));
        // 测试 createTime 不匹配
        tagMapper.insert(cloneIgnoreId(dbTag, o -> o.setCreateTime(null)));
        // 准备参数
        MemberTagPageReqVO reqVO = new MemberTagPageReqVO();
        reqVO.setName("test");
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<MemberTagDO> pageResult = tagService.getTagPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTag, pageResult.getList().get(0));
    }

}
