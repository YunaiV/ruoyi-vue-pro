package cn.iocoder.yudao.module.crm.service.receivable;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivablePlanMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ReceivablePlanServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ReceivablePlanServiceImpl.class)
public class ReceivablePlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ReceivablePlanServiceImpl receivablePlanService;

    @Resource
    private ReceivablePlanMapper receivablePlanMapper;

    @Test
    public void testCreateReceivablePlan_success() {
        // 准备参数
        ReceivablePlanCreateReqVO reqVO = randomPojo(ReceivablePlanCreateReqVO.class);

        // 调用
        Long receivablePlanId = receivablePlanService.createReceivablePlan(reqVO);
        // 断言
        assertNotNull(receivablePlanId);
        // 校验记录的属性是否正确
        ReceivablePlanDO receivablePlan = receivablePlanMapper.selectById(receivablePlanId);
        assertPojoEquals(reqVO, receivablePlan);
    }

    @Test
    public void testUpdateReceivablePlan_success() {
        // mock 数据
        ReceivablePlanDO dbReceivablePlan = randomPojo(ReceivablePlanDO.class);
        receivablePlanMapper.insert(dbReceivablePlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ReceivablePlanUpdateReqVO reqVO = randomPojo(ReceivablePlanUpdateReqVO.class, o -> {
            o.setId(dbReceivablePlan.getId()); // 设置更新的 ID
        });

        // 调用
        receivablePlanService.updateReceivablePlan(reqVO);
        // 校验是否更新正确
        ReceivablePlanDO receivablePlan = receivablePlanMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, receivablePlan);
    }

    @Test
    public void testUpdateReceivablePlan_notExists() {
        // 准备参数
        ReceivablePlanUpdateReqVO reqVO = randomPojo(ReceivablePlanUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> receivablePlanService.updateReceivablePlan(reqVO), RECEIVABLE_PLAN_NOT_EXISTS);
    }

    @Test
    public void testDeleteReceivablePlan_success() {
        // mock 数据
        ReceivablePlanDO dbReceivablePlan = randomPojo(ReceivablePlanDO.class);
        receivablePlanMapper.insert(dbReceivablePlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbReceivablePlan.getId();

        // 调用
        receivablePlanService.deleteReceivablePlan(id);
       // 校验数据不存在了
       assertNull(receivablePlanMapper.selectById(id));
    }

    @Test
    public void testDeleteReceivablePlan_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> receivablePlanService.deleteReceivablePlan(id), RECEIVABLE_PLAN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetReceivablePlanPage() {
       // mock 数据
       ReceivablePlanDO dbReceivablePlan = randomPojo(ReceivablePlanDO.class, o -> { // 等会查询到
           o.setIndexNo(null);
           o.setStatus(null);
           o.setCheckStatus(null);
           o.setReturnTime(null);
           o.setRemindDays(null);
           o.setRemindTime(null);
           o.setCustomerId(null);
           o.setContractId(null);
           o.setOwnerUserId(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       receivablePlanMapper.insert(dbReceivablePlan);
       // 测试 indexNo 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setIndexNo(null)));
       // 测试 status 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setStatus(null)));
       // 测试 checkStatus 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCheckStatus(null)));
       // 测试 returnTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setReturnTime(null)));
       // 测试 remindDays 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemindDays(null)));
       // 测试 remindTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemindTime(null)));
       // 测试 customerId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCustomerId(null)));
       // 测试 contractId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setContractId(null)));
       // 测试 ownerUserId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setOwnerUserId(null)));
       // 测试 remark 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCreateTime(null)));
       // 准备参数
       ReceivablePlanPageReqVO reqVO = new ReceivablePlanPageReqVO();
       reqVO.setIndexNo(null);
       reqVO.setStatus(null);
       reqVO.setCheckStatus(null);
       reqVO.setReturnTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemindDays(null);
       reqVO.setRemindTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCustomerId(null);
       reqVO.setContractId(null);
       reqVO.setOwnerUserId(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbReceivablePlan, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetReceivablePlanList() {
       // mock 数据
       ReceivablePlanDO dbReceivablePlan = randomPojo(ReceivablePlanDO.class, o -> { // 等会查询到
           o.setIndexNo(null);
           o.setStatus(null);
           o.setCheckStatus(null);
           o.setReturnTime(null);
           o.setRemindDays(null);
           o.setRemindTime(null);
           o.setCustomerId(null);
           o.setContractId(null);
           o.setOwnerUserId(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       receivablePlanMapper.insert(dbReceivablePlan);
       // 测试 indexNo 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setIndexNo(null)));
       // 测试 status 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setStatus(null)));
       // 测试 checkStatus 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCheckStatus(null)));
       // 测试 returnTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setReturnTime(null)));
       // 测试 remindDays 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemindDays(null)));
       // 测试 remindTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemindTime(null)));
       // 测试 customerId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCustomerId(null)));
       // 测试 contractId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setContractId(null)));
       // 测试 ownerUserId 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setOwnerUserId(null)));
       // 测试 remark 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       receivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCreateTime(null)));
       // 准备参数
       ReceivablePlanExportReqVO reqVO = new ReceivablePlanExportReqVO();
       reqVO.setIndexNo(null);
       reqVO.setStatus(null);
       reqVO.setCheckStatus(null);
       reqVO.setReturnTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemindDays(null);
       reqVO.setRemindTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCustomerId(null);
       reqVO.setContractId(null);
       reqVO.setOwnerUserId(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<ReceivablePlanDO> list = receivablePlanService.getReceivablePlanList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbReceivablePlan, list.get(0));
    }

}
