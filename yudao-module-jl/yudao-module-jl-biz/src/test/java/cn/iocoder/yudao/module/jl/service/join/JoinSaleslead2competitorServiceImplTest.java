package cn.iocoder.yudao.module.jl.service.join;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2competitorMapper;
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
 * {@link JoinSaleslead2competitorServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(JoinSaleslead2competitorServiceImpl.class)
public class JoinSaleslead2competitorServiceImplTest extends BaseDbUnitTest {

    @Resource
    private JoinSaleslead2competitorServiceImpl joinSaleslead2competitorService;

    @Resource
    private JoinSaleslead2competitorMapper joinSaleslead2competitorMapper;

    @Test
    public void testCreateJoinSaleslead2competitor_success() {
        // 准备参数
        JoinSaleslead2competitorCreateReqVO reqVO = randomPojo(JoinSaleslead2competitorCreateReqVO.class);

        // 调用
        Long joinSaleslead2competitorId = joinSaleslead2competitorService.createJoinSaleslead2competitor(reqVO);
        // 断言
        assertNotNull(joinSaleslead2competitorId);
        // 校验记录的属性是否正确
        JoinSaleslead2competitorDO joinSaleslead2competitor = joinSaleslead2competitorMapper.selectById(joinSaleslead2competitorId);
        assertPojoEquals(reqVO, joinSaleslead2competitor);
    }

    @Test
    public void testUpdateJoinSaleslead2competitor_success() {
        // mock 数据
        JoinSaleslead2competitorDO dbJoinSaleslead2competitor = randomPojo(JoinSaleslead2competitorDO.class);
        joinSaleslead2competitorMapper.insert(dbJoinSaleslead2competitor);// @Sql: 先插入出一条存在的数据
        // 准备参数
        JoinSaleslead2competitorUpdateReqVO reqVO = randomPojo(JoinSaleslead2competitorUpdateReqVO.class, o -> {
            o.setId(dbJoinSaleslead2competitor.getId()); // 设置更新的 ID
        });

        // 调用
        joinSaleslead2competitorService.updateJoinSaleslead2competitor(reqVO);
        // 校验是否更新正确
        JoinSaleslead2competitorDO joinSaleslead2competitor = joinSaleslead2competitorMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, joinSaleslead2competitor);
    }

    @Test
    public void testUpdateJoinSaleslead2competitor_notExists() {
        // 准备参数
        JoinSaleslead2competitorUpdateReqVO reqVO = randomPojo(JoinSaleslead2competitorUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2competitorService.updateJoinSaleslead2competitor(reqVO), JOIN_SALESLEAD2COMPETITOR_NOT_EXISTS);
    }

    @Test
    public void testDeleteJoinSaleslead2competitor_success() {
        // mock 数据
        JoinSaleslead2competitorDO dbJoinSaleslead2competitor = randomPojo(JoinSaleslead2competitorDO.class);
        joinSaleslead2competitorMapper.insert(dbJoinSaleslead2competitor);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbJoinSaleslead2competitor.getId();

        // 调用
        joinSaleslead2competitorService.deleteJoinSaleslead2competitor(id);
       // 校验数据不存在了
       assertNull(joinSaleslead2competitorMapper.selectById(id));
    }

    @Test
    public void testDeleteJoinSaleslead2competitor_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> joinSaleslead2competitorService.deleteJoinSaleslead2competitor(id), JOIN_SALESLEAD2COMPETITOR_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2competitorPage() {
       // mock 数据
       JoinSaleslead2competitorDO dbJoinSaleslead2competitor = randomPojo(JoinSaleslead2competitorDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setCompetitorId(null);
           o.setCompetitorQuotation(null);
       });
       joinSaleslead2competitorMapper.insert(dbJoinSaleslead2competitor);
       // 测试 createTime 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setSalesleadId(null)));
       // 测试 competitorId 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCompetitorId(null)));
       // 测试 competitorQuotation 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCompetitorQuotation(null)));
       // 准备参数
       JoinSaleslead2competitorPageReqVO reqVO = new JoinSaleslead2competitorPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setCompetitorId(null);
       reqVO.setCompetitorQuotation(null);

       // 调用
       PageResult<JoinSaleslead2competitorDO> pageResult = joinSaleslead2competitorService.getJoinSaleslead2competitorPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbJoinSaleslead2competitor, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinSaleslead2competitorList() {
       // mock 数据
       JoinSaleslead2competitorDO dbJoinSaleslead2competitor = randomPojo(JoinSaleslead2competitorDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setCompetitorId(null);
           o.setCompetitorQuotation(null);
       });
       joinSaleslead2competitorMapper.insert(dbJoinSaleslead2competitor);
       // 测试 createTime 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setSalesleadId(null)));
       // 测试 competitorId 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCompetitorId(null)));
       // 测试 competitorQuotation 不匹配
       joinSaleslead2competitorMapper.insert(cloneIgnoreId(dbJoinSaleslead2competitor, o -> o.setCompetitorQuotation(null)));
       // 准备参数
       JoinSaleslead2competitorExportReqVO reqVO = new JoinSaleslead2competitorExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setCompetitorId(null);
       reqVO.setCompetitorQuotation(null);

       // 调用
       List<JoinSaleslead2competitorDO> list = joinSaleslead2competitorService.getJoinSaleslead2competitorList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbJoinSaleslead2competitor, list.get(0));
    }

}
