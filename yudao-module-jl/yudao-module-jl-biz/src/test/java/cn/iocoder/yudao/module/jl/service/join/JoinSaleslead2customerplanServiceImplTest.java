package cn.iocoder.yudao.module.jl.service.join;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2customerplanMapper;
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
 * {@link JoinSaleslead2customerplanServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(JoinSaleslead2customerplanServiceImpl.class)
public class JoinSaleslead2customerplanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private JoinSaleslead2customerplanServiceImpl joinSaleslead2customerplanService;

    @Resource
    private JoinSaleslead2customerplanMapper joinSaleslead2customerplanMapper;

    @Test
    public void testCreateJoinSaleslead2customerplan_success() {
        // 准备参数
        JoinSaleslead2customerplanCreateReqVO reqVO = randomPojo(JoinSaleslead2customerplanCreateReqVO.class);

        // 调用
        Long joinSaleslead2customerplanId = joinSaleslead2customerplanService.createJoinSaleslead2customerplan(reqVO);
        // 断言
        assertNotNull(joinSaleslead2customerplanId);
        // 校验记录的属性是否正确
        JoinSaleslead2customerplanDO joinSaleslead2customerplan = joinSaleslead2customerplanMapper.selectById(joinSaleslead2customerplanId);
        assertPojoEquals(reqVO, joinSaleslead2customerplan);
    }

    @Test
    public void testUpdateJoinSaleslead2customerplan_success() {
        // mock 数据
        JoinSaleslead2customerplanDO dbJoinSaleslead2customerplan = randomPojo(JoinSaleslead2customerplanDO.class);
        joinSaleslead2customerplanMapper.insert(dbJoinSaleslead2customerplan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        JoinSaleslead2customerplanUpdateReqVO reqVO = randomPojo(JoinSaleslead2customerplanUpdateReqVO.class, o -> {
            o.setId(dbJoinSaleslead2customerplan.getId()); // 设置更新的 ID
        });

        // 调用
        joinSaleslead2customerplanService.updateJoinSaleslead2customerplan(reqVO);
        // 校验是否更新正确
        JoinSaleslead2customerplanDO joinSaleslead2customerplan = joinSaleslead2customerplanMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, joinSaleslead2customerplan);
    }

    @Test
    public void testUpdateJoinSaleslead2customerplan_notExists() {
        // 准备参数
        JoinSaleslead2customerplanUpdateReqVO reqVO = randomPojo(JoinSaleslead2customerplanUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2customerplanService.updateJoinSaleslead2customerplan(reqVO), JOIN_SALESLEAD2CUSTOMERPLAN_NOT_EXISTS);
    }

    @Test
    public void testDeleteJoinSaleslead2customerplan_success() {
        // mock 数据
        JoinSaleslead2customerplanDO dbJoinSaleslead2customerplan = randomPojo(JoinSaleslead2customerplanDO.class);
        joinSaleslead2customerplanMapper.insert(dbJoinSaleslead2customerplan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbJoinSaleslead2customerplan.getId();

        // 调用
        joinSaleslead2customerplanService.deleteJoinSaleslead2customerplan(id);
       // 校验数据不存在了
       assertNull(joinSaleslead2customerplanMapper.selectById(id));
    }

    @Test
    public void testDeleteJoinSaleslead2customerplan_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2customerplanService.deleteJoinSaleslead2customerplan(id), JOIN_SALESLEAD2CUSTOMERPLAN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2customerplanPage() {
       // mock 数据
       JoinSaleslead2customerplanDO dbJoinSaleslead2customerplan = randomPojo(JoinSaleslead2customerplanDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setFileUrl(null);
           o.setFileName(null);
       });
       joinSaleslead2customerplanMapper.insert(dbJoinSaleslead2customerplan);
       // 测试 createTime 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setSalesleadId(null)));
       // 测试 fileUrl 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setFileUrl(null)));
       // 测试 fileName 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setFileName(null)));
       // 准备参数
       JoinSaleslead2customerplanPageReqVO reqVO = new JoinSaleslead2customerplanPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setFileUrl(null);
       reqVO.setFileName(null);

       // 调用
       PageResult<JoinSaleslead2customerplanDO> pageResult = joinSaleslead2customerplanService.getJoinSaleslead2customerplanPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbJoinSaleslead2customerplan, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2customerplanList() {
       // mock 数据
       JoinSaleslead2customerplanDO dbJoinSaleslead2customerplan = randomPojo(JoinSaleslead2customerplanDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setFileUrl(null);
           o.setFileName(null);
       });
       joinSaleslead2customerplanMapper.insert(dbJoinSaleslead2customerplan);
       // 测试 createTime 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setSalesleadId(null)));
       // 测试 fileUrl 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setFileUrl(null)));
       // 测试 fileName 不匹配
       joinSaleslead2customerplanMapper.insert(cloneIgnoreId(dbJoinSaleslead2customerplan, o -> o.setFileName(null)));
       // 准备参数
       JoinSaleslead2customerplanExportReqVO reqVO = new JoinSaleslead2customerplanExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setFileUrl(null);
       reqVO.setFileName(null);

       // 调用
       List<JoinSaleslead2customerplanDO> list = joinSaleslead2customerplanService.getJoinSaleslead2customerplanList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbJoinSaleslead2customerplan, list.get(0));
    }

}
