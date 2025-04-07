package cn.iocoder.yudao.module.tms.dal.mysql.logistic.category;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collection;

@Slf4j
class ErpCustomCategoryMapperTest extends BaseDbUnitTest {
    @Resource
    ErpCustomCategoryMapper erpCustomCategoryMapper;


    //test
    @Test
    public void test() {
        ErpCustomCategorySaveReqVO vo = new ErpCustomCategorySaveReqVO();
        vo.setDeclaredType("LED灯条");
        vo.setMaterial(5);
        Collection<ErpCustomCategoryDO> categoryList = erpCustomCategoryMapper.getCustomRuleByMaterialAndDeclaredType(vo.getMaterial(), vo.getDeclaredType());
//        Collection<ErpCustomCategoryDO> categoryList =
//            customRuleCategoryMapper.getCustomRuleByMaterialAndDeclaredType(
//                createReqVO.getMaterial(), createReqVO.getDeclaredType());
        log.info("categoryList:{}", categoryList);
//        if (CollectionUtils.isNotEmpty(categoryList)) {
//            throw exception(ErrorCodeConstants.CUSTOM_RULE_CATEGORY_EXISTS, vo.getDeclaredType());
//        }
    }
}