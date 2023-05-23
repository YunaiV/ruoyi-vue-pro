package cn.iocoder.yudao.module.jl.service.join;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2managerMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link JoinSaleslead2managerServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(JoinSaleslead2managerServiceImpl.class)
public class JoinSaleslead2managerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private JoinSaleslead2managerServiceImpl joinSaleslead2managerService;

    @Resource
    private JoinSaleslead2managerMapper joinSaleslead2managerMapper;

    @Test
    public void testCreateJoinSaleslead2manager_success() {
        // 准备参数
        JoinSaleslead2managerCreateReqVO reqVO = randomPojo(JoinSaleslead2managerCreateReqVO.class);

        // 调用
        Long joinSaleslead2managerId = joinSaleslead2managerService.createJoinSaleslead2manager(reqVO);
        // 断言
        assertNotNull(joinSaleslead2managerId);
        // 校验记录的属性是否正确
        JoinSaleslead2managerDO joinSaleslead2manager = joinSaleslead2managerMapper.selectById(joinSaleslead2managerId);
        assertPojoEquals(reqVO, joinSaleslead2manager);
    }

    @Test
    public void testUpdateJoinSaleslead2manager_success() {
        // mock 数据
        JoinSaleslead2managerDO dbJoinSaleslead2manager = randomPojo(JoinSaleslead2managerDO.class);
        joinSaleslead2managerMapper.insert(dbJoinSaleslead2manager);// @Sql: 先插入出一条存在的数据
        // 准备参数
        JoinSaleslead2managerUpdateReqVO reqVO = randomPojo(JoinSaleslead2managerUpdateReqVO.class, o -> {
            o.setId(dbJoinSaleslead2manager.getId()); // 设置更新的 ID
        });

        // 调用
        joinSaleslead2managerService.updateJoinSaleslead2manager(reqVO);
        // 校验是否更新正确
        JoinSaleslead2managerDO joinSaleslead2manager = joinSaleslead2managerMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, joinSaleslead2manager);
    }

    @Test
    public void testUpdateJoinSaleslead2manager_notExists() {
        // 准备参数
        JoinSaleslead2managerUpdateReqVO reqVO = randomPojo(JoinSaleslead2managerUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2managerService.updateJoinSaleslead2manager(reqVO), JOIN_SALESLEAD2MANAGER_NOT_EXISTS);
    }

    @Test
    public void testDeleteJoinSaleslead2manager_success() {
        // mock 数据
        JoinSaleslead2managerDO dbJoinSaleslead2manager = randomPojo(JoinSaleslead2managerDO.class);
        joinSaleslead2managerMapper.insert(dbJoinSaleslead2manager);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbJoinSaleslead2manager.getId();

        // 调用
        joinSaleslead2managerService.deleteJoinSaleslead2manager(id);
       // 校验数据不存在了
       assertNull(joinSaleslead2managerMapper.selectById(id));
    }

    @Test
    public void testDeleteJoinSaleslead2manager_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2managerService.deleteJoinSaleslead2manager(id), JOIN_SALESLEAD2MANAGER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2managerPage() {
       // mock 数据
       JoinSaleslead2managerDO dbJoinSaleslead2manager = randomPojo(JoinSaleslead2managerDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setManagerId(null);
       });
       joinSaleslead2managerMapper.insert(dbJoinSaleslead2manager);
       // 测试 createTime 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setSalesleadId(null)));
       // 测试 managerId 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setManagerId(null)));
       // 准备参数
       JoinSaleslead2managerPageReqVO reqVO = new JoinSaleslead2managerPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setManagerId(null);

       // 调用
       PageResult<JoinSaleslead2managerDO> pageResult = joinSaleslead2managerService.getJoinSaleslead2managerPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbJoinSaleslead2manager, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2managerList() {
       // mock 数据
       JoinSaleslead2managerDO dbJoinSaleslead2manager = randomPojo(JoinSaleslead2managerDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setManagerId(null);
       });
       joinSaleslead2managerMapper.insert(dbJoinSaleslead2manager);
       // 测试 createTime 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setSalesleadId(null)));
       // 测试 managerId 不匹配
       joinSaleslead2managerMapper.insert(cloneIgnoreId(dbJoinSaleslead2manager, o -> o.setManagerId(null)));
       // 准备参数
       JoinSaleslead2managerExportReqVO reqVO = new JoinSaleslead2managerExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setManagerId(null);

       // 调用
       List<JoinSaleslead2managerDO> list = joinSaleslead2managerService.getJoinSaleslead2managerList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbJoinSaleslead2manager, list.get(0));
    }

}
