package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupUpdateReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmUserGroupMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.USER_GROUP_NOT_EXISTS;

/**
 * {@link BpmUserGroupServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(BpmUserGroupServiceImpl.class)
public class BpmUserGroupServiceTest extends BaseDbUnitTest {

    @Resource
    private BpmUserGroupServiceImpl userGroupService;

    @Resource
    private BpmUserGroupMapper userGroupMapper;

    @Test
    public void testCreateUserGroup_success() {
        // 准备参数
        BpmUserGroupCreateReqVO reqVO = RandomUtils.randomPojo(BpmUserGroupCreateReqVO.class);

        // 调用
        Long userGroupId = userGroupService.createUserGroup(reqVO);
        // 断言
        Assertions.assertNotNull(userGroupId);
        // 校验记录的属性是否正确
        BpmUserGroupDO userGroup = userGroupMapper.selectById(userGroupId);
        AssertUtils.assertPojoEquals(reqVO, userGroup);
    }

    @Test
    public void testUpdateUserGroup_success() {
        // mock 数据
        BpmUserGroupDO dbUserGroup = RandomUtils.randomPojo(BpmUserGroupDO.class);
        userGroupMapper.insert(dbUserGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BpmUserGroupUpdateReqVO reqVO = RandomUtils.randomPojo(BpmUserGroupUpdateReqVO.class, o -> {
            o.setId(dbUserGroup.getId()); // 设置更新的 ID
        });

        // 调用
        userGroupService.updateUserGroup(reqVO);
        // 校验是否更新正确
        BpmUserGroupDO userGroup = userGroupMapper.selectById(reqVO.getId()); // 获取最新的
        AssertUtils.assertPojoEquals(reqVO, userGroup);
    }

    @Test
    public void testUpdateUserGroup_notExists() {
        // 准备参数
        BpmUserGroupUpdateReqVO reqVO = RandomUtils.randomPojo(BpmUserGroupUpdateReqVO.class);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> userGroupService.updateUserGroup(reqVO), USER_GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteUserGroup_success() {
        // mock 数据
        BpmUserGroupDO dbUserGroup = RandomUtils.randomPojo(BpmUserGroupDO.class);
        userGroupMapper.insert(dbUserGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUserGroup.getId();

        // 调用
        userGroupService.deleteUserGroup(id);
       // 校验数据不存在了
       Assertions.assertNull(userGroupMapper.selectById(id));
    }

    @Test
    public void testDeleteUserGroup_notExists() {
        // 准备参数
        Long id = RandomUtils.randomLongId();

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> userGroupService.deleteUserGroup(id), USER_GROUP_NOT_EXISTS);
    }

    @Test
    public void testGetUserGroupPage() {
       // mock 数据
       BpmUserGroupDO dbUserGroup = RandomUtils.randomPojo(BpmUserGroupDO.class, o -> { // 等会查询到
           o.setName("芋道源码");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(buildTime(2021, 11, 11));
       });
       userGroupMapper.insert(dbUserGroup);
       // 测试 name 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setName("芋道")));
       // 测试 status 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setCreateTime(buildTime(2021, 12, 12))));
       // 准备参数
       BpmUserGroupPageReqVO reqVO = new BpmUserGroupPageReqVO();
       reqVO.setName("源码");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021, 11, 10),buildTime(2021, 11, 12)}));

       // 调用
       PageResult<BpmUserGroupDO> pageResult = userGroupService.getUserGroupPage(reqVO);
       // 断言
       Assertions.assertEquals(1, pageResult.getTotal());
       Assertions.assertEquals(1, pageResult.getList().size());
       AssertUtils.assertPojoEquals(dbUserGroup, pageResult.getList().get(0));
    }

}
