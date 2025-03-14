//package cn.iocoder.yudao.module.erp.service.logistic.category;
//
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
//import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.ErpCustomCategoryDO;
//import cn.iocoder.yudao.module.erp.dal.mysql.logistic.category.ErpCustomCategoryMapper;
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
//import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_CATEGORY_NOT_EXISTS;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * {@link ErpCustomCategoryServiceImpl} 的单元测试类
// *
// * @author 王岽宇
// */
//@Import(ErpCustomCategoryServiceImpl.class)
//public class ErpCustomCategoryServiceImplTest extends BaseDbUnitTest {
//
//    @Resource
//    private ErpCustomCategoryServiceImpl customRuleCategoryService;
//
//    @Resource
//    private ErpCustomCategoryMapper customRuleCategoryMapper;
//
//    @Test
//    public void testCreateCustomRuleCategory_success() {
//        // 准备参数
//        ErpCustomCategorySaveReqVO createReqVO = randomPojo(ErpCustomCategorySaveReqVO.class).setId(null);
//
//        // 调用
//        Long customRuleCategoryId = customRuleCategoryService.createCustomRuleCategory(createReqVO);
//        // 断言
//        assertNotNull(customRuleCategoryId);
//        // 校验记录的属性是否正确
//        ErpCustomCategoryDO customRuleCategory = customRuleCategoryMapper.selectById(customRuleCategoryId);
//        assertPojoEquals(createReqVO, customRuleCategory, "id");
//    }
//
//    @Test
//    public void testUpdateCustomRuleCategory_success() {
//        // mock 数据
//        ErpCustomCategoryDO dbCustomRuleCategory = randomPojo(ErpCustomCategoryDO.class);
//        customRuleCategoryMapper.insert(dbCustomRuleCategory);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        ErpCustomCategorySaveReqVO updateReqVO = randomPojo(ErpCustomCategorySaveReqVO.class, o -> {
//            o.setId(dbCustomRuleCategory.getId()); // 设置更新的 ID
//        });
//
//        // 调用
//        customRuleCategoryService.updateCustomRuleCategory(updateReqVO);
//        // 校验是否更新正确
//        ErpCustomCategoryDO customRuleCategory = customRuleCategoryMapper.selectById(updateReqVO.getId()); // 获取最新的
//        assertPojoEquals(updateReqVO, customRuleCategory);
//    }
//
//    @Test
//    public void testUpdateCustomRuleCategory_notExists() {
//        // 准备参数
//        ErpCustomCategorySaveReqVO updateReqVO = randomPojo(ErpCustomCategorySaveReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleCategoryService.updateCustomRuleCategory(updateReqVO), CUSTOM_RULE_CATEGORY_NOT_EXISTS);
//    }
//
//    @Test
//    public void testDeleteCustomRuleCategory_success() {
//        // mock 数据
//        ErpCustomCategoryDO dbCustomRuleCategory = randomPojo(ErpCustomCategoryDO.class);
//        customRuleCategoryMapper.insert(dbCustomRuleCategory);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        Long id = dbCustomRuleCategory.getId();
//
//        // 调用
//        customRuleCategoryService.deleteCustomRuleCategory(id);
//        // 校验数据不存在了
//        assertNull(customRuleCategoryMapper.selectById(id));
//    }
//
//    @Test
//    public void testDeleteCustomRuleCategory_notExists() {
//        // 准备参数
//        Long id = randomLongId();
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleCategoryService.deleteCustomRuleCategory(id), CUSTOM_RULE_CATEGORY_NOT_EXISTS);
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetCustomRuleCategoryPage() {
//        // mock 数据
//        ErpCustomCategoryDO dbCustomRuleCategory = randomPojo(ErpCustomCategoryDO.class, o -> { // 等会查询到
//            o.setCreateTime(null);
//            o.setMaterial(null);
//            o.setDeclaredType(null);
//            o.setDeclaredTypeEn(null);
//        });
//        customRuleCategoryMapper.insert(dbCustomRuleCategory);
//        // 测试 createTime 不匹配
//        customRuleCategoryMapper.insert(cloneIgnoreId(dbCustomRuleCategory, o -> o.setCreateTime(null)));
//        // 测试 material 不匹配
//        customRuleCategoryMapper.insert(cloneIgnoreId(dbCustomRuleCategory, o -> o.setMaterial(null)));
//        // 测试 declaredType 不匹配
//        customRuleCategoryMapper.insert(cloneIgnoreId(dbCustomRuleCategory, o -> o.setDeclaredType(null)));
//        // 测试 declaredTypeEn 不匹配
//        customRuleCategoryMapper.insert(cloneIgnoreId(dbCustomRuleCategory, o -> o.setDeclaredTypeEn(null)));
//        // 准备参数
//        ErpCustomCategoryPageReqVO reqVO = new ErpCustomCategoryPageReqVO();
//        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//        reqVO.setMaterial(null);
//        reqVO.setDeclaredType(null);
//        reqVO.setDeclaredTypeEn(null);
//
//        // 调用
//        PageResult<ErpCustomCategoryDO> pageResult = customRuleCategoryService.getCustomRuleCategoryPage(reqVO);
//        // 断言
//        assertEquals(1, pageResult.getTotal());
//        assertEquals(1, pageResult.getList().size());
//        assertPojoEquals(dbCustomRuleCategory, pageResult.getList().get(0));
//    }
//
//}