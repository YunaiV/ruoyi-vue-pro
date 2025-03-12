//package cn.iocoder.yudao.module.erp.dal.mysql.logistic.category;
//
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
//import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.ErpCustomCategoryDO;
//import cn.iocoder.yudao.module.erp.service.logistic.category.bo.ErpCustomCategoryBO;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import java.util.List;
//
//@Slf4j
//@Import(ErpCustomCategoryMapper.class)
//class ErpCustomCategoryMapperTest extends BaseDbUnitTest {
//    @Resource
//    ErpCustomCategoryMapper erpCustomCategoryMapper;
//
//    @Test
//    void selectPage() {
//        PageResult<ErpCustomCategoryDO> erpCustomCategoryDOPageResult = erpCustomCategoryMapper.selectPage(new ErpCustomCategoryPageReqVO());
//        //log
//        log.info(erpCustomCategoryDOPageResult.toString());
//    }
//
//    @Test
//    void getCustomRuleCategoryList() {
//        //
//        List<ErpCustomCategoryDO> customRuleCategoryList = erpCustomCategoryMapper.getCustomRuleCategoryList(new ErpCustomCategoryPageReqVO());
//        //log
//        log.info(customRuleCategoryList.toString());
//    }
//
//    @Test
//    void getErpCustomCategoryBOList() {
//        List<ErpCustomCategoryBO> bos = erpCustomCategoryMapper.getErpCustomCategoryBOListByCategoryId(300L);
//        //log
//        for (ErpCustomCategoryBO bo : bos) {
//            if (bo.getProductDO() != null)
//                log.info(bo.getProductDO().toString());
//        }
//        log.info(bos.toString());
//    }
//}