package cn.iocoder.yudao.module.crm.service.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivablePlanMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.RECEIVABLE_PLAN_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：后续，需要补充测试用例
/**
 * {@link CrmReceivablePlanServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CrmReceivablePlanServiceImpl.class)
public class CrmCrmReceivablePlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmReceivablePlanServiceImpl receivablePlanService;

    @Resource
    private CrmReceivablePlanMapper crmReceivablePlanMapper;

    @Test
    public void testCreateReceivablePlan_success() {
        // 准备参数
        CrmReceivablePlanCreateReqVO reqVO = randomPojo(CrmReceivablePlanCreateReqVO.class);

        // 调用
        Long receivablePlanId = receivablePlanService.createReceivablePlan(reqVO);
        // 断言
        assertNotNull(receivablePlanId);
        // 校验记录的属性是否正确
        CrmReceivablePlanDO receivablePlan = crmReceivablePlanMapper.selectById(receivablePlanId);
        assertPojoEquals(reqVO, receivablePlan);
    }

    @Test
    public void testUpdateReceivablePlan_success() {
        // mock 数据
        CrmReceivablePlanDO dbReceivablePlan = randomPojo(CrmReceivablePlanDO.class);
        crmReceivablePlanMapper.insert(dbReceivablePlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmReceivablePlanUpdateReqVO reqVO = randomPojo(CrmReceivablePlanUpdateReqVO.class, o -> {
            o.setId(dbReceivablePlan.getId()); // 设置更新的 ID
        });

        // 调用
        receivablePlanService.updateReceivablePlan(reqVO);
        // 校验是否更新正确
        CrmReceivablePlanDO receivablePlan = crmReceivablePlanMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, receivablePlan);
    }

    @Test
    public void testUpdateReceivablePlan_notExists() {
        // 准备参数
        CrmReceivablePlanUpdateReqVO reqVO = randomPojo(CrmReceivablePlanUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> receivablePlanService.updateReceivablePlan(reqVO), RECEIVABLE_PLAN_NOT_EXISTS);
    }

    @Test
    public void testDeleteReceivablePlan_success() {
        // mock 数据
        CrmReceivablePlanDO dbReceivablePlan = randomPojo(CrmReceivablePlanDO.class);
        crmReceivablePlanMapper.insert(dbReceivablePlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbReceivablePlan.getId();

        // 调用
        receivablePlanService.deleteReceivablePlan(id);
       // 校验数据不存在了
       assertNull(crmReceivablePlanMapper.selectById(id));
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
       CrmReceivablePlanDO dbReceivablePlan = randomPojo(CrmReceivablePlanDO.class, o -> { // 等会查询到
           o.setPeriod(null);
           o.setReturnTime(null);
           o.setRemindDays(null);
           o.setRemindTime(null);
           o.setCustomerId(null);
           o.setContractId(null);
           o.setOwnerUserId(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       crmReceivablePlanMapper.insert(dbReceivablePlan);
       // 测试 customerId 不匹配
       crmReceivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setCustomerId(null)));
       // 测试 contractId 不匹配
       crmReceivablePlanMapper.insert(cloneIgnoreId(dbReceivablePlan, o -> o.setContractId(null)));
       // 准备参数
       CrmReceivablePlanPageReqVO reqVO = new CrmReceivablePlanPageReqVO();
       reqVO.setCustomerId(null);
       reqVO.setContractId(null);

       // 调用
       PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbReceivablePlan, pageResult.getList().get(0));
    }

}
