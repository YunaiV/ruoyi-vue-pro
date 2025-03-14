//package cn.iocoder.yudao.module.erp.service.logistic.customrule;
//
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRulePageReqVO;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
//import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
//import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
//import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
//import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
//import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
//import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
//import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
//import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//
///**
// * {@link ErpCustomRuleServiceImpl} 的单元测试类
// *
// * @author 索迈管理员
// */
//@Import(ErpCustomRuleServiceImpl.class)
//public class ErpCustomRuleServiceImplTest extends BaseDbUnitTest {
//
//    @Resource
//    private ErpCustomRuleServiceImpl customRuleService;
//
//    @Resource
//    private ErpCustomRuleMapper customRuleMapper;
//
//    @Test
//    public void testCreateCustomRule_success() {
//        // 准备参数
//        ErpCustomRuleSaveReqVO createReqVO = randomPojo(ErpCustomRuleSaveReqVO.class).setId(null);
//
//        // 调用
//        Long customRuleId = customRuleService.createCustomRule(createReqVO);
//        // 断言
//        assertNotNull(customRuleId);
//        // 校验记录的属性是否正确
//        ErpCustomRuleDO customRule = customRuleMapper.selectById(customRuleId);
//        assertPojoEquals(createReqVO, customRule, "id");
//    }
//
//    @Test
//    public void testUpdateCustomRule_success() {
//        // mock 数据
//        ErpCustomRuleDO dbCustomRule = randomPojo(ErpCustomRuleDO.class);
//        customRuleMapper.insert(dbCustomRule);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        ErpCustomRuleSaveReqVO updateReqVO = randomPojo(ErpCustomRuleSaveReqVO.class, o -> {
//            o.setId(dbCustomRule.getId()); // 设置更新的 ID
//        });
//
//        // 调用
//        customRuleService.updateCustomRule(updateReqVO);
//        // 校验是否更新正确
//        ErpCustomRuleDO customRule = customRuleMapper.selectById(updateReqVO.getId()); // 获取最新的
//        assertPojoEquals(updateReqVO, customRule);
//    }
//
//    @Test
//    public void testUpdateCustomRule_notExists() {
//        // 准备参数
//        ErpCustomRuleSaveReqVO updateReqVO = randomPojo(ErpCustomRuleSaveReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleService.updateCustomRule(updateReqVO), CUSTOM_RULE_NOT_EXISTS);
//    }
//
//    @Test
//    public void testDeleteCustomRule_success() {
//        // mock 数据
//        ErpCustomRuleDO dbCustomRule = randomPojo(ErpCustomRuleDO.class);
//        customRuleMapper.insert(dbCustomRule);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        Long id = dbCustomRule.getId();
//
//        // 调用
//        customRuleService.deleteCustomRule(id);
//        // 校验数据不存在了
//        assertNull(customRuleMapper.selectById(id));
//    }
//
//    @Test
//    public void testDeleteCustomRule_notExists() {
//        // 准备参数
//        Long id = randomLongId();
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleService.deleteCustomRule(id), CUSTOM_RULE_NOT_EXISTS);
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetCustomRulePage() {
//        // mock 数据
//        ErpCustomRuleDO dbCustomRule = randomPojo(ErpCustomRuleDO.class, o -> { // 等会查询到
//            o.setCountryCode(null);
////           o.setType(null);
////           o.setSupplierProductId(null);
//            o.setDeclaredValue(null);
//            o.setDeclaredValueCurrencyCode(null);
//            o.setLogisticAttribute(null);
//            o.setCreateTime(null);
//        });
//        customRuleMapper.insert(dbCustomRule);
//        // 测试 countryCode 不匹配
//        customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setCountryCode(null)));
//        // 测试 type 不匹配
////       customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setType(null)));
//        // 测试 supplierProductId 不匹配
////       customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setSupplierProductId(null)));
//        // 测试 declaredTypeEn 不匹配
//        // 测试 declaredValue 不匹配
//        customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setDeclaredValue(null)));
//        // 测试 declaredValueCurrencyCode 不匹配
//        customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setDeclaredValueCurrencyCode(null)));
//        // 测试 hscode 不匹配
//        // 测试 logisticAttribute 不匹配
//        customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setLogisticAttribute(null)));
//        // 测试 createTime 不匹配
//        customRuleMapper.insert(cloneIgnoreId(dbCustomRule, o -> o.setCreateTime(null)));
//        // 准备参数
//        ErpCustomRulePageReqVO reqVO = new ErpCustomRulePageReqVO();
//        reqVO.setCountryCode(null);
////       reqVO.setType(null);
////       reqVO.setSupplierProductId(null);
//        reqVO.setDeclaredTypeEn(null);
//        reqVO.setDeclaredType(null);
//        reqVO.setDeclaredValue(null);
//        reqVO.setDeclaredValueCurrencyCode(null);
//        reqVO.setTaxRate(null);
//        reqVO.setHscode(null);
//        reqVO.setLogisticAttribute(null);
//        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//
//        // 调用
////        PageResult<ErpCustomRuleDO> pageResult = customRuleService.getCustomRulePage(reqVO);
//        // 断言
////        assertEquals(1, pageResult.getTotal());
////        assertEquals(1, pageResult.getList().size());
////        assertPojoEquals(dbCustomRule, pageResult.getList().get(0));
//    }
//
//}