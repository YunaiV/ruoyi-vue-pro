package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collection;

@Slf4j
class TmsCustomCategoryMapperTest extends BaseDbUnitTest {
    @Resource
    TmsCustomCategoryMapper TmsCustomCategoryMapper;


    //test
    @Test
    public void test() {
        TmsCustomCategorySaveReqVO vo = new TmsCustomCategorySaveReqVO();
        vo.setDeclaredType("LED灯条");
        vo.setMaterial(5);
        Collection<TmsCustomCategoryDO> categoryList = TmsCustomCategoryMapper.getCustomRuleByMaterialAndDeclaredType(vo.getMaterial(), vo.getDeclaredType());
        //        Collection<TmsCustomCategoryDO> categoryList =
//            customRuleCategoryMapper.getCustomRuleByMaterialAndDeclaredType(
//                createReqVO.getMaterial(), createReqVO.getDeclaredType());
        log.info("categoryList:{}", categoryList);
//        if (CollectionUtils.isNotEmpty(categoryList)) {
//            throw exception(ErrorCodeConstants.CUSTOM_RULE_CATEGORY_EXISTS, vo.getDeclaredType());
//        }
    }
}