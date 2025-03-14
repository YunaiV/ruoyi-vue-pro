//package cn.iocoder.yudao.module.erp.dal.mysql.logistic.category.item;
//
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemPageReqVO;
//import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import java.util.List;
//
//@Slf4j
//@Import(ErpCustomCategoryItemMapper.class)
//class ErpCustomCategoryItemMapperTest extends BaseDbUnitTest {
//    @Resource
//    private ErpCustomCategoryItemMapper erpCustomCategoryItemMapper;
//
//
//    @Test
//    void selectPage() {
//        PageResult<ErpCustomCategoryItemDO> page = erpCustomCategoryItemMapper.selectPage(new ErpCustomCategoryItemPageReqVO());
//        //log
//        log.info("page: {}", page);
//    }
//
//    @Test
//    void selectListByCategoryId() {
//        List<ErpCustomCategoryItemDO> list = erpCustomCategoryItemMapper.selectListByCategoryId(1L);
//        //log
//        log.info("list: {}", list);
//    }
//
//    @Test
//    void testSelectListByCategoryId() {
//        List<ErpCustomCategoryItemDO> list = erpCustomCategoryItemMapper.selectListByCategoryId(List.of(1L, 2L));
//        //log
//        log.info("list: {}", list);
//    }
//}