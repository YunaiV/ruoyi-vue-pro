package cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.ErpCustomCategoryMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.ErpCustomCategoryBO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.ErpCustomRuleBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
class ErpCustomRuleMapperTest extends BaseDbUnitTest {
    @Resource
    ErpCustomRuleMapper erpCustomRuleMapper;
    @Resource
    ErpCustomCategoryMapper erpCustomCategoryMapper;

    @Test
    void getCustomRuleBOById() {
        ErpCustomRuleBO ruleBO = erpCustomRuleMapper.getCustomRuleBOById(1L);
        log.info("ruleBO:{}", ruleBO);
    }

    //test1
    @Test
    void selectBOListEqCountryCodeByItemId() {
        PageResult<ErpCustomCategoryBO> bo = erpCustomCategoryMapper.selectPageBO(new ErpCustomCategoryPageReqVO());
        for (ErpCustomCategoryBO erpCustomCategoryBO : bo.getList()) {
            log.info("erpCustomCategoryBO:{}", erpCustomCategoryBO);
        }
    }
}