package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate.HrmSalaryChangeOptionVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate.HrmSalaryChangeTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryChangeTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryChangeTemplateMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_TEMPLATE_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_TEMPLATE_OPTION_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryChangeTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryChangeTemplateServiceImpl.class)
public class HrmSalaryChangeTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryChangeTemplateServiceImpl salaryChangeTemplateService;
    @Resource
    private HrmSalaryChangeTemplateMapper salaryChangeTemplateMapper;
    @MockBean
    private HrmSalaryOptionService salaryOptionService;

    @Test
    public void testCreateSalaryChangeTemplate_default_success() {
        // mock 数据
        List<HrmSalaryOptionDO> salaryOptions = Collections.singletonList(
                createSalaryOption(10101, 10, "基本工资", true));
        when(salaryOptionService.getSalaryOptionList(true)).thenReturn(salaryOptions);

        // 调用
        List<HrmSalaryChangeOptionVO> optionReqVOs = BeanUtils.toBean(
                salaryOptions, HrmSalaryChangeOptionVO.class);
        optionReqVOs.get(0).setName("伪造的薪资项名称");
        Long defaultTemplateId = salaryChangeTemplateService.createSalaryChangeTemplate(
                createSalaryChangeTemplateReqVO("默认调薪模板", true, optionReqVOs));
        Long newDefaultTemplateId = salaryChangeTemplateService.createSalaryChangeTemplate(
                createSalaryChangeTemplateReqVO("研发调薪模板", true, optionReqVOs));

        // 断言
        assertEquals(false, salaryChangeTemplateMapper.selectById(defaultTemplateId).getDefaultStatus());
        assertEquals(true, salaryChangeTemplateMapper.selectById(newDefaultTemplateId).getDefaultStatus());
        assertEquals(2, salaryChangeTemplateService.getSalaryChangeTemplateList().size());
        assertEquals("基本工资", salaryChangeTemplateMapper.selectById(newDefaultTemplateId)
                .getOptions().get(0).getName());
        assertEquals(10101, salaryChangeTemplateService.getSalaryChangeTemplateList().get(0)
                .getOptions().get(0).getCode());
    }

    @Test
    public void testCreateSalaryChangeTemplate_optionInvalid() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(true)).thenReturn(Collections.singletonList(
                createSalaryOption(10101, 10, "基本工资", true)));
        // 准备参数
        HrmSalaryChangeOptionVO option = new HrmSalaryChangeOptionVO();
        option.setCode(99999);

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeTemplateService.createSalaryChangeTemplate(
                createSalaryChangeTemplateReqVO("研发调薪模板", false, Collections.singletonList(option))),
                SALARY_CHANGE_TEMPLATE_OPTION_INVALID);
    }

    @Test
    public void testCreateSalaryChangeTemplate_optionDuplicate() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(true)).thenReturn(Collections.singletonList(
                createSalaryOption(10101, 10, "基本工资", true)));
        // 准备参数
        HrmSalaryChangeOptionVO option = new HrmSalaryChangeOptionVO();
        option.setCode(10101);

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeTemplateService.createSalaryChangeTemplate(
                createSalaryChangeTemplateReqVO("研发调薪模板", false, Arrays.asList(option, option))),
                SALARY_CHANGE_TEMPLATE_OPTION_INVALID);
    }

    @Test
    public void testGetSalaryChangeTemplateList_empty() {
        // 调用
        List<HrmSalaryChangeTemplateDO> templates = salaryChangeTemplateService.getSalaryChangeTemplateList();

        // 断言
        assertEquals(0, templates.size());
        assertEquals(0L, salaryChangeTemplateMapper.selectCount());
    }

    @Test
    public void testDeleteSalaryChangeTemplate_success() {
        // mock 数据
        HrmSalaryChangeTemplateDO template = HrmSalaryChangeTemplateDO.builder()
                .name("普通调薪模板").defaultStatus(false).options(Collections.emptyList()).build();
        salaryChangeTemplateMapper.insert(template);

        // 调用
        salaryChangeTemplateService.deleteSalaryChangeTemplate(template.getId());

        // 断言
        assertNull(salaryChangeTemplateMapper.selectById(template.getId()));
    }

    @Test
    public void testDeleteSalaryChangeTemplate_default() {
        // mock 数据
        HrmSalaryChangeTemplateDO template = HrmSalaryChangeTemplateDO.builder()
                .name("默认调薪模板").defaultStatus(true).options(Collections.emptyList()).build();
        salaryChangeTemplateMapper.insert(template);

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeTemplateService.deleteSalaryChangeTemplate(template.getId()),
                SALARY_CHANGE_TEMPLATE_DEFAULT_CANNOT_DELETE);
    }

    // ========== 随机对象 ==========

    private HrmSalaryOptionDO createSalaryOption(Integer code, Integer parentCode, String name, Boolean enabled) {
        return HrmSalaryOptionDO.builder().code(code).parentCode(parentCode).name(name)
                .systemFlag(true).templateId(1L).type(1).taxEnabled(true).visible(true)
                .calculateEnabled(true).enabled(enabled).build();
    }

    private HrmSalaryChangeTemplateSaveReqVO createSalaryChangeTemplateReqVO(
            String name, Boolean defaultStatus, List<HrmSalaryChangeOptionVO> options) {
        HrmSalaryChangeTemplateSaveReqVO reqVO = new HrmSalaryChangeTemplateSaveReqVO();
        reqVO.setName(name);
        reqVO.setDefaultStatus(defaultStatus);
        reqVO.setOptions(options == null ? Collections.emptyList() : options);
        return reqVO;
    }

}
