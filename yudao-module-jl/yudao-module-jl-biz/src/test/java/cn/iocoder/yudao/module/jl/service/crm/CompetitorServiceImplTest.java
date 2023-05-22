package cn.iocoder.yudao.module.jl.service.crm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.CompetitorMapper;
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
 * {@link CompetitorServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(CompetitorServiceImpl.class)
public class CompetitorServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CompetitorServiceImpl competitorService;

    @Resource
    private CompetitorMapper competitorMapper;

    @Test
    public void testCreateCompetitor_success() {
        // 准备参数
        CompetitorCreateReqVO reqVO = randomPojo(CompetitorCreateReqVO.class);

        // 调用
        Long competitorId = competitorService.createCompetitor(reqVO);
        // 断言
        assertNotNull(competitorId);
        // 校验记录的属性是否正确
        CompetitorDO competitor = competitorMapper.selectById(competitorId);
        assertPojoEquals(reqVO, competitor);
    }

    @Test
    public void testUpdateCompetitor_success() {
        // mock 数据
        CompetitorDO dbCompetitor = randomPojo(CompetitorDO.class);
        competitorMapper.insert(dbCompetitor);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CompetitorUpdateReqVO reqVO = randomPojo(CompetitorUpdateReqVO.class, o -> {
            o.setId(dbCompetitor.getId()); // 设置更新的 ID
        });

        // 调用
        competitorService.updateCompetitor(reqVO);
        // 校验是否更新正确
        CompetitorDO competitor = competitorMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, competitor);
    }

    @Test
    public void testUpdateCompetitor_notExists() {
        // 准备参数
        CompetitorUpdateReqVO reqVO = randomPojo(CompetitorUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> competitorService.updateCompetitor(reqVO), COMPETITOR_NOT_EXISTS);
    }

    @Test
    public void testDeleteCompetitor_success() {
        // mock 数据
        CompetitorDO dbCompetitor = randomPojo(CompetitorDO.class);
        competitorMapper.insert(dbCompetitor);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCompetitor.getId();

        // 调用
        competitorService.deleteCompetitor(id);
       // 校验数据不存在了
       assertNull(competitorMapper.selectById(id));
    }

    @Test
    public void testDeleteCompetitor_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> competitorService.deleteCompetitor(id), COMPETITOR_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCompetitorPage() {
       // mock 数据
       CompetitorDO dbCompetitor = randomPojo(CompetitorDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setName(null);
           o.setContactName(null);
           o.setPhone(null);
           o.setType(null);
           o.setAdvantage(null);
           o.setDisadvantage(null);
           o.setMark(null);
       });
       competitorMapper.insert(dbCompetitor);
       // 测试 createTime 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setCreateTime(null)));
       // 测试 name 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setName(null)));
       // 测试 contactName 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setContactName(null)));
       // 测试 phone 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setPhone(null)));
       // 测试 type 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setType(null)));
       // 测试 advantage 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setAdvantage(null)));
       // 测试 disadvantage 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setDisadvantage(null)));
       // 测试 mark 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setMark(null)));
       // 准备参数
       CompetitorPageReqVO reqVO = new CompetitorPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setName(null);
       reqVO.setContactName(null);
       reqVO.setPhone(null);
       reqVO.setType(null);
       reqVO.setAdvantage(null);
       reqVO.setDisadvantage(null);
       reqVO.setMark(null);

       // 调用
       PageResult<CompetitorDO> pageResult = competitorService.getCompetitorPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCompetitor, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCompetitorList() {
       // mock 数据
       CompetitorDO dbCompetitor = randomPojo(CompetitorDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setName(null);
           o.setContactName(null);
           o.setPhone(null);
           o.setType(null);
           o.setAdvantage(null);
           o.setDisadvantage(null);
           o.setMark(null);
       });
       competitorMapper.insert(dbCompetitor);
       // 测试 createTime 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setCreateTime(null)));
       // 测试 name 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setName(null)));
       // 测试 contactName 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setContactName(null)));
       // 测试 phone 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setPhone(null)));
       // 测试 type 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setType(null)));
       // 测试 advantage 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setAdvantage(null)));
       // 测试 disadvantage 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setDisadvantage(null)));
       // 测试 mark 不匹配
       competitorMapper.insert(cloneIgnoreId(dbCompetitor, o -> o.setMark(null)));
       // 准备参数
       CompetitorExportReqVO reqVO = new CompetitorExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setName(null);
       reqVO.setContactName(null);
       reqVO.setPhone(null);
       reqVO.setType(null);
       reqVO.setAdvantage(null);
       reqVO.setDisadvantage(null);
       reqVO.setMark(null);

       // 调用
       List<CompetitorDO> list = competitorService.getCompetitorList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbCompetitor, list.get(0));
    }

}
