package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardProjectRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardTypeRespVO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HrmInsuranceStandardServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class HrmInsuranceStandardServiceImplTest {

    private final HrmInsuranceStandardServiceImpl insuranceStandardService =
            new HrmInsuranceStandardServiceImpl();

    @Test
    public void testParseTypeList_success() {
        // 准备参数
        String body = "{\"data\":{\"SocialTypes\":["
                + "{\"AccountNo\":\"SS100172\",\"InsuredType\":\"非深户一档\"},"
                + "{\"AccountNo\":\"SS100173\",\"InsuredType\":\"深户一档\"}]}}";

        // 调用
        List<HrmInsuranceStandardTypeRespVO> result = insuranceStandardService.parseTypeList(body);

        // 断言
        assertEquals(2, result.size());
        assertEquals("SS100172", result.get(0).getCode());
        assertEquals("非深户一档", result.get(0).getName());
        assertEquals("SS100173", result.get(1).getCode());
        assertEquals("深户一档", result.get(1).getName());
    }

    @Test
    public void testParseProjectList_success() {
        // 准备参数
        String body = "<table><tr><th>项目</th></tr>"
                + "<tr><td>养老保险</td><td>10,000.00</td><td></td><td>16%</td><td>8%</td>"
                + "<td>1,600.00</td><td>800.00</td></tr>"
                + "<tr><td>失业保险</td><td>2520</td><td></td><td>0.8%</td><td>0.2%</td>"
                + "<td>20.16</td><td>5.04</td></tr>"
                + "<tr><td>工伤保险</td><td>2520</td><td></td><td>0.5%</td><td>0%</td>"
                + "<td>12.60</td><td>0</td></tr>"
                + "<tr><td>未知项目</td><td>0</td><td></td><td>0</td><td>0</td>"
                + "<td>0</td><td>0</td></tr></table>";

        // 调用
        List<HrmInsuranceStandardProjectRespVO> result = insuranceStandardService.parseProjectList(body);

        // 断言
        assertEquals(3, result.size());
        HrmInsuranceStandardProjectRespVO pension = result.get(0);
        assertEquals(HrmInsuranceProjectTypeEnum.PENSION_INSURANCE.getType(), pension.getType());
        assertAmount("10000.00", pension.getBaseAmount());
        assertAmount("16", pension.getCorporateRate());
        assertAmount("800.00", pension.getPersonalAmount());
        assertEquals(HrmInsuranceProjectTypeEnum.UNEMPLOYMENT_INSURANCE.getType(), result.get(1).getType());
        assertEquals(HrmInsuranceProjectTypeEnum.WORK_INJURY_INSURANCE.getType(), result.get(2).getType());
    }

    @Test
    public void testGetCityName_success() {
        assertEquals("深圳", HrmInsuranceStandardServiceImpl.getCityName(440305));
        assertEquals("北京", HrmInsuranceStandardServiceImpl.getCityName(110105));
        assertNull(HrmInsuranceStandardServiceImpl.getCityName(-1));
    }

    @Test
    public void testParseProjectList_duplicateTypeUsesFirst() {
        // 准备参数
        String body = "<table>"
                + "<tr><td>养老保险</td><td>10000</td><td></td><td>16%</td><td>8%</td>"
                + "<td>1600</td><td>800</td></tr>"
                + "<tr><td>养老保险单位部分</td><td>8000</td><td></td><td>12%</td><td>6%</td>"
                + "<td>960</td><td>480</td></tr></table>";

        // 调用
        List<HrmInsuranceStandardProjectRespVO> result = insuranceStandardService.parseProjectList(body);

        // 断言
        assertEquals(1, result.size());
        assertAmount("10000", result.get(0).getBaseAmount());
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

}
