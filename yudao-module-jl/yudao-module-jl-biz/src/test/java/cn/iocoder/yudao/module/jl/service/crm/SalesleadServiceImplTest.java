package cn.iocoder.yudao.module.jl.service.crm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.SalesleadMapper;
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
 * {@link SalesleadServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(SalesleadServiceImpl.class)
public class SalesleadServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SalesleadServiceImpl salesleadService;

    @Resource
    private SalesleadMapper salesleadMapper;

    @Test
    public void testCreateSaleslead_success() {
        // 准备参数
        SalesleadCreateReqVO reqVO = randomPojo(SalesleadCreateReqVO.class);

        // 调用
        Long salesleadId = salesleadService.createSaleslead(reqVO);
        // 断言
        assertNotNull(salesleadId);
        // 校验记录的属性是否正确
        SalesleadDO saleslead = salesleadMapper.selectById(salesleadId);
        assertPojoEquals(reqVO, saleslead);
    }

    @Test
    public void testUpdateSaleslead_success() {
        // mock 数据
        SalesleadDO dbSaleslead = randomPojo(SalesleadDO.class);
        salesleadMapper.insert(dbSaleslead);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SalesleadUpdateReqVO reqVO = randomPojo(SalesleadUpdateReqVO.class, o -> {
            o.setId(dbSaleslead.getId()); // 设置更新的 ID
        });

        // 调用
        salesleadService.updateSaleslead(reqVO);
        // 校验是否更新正确
        SalesleadDO saleslead = salesleadMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, saleslead);
    }

    @Test
    public void testUpdateSaleslead_notExists() {
        // 准备参数
        SalesleadUpdateReqVO reqVO = randomPojo(SalesleadUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> salesleadService.updateSaleslead(reqVO), SALESLEAD_NOT_EXISTS);
    }

    @Test
    public void testDeleteSaleslead_success() {
        // mock 数据
        SalesleadDO dbSaleslead = randomPojo(SalesleadDO.class);
        salesleadMapper.insert(dbSaleslead);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSaleslead.getId();

        // 调用
        salesleadService.deleteSaleslead(id);
       // 校验数据不存在了
       assertNull(salesleadMapper.selectById(id));
    }

    @Test
    public void testDeleteSaleslead_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> salesleadService.deleteSaleslead(id), SALESLEAD_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSalesleadPage() {
       // mock 数据
       SalesleadDO dbSaleslead = randomPojo(SalesleadDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSource(null);
           o.setRequirement(null);
           o.setBudget(null);
           o.setQuotation(null);
           o.setStatus(null);
           o.setCustomerId(null);
           o.setProjectId(null);
       });
       salesleadMapper.insert(dbSaleslead);
       // 测试 createTime 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setCreateTime(null)));
       // 测试 source 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setSource(null)));
       // 测试 requirement 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setRequirement(null)));
       // 测试 budget 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setBudget(null)));
       // 测试 quotation 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setQuotation(null)));
       // 测试 status 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setStatus(null)));
       // 测试 customerId 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setCustomerId(null)));
       // 测试 projectId 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setProjectId(null)));
       // 准备参数
       SalesleadPageReqVO reqVO = new SalesleadPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSource(null);
       reqVO.setRequirement(null);
       reqVO.setBudget(null);
       reqVO.setQuotation(null);
       reqVO.setStatus(null);
       reqVO.setCustomerId(null);
       reqVO.setProjectId(null);

       // 调用
       PageResult<SalesleadDO> pageResult = salesleadService.getSalesleadPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSaleslead, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSalesleadList() {
       // mock 数据
       SalesleadDO dbSaleslead = randomPojo(SalesleadDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSource(null);
           o.setRequirement(null);
           o.setBudget(null);
           o.setQuotation(null);
           o.setStatus(null);
           o.setCustomerId(null);
           o.setProjectId(null);
       });
       salesleadMapper.insert(dbSaleslead);
       // 测试 createTime 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setCreateTime(null)));
       // 测试 source 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setSource(null)));
       // 测试 requirement 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setRequirement(null)));
       // 测试 budget 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setBudget(null)));
       // 测试 quotation 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setQuotation(null)));
       // 测试 status 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setStatus(null)));
       // 测试 customerId 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setCustomerId(null)));
       // 测试 projectId 不匹配
       salesleadMapper.insert(cloneIgnoreId(dbSaleslead, o -> o.setProjectId(null)));
       // 准备参数
       SalesleadExportReqVO reqVO = new SalesleadExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSource(null);
       reqVO.setRequirement(null);
       reqVO.setBudget(null);
       reqVO.setQuotation(null);
       reqVO.setStatus(null);
       reqVO.setCustomerId(null);
       reqVO.setProjectId(null);

       // 调用
       List<SalesleadDO> list = salesleadService.getSalesleadList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSaleslead, list.get(0));
    }

}
