package cn.iocoder.yudao.module.jl.service.join;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2reportMapper;
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
 * {@link JoinSaleslead2reportServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(JoinSaleslead2reportServiceImpl.class)
public class JoinSaleslead2reportServiceImplTest extends BaseDbUnitTest {

    @Resource
    private JoinSaleslead2reportServiceImpl joinSaleslead2reportService;

    @Resource
    private JoinSaleslead2reportMapper joinSaleslead2reportMapper;

    @Test
    public void testCreateJoinSaleslead2report_success() {
        // 准备参数
        JoinSaleslead2reportCreateReqVO reqVO = randomPojo(JoinSaleslead2reportCreateReqVO.class);

        // 调用
        Long joinSaleslead2reportId = joinSaleslead2reportService.createJoinSaleslead2report(reqVO);
        // 断言
        assertNotNull(joinSaleslead2reportId);
        // 校验记录的属性是否正确
        JoinSaleslead2reportDO joinSaleslead2report = joinSaleslead2reportMapper.selectById(joinSaleslead2reportId);
        assertPojoEquals(reqVO, joinSaleslead2report);
    }

    @Test
    public void testUpdateJoinSaleslead2report_success() {
        // mock 数据
        JoinSaleslead2reportDO dbJoinSaleslead2report = randomPojo(JoinSaleslead2reportDO.class);
        joinSaleslead2reportMapper.insert(dbJoinSaleslead2report);// @Sql: 先插入出一条存在的数据
        // 准备参数
        JoinSaleslead2reportUpdateReqVO reqVO = randomPojo(JoinSaleslead2reportUpdateReqVO.class, o -> {
            o.setId(dbJoinSaleslead2report.getId()); // 设置更新的 ID
        });

        // 调用
        joinSaleslead2reportService.updateJoinSaleslead2report(reqVO);
        // 校验是否更新正确
        JoinSaleslead2reportDO joinSaleslead2report = joinSaleslead2reportMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, joinSaleslead2report);
    }

    @Test
    public void testUpdateJoinSaleslead2report_notExists() {
        // 准备参数
        JoinSaleslead2reportUpdateReqVO reqVO = randomPojo(JoinSaleslead2reportUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2reportService.updateJoinSaleslead2report(reqVO), JOIN_SALESLEAD2REPORT_NOT_EXISTS);
    }

    @Test
    public void testDeleteJoinSaleslead2report_success() {
        // mock 数据
        JoinSaleslead2reportDO dbJoinSaleslead2report = randomPojo(JoinSaleslead2reportDO.class);
        joinSaleslead2reportMapper.insert(dbJoinSaleslead2report);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbJoinSaleslead2report.getId();

        // 调用
        joinSaleslead2reportService.deleteJoinSaleslead2report(id);
       // 校验数据不存在了
       assertNull(joinSaleslead2reportMapper.selectById(id));
    }

    @Test
    public void testDeleteJoinSaleslead2report_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2reportService.deleteJoinSaleslead2report(id), JOIN_SALESLEAD2REPORT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2reportPage() {
       // mock 数据
       JoinSaleslead2reportDO dbJoinSaleslead2report = randomPojo(JoinSaleslead2reportDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setFileUrl(null);
           o.setFileName(null);
       });
       joinSaleslead2reportMapper.insert(dbJoinSaleslead2report);
       // 测试 createTime 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setSalesleadId(null)));
       // 测试 fileUrl 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setFileUrl(null)));
       // 测试 fileName 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setFileName(null)));
       // 准备参数
       JoinSaleslead2reportPageReqVO reqVO = new JoinSaleslead2reportPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setFileUrl(null);
       reqVO.setFileName(null);

       // 调用
       PageResult<JoinSaleslead2reportDO> pageResult = joinSaleslead2reportService.getJoinSaleslead2reportPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbJoinSaleslead2report, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2reportList() {
       // mock 数据
       JoinSaleslead2reportDO dbJoinSaleslead2report = randomPojo(JoinSaleslead2reportDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setFileUrl(null);
           o.setFileName(null);
       });
       joinSaleslead2reportMapper.insert(dbJoinSaleslead2report);
       // 测试 createTime 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setSalesleadId(null)));
       // 测试 fileUrl 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setFileUrl(null)));
       // 测试 fileName 不匹配
       joinSaleslead2reportMapper.insert(cloneIgnoreId(dbJoinSaleslead2report, o -> o.setFileName(null)));
       // 准备参数
       JoinSaleslead2reportExportReqVO reqVO = new JoinSaleslead2reportExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setFileUrl(null);
       reqVO.setFileName(null);

       // 调用
       List<JoinSaleslead2reportDO> list = joinSaleslead2reportService.getJoinSaleslead2reportList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbJoinSaleslead2report, list.get(0));
    }

}
