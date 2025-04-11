package cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.TmsCustomCategoryMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.TmsCustomCategoryBO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
class TmsCustomRuleMapperTest extends BaseDbUnitTest {
    @Resource
    TmsCustomRuleMapper TmsCustomRuleMapper;
    @Resource
    TmsCustomCategoryMapper TmsCustomCategoryMapper;

    @Test
    void getCustomRuleBOById() {
        TmsCustomRuleBO ruleBO = TmsCustomRuleMapper.getCustomRuleBOById(1L);
        log.info("ruleBO:{}", ruleBO);
    }

    //test1
    @Test
    void selectBOListEqCountryCodeByItemId() {
        PageResult<TmsCustomCategoryBO> bo = TmsCustomCategoryMapper.selectPageBO(new TmsCustomCategoryPageReqVO());
        for (TmsCustomCategoryBO TmsCustomCategoryBO : bo.getList()) {
            log.info("TmsCustomCategoryBO:{}", TmsCustomCategoryBO);
        }
    }
}