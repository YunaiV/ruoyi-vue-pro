package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmUserGroupMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl.BpmUserGroupServiceImpl;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.USER_GROUP_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

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
        BpmUserGroupCreateReqVO reqVO = randomPojo(BpmUserGroupCreateReqVO.class);

        // 调用
        Long userGroupId = userGroupService.createUserGroup(reqVO);
        // 断言
        assertNotNull(userGroupId);
        // 校验记录的属性是否正确
        BpmUserGroupDO userGroup = userGroupMapper.selectById(userGroupId);
        assertPojoEquals(reqVO, userGroup);
    }

    @Test
    public void testUpdateUserGroup_success() {
        // mock 数据
        BpmUserGroupDO dbUserGroup = randomPojo(BpmUserGroupDO.class);
        userGroupMapper.insert(dbUserGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BpmUserGroupUpdateReqVO reqVO = randomPojo(BpmUserGroupUpdateReqVO.class, o -> {
            o.setId(dbUserGroup.getId()); // 设置更新的 ID
        });

        // 调用
        userGroupService.updateUserGroup(reqVO);
        // 校验是否更新正确
        BpmUserGroupDO userGroup = userGroupMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, userGroup);
    }

    @Test
    public void testUpdateUserGroup_notExists() {
        // 准备参数
        BpmUserGroupUpdateReqVO reqVO = randomPojo(BpmUserGroupUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> userGroupService.updateUserGroup(reqVO), USER_GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteUserGroup_success() {
        // mock 数据
        BpmUserGroupDO dbUserGroup = randomPojo(BpmUserGroupDO.class);
        userGroupMapper.insert(dbUserGroup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUserGroup.getId();

        // 调用
        userGroupService.deleteUserGroup(id);
       // 校验数据不存在了
       assertNull(userGroupMapper.selectById(id));
    }

    @Test
    public void testDeleteUserGroup_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> userGroupService.deleteUserGroup(id), USER_GROUP_NOT_EXISTS);
    }

    @Test
    public void testGetUserGroupPage() {
       // mock 数据
       BpmUserGroupDO dbUserGroup = randomPojo(BpmUserGroupDO.class, o -> { // 等会查询到
           o.setName("芋道源码");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(DateUtils.buildTime(2021, 11, 11));
       });
       userGroupMapper.insert(dbUserGroup);
       // 测试 name 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setName("芋道")));
       // 测试 status 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       userGroupMapper.insert(cloneIgnoreId(dbUserGroup, o -> o.setCreateTime(DateUtils.buildTime(2021, 12, 12))));
       // 准备参数
       BpmUserGroupPageReqVO reqVO = new BpmUserGroupPageReqVO();
       reqVO.setName("源码");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setBeginCreateTime(DateUtils.buildTime(2021, 11, 10));
       reqVO.setEndCreateTime(DateUtils.buildTime(2021, 11, 12));

       // 调用
       PageResult<BpmUserGroupDO> pageResult = userGroupService.getUserGroupPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbUserGroup, pageResult.getList().get(0));
    }

}
