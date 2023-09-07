package cn.iocoder.yudao.module.member.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.group.MemberGroupDO;
import cn.iocoder.yudao.module.member.dal.mysql.group.MemberGroupMapper;
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
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.GROUP_HAS_USER;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.GROUP_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// TODO 芋艿：完全 review 完，在去 review 单测
/**
 * {@link MemberGroupServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Import(MemberGroupServiceImpl.class)
public class MemberGroupServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MemberGroupServiceImpl groupService;

    @Resource
    private MemberGroupMapper groupMapper;

    @MockBean
    private MemberUserService memberUserService;

    @Test
    public void testCreateGroup_success() {
        // 准备参数
        MemberGroupCreateReqVO reqVO = randomPojo(MemberGroupCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));

        // 调用
        Long groupId = groupService.createGroup(reqVO);
        // 断言
        assertNotNull(groupId);
        // 校验记录的属性是否正确
        MemberGroupDO group = groupMapper.selectById(groupId);
        assertPojoEquals(reqVO, group);
    }

    @Test
    public void testUpdateGroup_success() {
        // mock 数据
        MemberGroupDO dbGroup = randomPojo(MemberGroupDO.class);
        groupMapper.insert(dbGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MemberGroupUpdateReqVO reqVO = randomPojo(MemberGroupUpdateReqVO.class, o -> {
            o.setId(dbGroup.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });

        // 调用
        groupService.updateGroup(reqVO);
        // 校验是否更新正确
        MemberGroupDO group = groupMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, group);
    }

    @Test
    public void testUpdateGroup_notExists() {
        // 准备参数
        MemberGroupUpdateReqVO reqVO = randomPojo(MemberGroupUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> groupService.updateGroup(reqVO), GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteGroup_success() {
        // mock 数据
        MemberGroupDO dbGroup = randomPojo(MemberGroupDO.class);
        groupMapper.insert(dbGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbGroup.getId();

        // 调用
        groupService.deleteGroup(id);
        // 校验数据不存在了
        assertNull(groupMapper.selectById(id));
    }

    @Test
    public void testDeleteGroup_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> groupService.deleteGroup(id), GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteGroup_hasUser() {
        // mock 数据
        MemberGroupDO dbGroup = randomPojo(MemberGroupDO.class);
        groupMapper.insert(dbGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbGroup.getId();

        // mock 会员数据
        when(memberUserService.getUserCountByGroupId(eq(id))).thenReturn(1L);

        // 调用, 并断言异常
        assertServiceException(() -> groupService.deleteGroup(id), GROUP_HAS_USER);
    }

    @Test
    public void testGetGroupPage() {
        String name = randomString();
        int status = CommonStatusEnum.ENABLE.getStatus();

        // mock 数据
        MemberGroupDO dbGroup = randomPojo(MemberGroupDO.class, o -> { // 等会查询到
            o.setName(name);
            o.setStatus(status);
            o.setCreateTime(buildTime(2023, 2, 18));
        });
        groupMapper.insert(dbGroup);
        // 测试 name 不匹配
        groupMapper.insert(cloneIgnoreId(dbGroup, o -> o.setName("")));
        // 测试 status 不匹配
        groupMapper.insert(cloneIgnoreId(dbGroup, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 createTime 不匹配
        groupMapper.insert(cloneIgnoreId(dbGroup, o -> o.setCreateTime(null)));
        // 准备参数
        MemberGroupPageReqVO reqVO = new MemberGroupPageReqVO();
        reqVO.setName(name);
        reqVO.setStatus(status);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<MemberGroupDO> pageResult = groupService.getGroupPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbGroup, pageResult.getList().get(0));
    }

}
