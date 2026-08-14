package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryOptionMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryOptionTemplateMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_OPTION_STANDARD_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_OPTION_CODE_OCCUPIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HrmSalaryOptionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryOptionServiceImpl.class)
public class HrmSalaryOptionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryOptionServiceImpl salaryOptionService;

    @Resource
    private HrmSalaryOptionMapper salaryOptionMapper;
    @Resource
    private HrmSalaryOptionTemplateMapper salaryOptionTemplateMapper;

    @Test
    public void testCreateSalaryOption_success() {
        // mock 数据
        salaryOptionMapper.insert(createSalaryOption(30, 0, "浮动工资", false).setTemplateId(1L));
        // 准备参数
        HrmSalaryOptionSaveReqVO createReqVO = createSalaryOptionReqVO("绩效奖金");

        // 调用
        Long id = salaryOptionService.createSalaryOption(createReqVO);

        // 断言
        HrmSalaryOptionDO option = salaryOptionMapper.selectById(id);
        assertEquals(Integer.valueOf(HrmSalaryOptionDO.CUSTOM_OPTION_CODE_BASE + 30 * 10_000 + 1),
                option.getCode());
        assertEquals(Integer.valueOf(30), option.getParentCode());
        assertEquals("绩效奖金", option.getName());
        assertFalse(option.getSystemFlag());
        assertNull(option.getTemplateId());
        assertTrue(option.getTaxEnabled());
        assertTrue(option.getCalculateEnabled());
        assertTrue(option.getVisible());
        assertTrue(option.getEnabled());
    }

    @Test
    public void testCreateSalaryOption_deletedCode() {
        // mock 数据
        salaryOptionMapper.insert(createSalaryOption(30, 0, "浮动工资", false).setTemplateId(1L));
        HrmSalaryOptionDO deletedOption = createSalaryOption(30101, 30, "已删除奖金", false);
        salaryOptionMapper.insert(deletedOption);
        salaryOptionMapper.deleteById(deletedOption.getId());
        // 准备参数
        HrmSalaryOptionSaveReqVO createReqVO = createSalaryOptionReqVO("绩效奖金");

        // 调用
        Long id = salaryOptionService.createSalaryOption(createReqVO);

        // 断言
        assertEquals(Integer.valueOf(HrmSalaryOptionDO.CUSTOM_OPTION_CODE_BASE + 30 * 10_000 + 1),
                salaryOptionMapper.selectById(id).getCode());
    }

    @Test
    public void testUpdateSalaryOptionEnabled_success() {
        // mock 数据
        HrmSalaryOptionDO option = createSalaryOption(10101, 10, "基本工资", false);
        salaryOptionMapper.insert(option);

        // 调用
        salaryOptionService.updateSalaryOptionEnabled(option.getId(), false);

        // 断言
        assertFalse(salaryOptionMapper.selectById(option.getId()).getEnabled());
    }

    @Test
    public void testUpdateSalaryOptionVisible_success() {
        // mock 数据
        HrmSalaryOptionDO option = createSalaryOption(210101, 210, "应发工资", true);
        salaryOptionMapper.insert(option);

        // 调用
        salaryOptionService.updateSalaryOptionVisible(option.getId(), false);

        // 断言
        assertFalse(salaryOptionMapper.selectById(option.getId()).getVisible());
    }

    @Test
    public void testDeleteSalaryOption_success() {
        // mock 数据
        HrmSalaryOptionDO option = createSalaryOption(30101, 30, "绩效奖金", false);
        salaryOptionMapper.insert(option);

        // 调用
        salaryOptionService.deleteSalaryOption(option.getId());

        // 断言
        assertNull(salaryOptionMapper.selectById(option.getId()));
    }

    @Test
    public void testDeleteSalaryOption_standard() {
        // mock 数据
        HrmSalaryOptionDO option = createSalaryOption(30101, 30, "绩效奖金", false).setTemplateId(1L);
        salaryOptionMapper.insert(option);

        // 调用，并断言异常
        assertServiceException(() -> salaryOptionService.deleteSalaryOption(option.getId()),
                SALARY_OPTION_STANDARD_CANNOT_MODIFY);
    }

    @Test
    public void testGetSalaryOptionList_visible() {
        // mock 数据
        HrmSalaryOptionDO category = createSalaryOption(30, 0, "浮动工资", false);
        salaryOptionMapper.insert(category);
        salaryOptionMapper.insert(createSalaryOption(30101, 30, "绩效奖金", false));
        salaryOptionMapper.insert(createSalaryOption(30102, 30, "隐藏奖金", false).setVisible(false));

        // 调用
        List<HrmSalaryOptionDO> list = salaryOptionService.getSalaryOptionList(false, true);

        // 断言
        assertEquals(2, list.size());
        assertEquals(category.getId(), list.get(0).getId());
        assertEquals(Integer.valueOf(30101), list.get(1).getCode());
    }

    @Test
    public void testGetSalaryOptionList_adjustable() {
        // mock 数据
        salaryOptionMapper.insert(createSalaryOption(10, 0, "基本工资", false));
        salaryOptionMapper.insert(createSalaryOption(10101, 10, "基本工资", false));
        salaryOptionMapper.insert(createSalaryOption(100, 0, "社保", false));
        salaryOptionMapper.insert(createSalaryOption(100101, 100, "个人社保", false));
        salaryOptionMapper.insert(createSalaryOption(20, 0, "津贴", false).setEnabled(false));
        salaryOptionMapper.insert(createSalaryOption(20101, 20, "交通补贴", false));

        // 调用
        List<HrmSalaryOptionDO> options = salaryOptionService.getSalaryOptionList(true);

        // 断言
        assertEquals(1, options.size());
        assertEquals(Integer.valueOf(10101), options.get(0).getCode());
    }

    @Test
    public void testSyncSalaryOption() {
        // mock 数据
        HrmSalaryOptionTemplateDO categoryTemplate = createSalaryOptionTemplate(10, 0, "基本工资");
        salaryOptionTemplateMapper.insert(categoryTemplate);
        HrmSalaryOptionTemplateDO optionTemplate = createSalaryOptionTemplate(10101, 10, "基本工资");
        salaryOptionTemplateMapper.insert(optionTemplate);
        HrmSalaryOptionDO disabledOption = createSalaryOption(10101, 10, "旧基本工资", false)
                .setTemplateId(optionTemplate.getId()).setEnabled(false).setVisible(false);
        salaryOptionMapper.insert(disabledOption);

        // 调用
        salaryOptionService.syncSalaryOption();
        List<HrmSalaryOptionDO> list = salaryOptionService.getSalaryOptionList();

        // 断言
        assertEquals(2L, salaryOptionMapper.selectCount());
        HrmSalaryOptionDO syncedOption = salaryOptionMapper.selectById(disabledOption.getId());
        assertEquals("基本工资", syncedOption.getName());
        assertFalse(syncedOption.getEnabled());
        assertFalse(syncedOption.getVisible());
        assertEquals(optionTemplate.getId(), syncedOption.getTemplateId());
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(10), list.get(0).getCode());
        assertEquals(Integer.valueOf(10101), list.get(1).getCode());
        assertEquals(categoryTemplate.getId(), list.get(0).getTemplateId());
        assertEquals(optionTemplate.getId(), list.get(1).getTemplateId());
    }

    @Test
    public void testSyncSalaryOption_customCodeOccupied() {
        // mock 数据
        salaryOptionTemplateMapper.insert(createSalaryOptionTemplate(10101, 10, "基本工资"));
        salaryOptionMapper.insert(createSalaryOption(10101, 10, "租户自定义项", false));

        // 调用，并断言异常
        assertServiceException(() -> salaryOptionService.syncSalaryOption(),
                SALARY_OPTION_CODE_OCCUPIED, 10101);
    }

    // ========== 随机对象 ==========

    private HrmSalaryOptionSaveReqVO createSalaryOptionReqVO(String name) {
        return new HrmSalaryOptionSaveReqVO().setParentCode(30).setName(name);
    }

    private HrmSalaryOptionDO createSalaryOption(Integer code, Integer parentCode, String name,
                                                  Boolean systemFlag) {
        return HrmSalaryOptionDO.builder().code(code).parentCode(parentCode).name(name)
                .systemFlag(systemFlag).type(1).taxEnabled(true).visible(true)
                .calculateEnabled(true).enabled(true).build();
    }

    private HrmSalaryOptionTemplateDO createSalaryOptionTemplate(Integer code, Integer parentCode, String name) {
        return HrmSalaryOptionTemplateDO.builder().code(code).parentCode(parentCode).name(name)
                .systemFlag(false).type(1).taxEnabled(true).visible(true).calculateEnabled(true).build();
    }

}
