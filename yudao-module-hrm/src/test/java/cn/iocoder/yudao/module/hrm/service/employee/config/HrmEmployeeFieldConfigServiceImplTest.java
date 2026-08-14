package cn.iocoder.yudao.module.hrm.service.employee.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeArchiveFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeCreateFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeCreateFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.service.config.HrmConfigService;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_FIELD_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_FIELD_NOT_VISIBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmEmployeeFieldConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeFieldConfigServiceImpl.class)
public class HrmEmployeeFieldConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeFieldConfigServiceImpl employeeFieldConfigService;

    @MockitoBean
    private HrmConfigService configService;

    @Test
    public void testGetCreateFieldConfigList_default() {
        // mock 方法
        when(configService.getConfigValueList(HrmConfigTypeEnum.EMPLOYEE_CREATE_ACTIVE_FIELD.getType()))
                .thenReturn(Collections.emptyList());

        // 调用
        List<HrmEmployeeFieldConfigRespVO> fields = employeeFieldConfigService.getEmployeeCreateFieldConfigList(
                HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());

        // 断言
        assertEquals(HrmEmployeeCreateFieldEnum.values().length, fields.size());
        HrmEmployeeFieldConfigRespVO nameField = getField(fields, "name");
        assertTrue(nameField.getVisible());
        assertTrue(nameField.getVisibleLocked());
        assertTrue(getField(fields, "jobNumber").getVisible());
        assertTrue(getField(fields, "jobNumber").getVisibleLocked());
        assertFalse(getField(fields, "country").getVisible());
        assertFalse(getField(fields, "age").getVisible());
    }

    @Test
    public void testGetCreateFieldConfigList_pendingEntryDefault() {
        // mock 方法
        when(configService.getConfigValueList(HrmConfigTypeEnum.EMPLOYEE_CREATE_PENDING_FIELD.getType()))
                .thenReturn(Collections.emptyList());

        // 调用
        List<HrmEmployeeFieldConfigRespVO> fields = employeeFieldConfigService.getEmployeeCreateFieldConfigList(
                HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());

        // 断言
        assertFalse(getField(fields, "jobNumber").getVisible());
        assertFalse(getField(fields, "jobNumber").getVisibleLocked());
        assertTrue(getField(fields, "entryTime").getVisible());
        assertTrue(getField(fields, "entryTime").getVisibleLocked());
    }

    @Test
    public void testValidateEmployeeCreateFields_hiddenField() {
        // mock 方法
        when(configService.getConfigValueList(HrmConfigTypeEnum.EMPLOYEE_CREATE_ACTIVE_FIELD.getType()))
                .thenReturn(Collections.emptyList());
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = new HrmEmployeeSaveReqVO().setCountry("中国");

        // 调用，并断言异常
        assertServiceException(() -> employeeFieldConfigService.validateEmployeeCreateFields(
                reqVO, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()),
                EMPLOYEE_FIELD_NOT_VISIBLE, "国家或地区");
    }

    @Test
    public void testSaveCreateFieldConfig_success() {
        // 准备参数
        HrmEmployeeCreateFieldConfigSaveReqVO reqVO = new HrmEmployeeCreateFieldConfigSaveReqVO();
        reqVO.setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        reqVO.setFields(Arrays.asList(createField("name", false, null),
                createField("country", false, null), createField("age", true, null)));

        // 调用
        employeeFieldConfigService.saveEmployeeCreateFieldConfig(reqVO);

        // mock 方法
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> configValuesCaptor = ArgumentCaptor.forClass(List.class);
        verify(configService).replaceConfigValueList(eq(HrmConfigTypeEnum.EMPLOYEE_CREATE_ACTIVE_FIELD.getType()),
                configValuesCaptor.capture());
        when(configService.getConfigValueList(HrmConfigTypeEnum.EMPLOYEE_CREATE_ACTIVE_FIELD.getType()))
                .thenReturn(configValuesCaptor.getValue());

        // 断言
        List<HrmEmployeeFieldConfigRespVO> fields = employeeFieldConfigService.getEmployeeCreateFieldConfigList(
                HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        assertTrue(getField(fields, "name").getVisible());
        assertFalse(getField(fields, "country").getVisible());
        assertTrue(getField(fields, "age").getVisible());
        assertEquals(HrmEmployeeCreateFieldEnum.values().length, configValuesCaptor.getValue().size());
    }

    @Test
    public void testSaveCreateFieldConfig_fieldInvalid() {
        // 准备参数
        HrmEmployeeCreateFieldConfigSaveReqVO reqVO = new HrmEmployeeCreateFieldConfigSaveReqVO();
        reqVO.setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        reqVO.setFields(Arrays.asList(createField("unknown", true, null)));

        // 调用，并断言异常
        assertServiceException(() -> employeeFieldConfigService.saveEmployeeCreateFieldConfig(reqVO),
                EMPLOYEE_FIELD_CONFIG_INVALID, "unknown");
    }

    @Test
    public void testSaveArchiveFieldConfig_success() {
        // 准备参数
        HrmEmployeeArchiveFieldConfigSaveReqVO reqVO = new HrmEmployeeArchiveFieldConfigSaveReqVO();
        reqVO.setFields(Arrays.asList(createField("mobile", false, true),
                createField("email", true, true), createField("age", true, true)));

        // 调用
        employeeFieldConfigService.saveEmployeeArchiveFieldConfig(reqVO);

        // mock 方法
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> configValuesCaptor = ArgumentCaptor.forClass(List.class);
        verify(configService).replaceConfigValueList(eq(HrmConfigTypeEnum.EMPLOYEE_ARCHIVE_FIELD.getType()),
                configValuesCaptor.capture());
        when(configService.getConfigValueList(HrmConfigTypeEnum.EMPLOYEE_ARCHIVE_FIELD.getType()))
                .thenReturn(configValuesCaptor.getValue());

        // 断言
        List<HrmEmployeeFieldConfigRespVO> fields = employeeFieldConfigService.getEmployeeArchiveFieldConfigList();
        assertFalse(getField(fields, "mobile").getVisible());
        assertFalse(getField(fields, "mobile").getEditable());
        assertTrue(getField(fields, "email").getEditable());
        assertFalse(getField(fields, "age").getEditable());
        Set<String> editableFields = employeeFieldConfigService.getEditableArchiveFieldNames();
        assertTrue(editableFields.contains("email"));
        assertFalse(editableFields.contains("mobile"));
        assertEquals(HrmEmployeeArchiveFieldEnum.values().length, configValuesCaptor.getValue().size());
    }

    private HrmEmployeeFieldConfigRespVO getField(List<HrmEmployeeFieldConfigRespVO> fields, String name) {
        return fields.stream().filter(field -> field.getName().equals(name)).findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private HrmEmployeeFieldConfigSaveReqVO createField(String name, Boolean visible, Boolean editable) {
        HrmEmployeeFieldConfigSaveReqVO field = new HrmEmployeeFieldConfigSaveReqVO();
        field.setName(name);
        field.setVisible(visible);
        field.setEditable(editable);
        return field;
    }

}
