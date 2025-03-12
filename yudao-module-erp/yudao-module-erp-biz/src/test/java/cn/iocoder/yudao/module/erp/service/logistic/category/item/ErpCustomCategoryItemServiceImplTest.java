//package cn.iocoder.yudao.module.erp.service.logistic.category.item;
//
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemPageReqVO;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSaveReqVO;
//import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
//import cn.iocoder.yudao.module.erp.dal.mysql.logistic.category.item.ErpCustomCategoryItemMapper;
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
//import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * {@link ErpCustomCategoryItemServiceImpl} 的单元测试类
// *
// * @author 王岽宇
// */
//@Import(ErpCustomCategoryItemServiceImpl.class)
//public class ErpCustomCategoryItemServiceImplTest extends BaseDbUnitTest {
//
//    @Resource
//    private ErpCustomCategoryItemServiceImpl customRuleCategoryItemService;
//
//    @Resource
//    private ErpCustomCategoryItemMapper customRuleCategoryItemMapper;
//
//    @Test
//    public void testCreateCustomRuleCategoryItem_success() {
//        // 准备参数
//        ErpCustomCategoryItemSaveReqVO createReqVO = randomPojo(ErpCustomCategoryItemSaveReqVO.class).setId(null);
//
//        // 调用
//        Long customRuleCategoryItemId = customRuleCategoryItemService.createCustomRuleCategoryItem(createReqVO);
//        // 断言
//        assertNotNull(customRuleCategoryItemId);
//        // 校验记录的属性是否正确
//        ErpCustomCategoryItemDO customRuleCategoryItem = customRuleCategoryItemMapper.selectById(customRuleCategoryItemId);
//        assertPojoEquals(createReqVO, customRuleCategoryItem, "id");
//    }
//
//    @Test
//    public void testUpdateCustomRuleCategoryItem_success() {
//        // mock 数据
//        ErpCustomCategoryItemDO dbCustomRuleCategoryItem = randomPojo(ErpCustomCategoryItemDO.class);
//        customRuleCategoryItemMapper.insert(dbCustomRuleCategoryItem);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        ErpCustomCategoryItemSaveReqVO updateReqVO = randomPojo(ErpCustomCategoryItemSaveReqVO.class, o -> {
//            o.setId(dbCustomRuleCategoryItem.getId()); // 设置更新的 ID
//        });
//
//        // 调用
//        customRuleCategoryItemService.updateCustomRuleCategoryItem(updateReqVO);
//        // 校验是否更新正确
//        ErpCustomCategoryItemDO customRuleCategoryItem = customRuleCategoryItemMapper.selectById(updateReqVO.getId()); // 获取最新的
//        assertPojoEquals(updateReqVO, customRuleCategoryItem);
//    }
//
//    @Test
//    public void testUpdateCustomRuleCategoryItem_notExists() {
//        // 准备参数
//        ErpCustomCategoryItemSaveReqVO updateReqVO = randomPojo(ErpCustomCategoryItemSaveReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleCategoryItemService.updateCustomRuleCategoryItem(updateReqVO), CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS);
//    }
//
//    @Test
//    public void testDeleteCustomRuleCategoryItem_success() {
//        // mock 数据
//        ErpCustomCategoryItemDO dbCustomRuleCategoryItem = randomPojo(ErpCustomCategoryItemDO.class);
//        customRuleCategoryItemMapper.insert(dbCustomRuleCategoryItem);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        Long id = dbCustomRuleCategoryItem.getId();
//
//        // 调用
//        customRuleCategoryItemService.deleteCustomRuleCategoryItem(id);
//        // 校验数据不存在了
//        assertNull(customRuleCategoryItemMapper.selectById(id));
//    }
//
//    @Test
//    public void testDeleteCustomRuleCategoryItem_notExists() {
//        // 准备参数
//        Long id = randomLongId();
//
//        // 调用, 并断言异常
//        assertServiceException(() -> customRuleCategoryItemService.deleteCustomRuleCategoryItem(id), CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS);
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetCustomRuleCategoryItemPage() {
//        // mock 数据
//        ErpCustomCategoryItemDO dbCustomRuleCategoryItem = randomPojo(ErpCustomCategoryItemDO.class, o -> { // 等会查询到
//            o.setCustomCategoryId(null);
//            o.setCountryCode(null);
//            o.setTaxRate(null);
//            o.setCreateTime(null);
//        });
//        customRuleCategoryItemMapper.insert(dbCustomRuleCategoryItem);
//        // 测试 categoryId 不匹配
//        customRuleCategoryItemMapper.insert(cloneIgnoreId(dbCustomRuleCategoryItem, o -> o.setCustomCategoryId(null)));
//        // 测试 countryCode 不匹配
//        customRuleCategoryItemMapper.insert(cloneIgnoreId(dbCustomRuleCategoryItem, o -> o.setCountryCode(null)));
//        // 测试 hsCode 不匹配
//        // 测试 taxRate 不匹配
//        customRuleCategoryItemMapper.insert(cloneIgnoreId(dbCustomRuleCategoryItem, o -> o.setTaxRate(null)));
//        // 测试 createTime 不匹配
//        customRuleCategoryItemMapper.insert(cloneIgnoreId(dbCustomRuleCategoryItem, o -> o.setCreateTime(null)));
//        // 准备参数
//        ErpCustomCategoryItemPageReqVO reqVO = new ErpCustomCategoryItemPageReqVO();
////        reqVO.setCategoryId(null);
//        reqVO.setCountryCode(null);
////        reqVO.setHsCode(null);
//        reqVO.setTaxRate(null);
//        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//
//        // 调用
//        PageResult<ErpCustomCategoryItemDO> pageResult = customRuleCategoryItemService.getCustomRuleCategoryItemPage(reqVO);
//        // 断言
//        assertEquals(1, pageResult.getTotal());
//        assertEquals(1, pageResult.getList().size());
//        assertPojoEquals(dbCustomRuleCategoryItem, pageResult.getList().get(0));
//    }
//
//}