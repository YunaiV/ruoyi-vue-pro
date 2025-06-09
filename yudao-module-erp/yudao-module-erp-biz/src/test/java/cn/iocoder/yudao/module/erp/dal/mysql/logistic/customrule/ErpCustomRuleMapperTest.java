//package cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule;
//
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import java.util.List;
//
//@Slf4j
//@Import(ErpCustomRuleMapper.class)
//class ErpCustomRuleMapperTest extends BaseDbUnitTest {
//    @Resource
//    ErpCustomRuleMapper erpCustomRuleMapper;
//
//    @BeforeEach
//    void setUp() {
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void selectPage() {
//        log.info("selectPage:{}", erpCustomRuleMapper.selectPage(new ErpCustomRulePageReqVO().setCode("W06S305QN")));
//    }
//
//    @Test
//    void getCustomRuleByCountryCodeAndProductId() {
//    }
//
//    @Test
//    void selectByProductId() {
//    }
//
//    @Test
//    void selectBOPage() {
//        log.info("selectBOPage:{}", erpCustomRuleMapper.selectBOPage(new ErpCustomRulePageReqVO()));
//    }
//
//    @Test
//    void getCustomRuleBOById() {
//        log.info("{}", erpCustomRuleMapper.getCustomRuleBOById(5L));
//    }
//
//    @Test
//    void selectCustomRuleBOByIds() {
//        log.info("{}", erpCustomRuleMapper.selectCustomRuleBOByIds(null));
//    }
//
//    @Test
//    void testSelectByProductId() {
//        log.info("{}", erpCustomRuleMapper.selectByProductId(List.of(1L, 2L)));
//    }
//
//    @Test
//    void testSelectByProductId1() {
//        var bos = erpCustomRuleMapper.selectByProductId(List.of(487L, 486L));
//        log.info("{}", erpCustomRuleMapper.selectByProductId(List.of(487L, 486L)));
//        if (bos.isEmpty()) {
//            log.info("{}", bos);
//        }
//    }
//    @Test
//    void selectBOList() {
/// /        List<ErpCustomRuleBO> erpCustomRuleBOS = erpCustomRuleMapper.selectBOList(new ErpCustomRulePageReqVO().setCustomCategoryId(318L));
/// /        318不存在海关规则，所以没有数据
/// /        log.info("{}", erpCustomRuleBOS);
//    }
//}