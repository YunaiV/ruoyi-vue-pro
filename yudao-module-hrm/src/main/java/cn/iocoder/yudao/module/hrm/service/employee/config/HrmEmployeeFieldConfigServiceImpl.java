package cn.iocoder.yudao.module.hrm.service.employee.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeArchiveFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeCreateFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeCreateFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.service.config.HrmConfigService;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_FIELD_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_FIELD_NOT_VISIBLE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CONFIG_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUCCESS;

/**
 * HRM 员工字段配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeFieldConfigServiceImpl implements HrmEmployeeFieldConfigService {

    @Resource
    private HrmConfigService configService;

    @Override
    public List<HrmEmployeeFieldConfigRespVO> getEmployeeCreateFieldConfigList(Integer entryStatus) {
        Integer configType = getCreateFieldConfigType(entryStatus);
        Map<String, HrmEmployeeFieldConfigValueVO> configMap = getFieldConfigMap(configType);
        return convertList(HrmEmployeeCreateFieldEnum.values(), field -> {
            HrmEmployeeFieldConfigValueVO config = configMap.get(field.getName());
            boolean visibleLocked = Boolean.TRUE.equals(field.getVisibleLocked(entryStatus));
            boolean visible = (visibleLocked || config == null)
                    ? field.getDefaultVisible(entryStatus) : Boolean.TRUE.equals(config.getVisible());
            return buildFieldConfigRespVO(field.getName(), field.getTitle(), field.getGroupName(), visible)
                    .setVisibleLocked(visibleLocked);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_CONFIG_TYPE, subType = HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUB_TYPE,
            bizNo = "{{#configType}}", success = HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUCCESS)
    public void saveEmployeeCreateFieldConfig(HrmEmployeeCreateFieldConfigSaveReqVO reqVO) {
        // 1. 校验字段配置
        Integer configType = getCreateFieldConfigType(reqVO.getEntryStatus());
        Map<String, HrmEmployeeFieldConfigSaveReqVO> fieldConfigMap = validateFieldConfigList(reqVO.getFields());
        for (String name : fieldConfigMap.keySet()) {
            if (HrmEmployeeCreateFieldEnum.valueOfName(name) == null) {
                throw exception(EMPLOYEE_FIELD_CONFIG_INVALID, name);
            }
        }
        HrmEmployeeCreateFieldConfigSaveReqVO oldConfig = new HrmEmployeeCreateFieldConfigSaveReqVO()
                .setEntryStatus(reqVO.getEntryStatus())
                .setFields(BeanUtils.toBean(getEmployeeCreateFieldConfigList(reqVO.getEntryStatus()),
                        HrmEmployeeFieldConfigSaveReqVO.class));

        // 2. 保存字段配置
        List<String> configValues = convertList(HrmEmployeeCreateFieldEnum.values(), field -> {
            HrmEmployeeFieldConfigSaveReqVO fieldConfig = fieldConfigMap.get(field.getName());
            boolean visible = (Boolean.TRUE.equals(field.getVisibleLocked(reqVO.getEntryStatus()))
                    || fieldConfig == null) ? field.getDefaultVisible(reqVO.getEntryStatus())
                    : Boolean.TRUE.equals(fieldConfig.getVisible());
            return JsonUtils.toJsonString(new HrmEmployeeFieldConfigValueVO(field.getName(), visible, null));
        });
        configService.replaceConfigValueList(configType, configValues);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("configType", configType);
        LogRecordContext.putVariable("configGroupName",
                HrmEmployeeEntryStatusEnum.valueOf(reqVO.getEntryStatus()).getName() + "员工新建字段配置");
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldConfig);
    }

    @Override
    public void validateEmployeeCreateFields(HrmEmployeeSaveReqVO reqVO, Integer entryStatus) {
        for (HrmEmployeeFieldConfigRespVO field : getEmployeeCreateFieldConfigList(entryStatus)) {
            if (Boolean.TRUE.equals(field.getVisible())
                    || HrmEmployeeCreateFieldEnum.CANDIDATE_ID.getName().equals(field.getName())) {
                continue;
            }
            if (ObjectUtil.isNotEmpty(BeanUtil.getFieldValue(reqVO, field.getName()))) {
                throw exception(EMPLOYEE_FIELD_NOT_VISIBLE, field.getTitle());
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public List<HrmEmployeeFieldConfigRespVO> getEmployeeArchiveFieldConfigList() {
        Map<String, HrmEmployeeFieldConfigValueVO> configMap = getFieldConfigMap(
                HrmConfigTypeEnum.EMPLOYEE_ARCHIVE_FIELD.getType());
        return convertList(HrmEmployeeArchiveFieldEnum.values(), field -> {
            HrmEmployeeFieldConfigValueVO config = configMap.get(field.getName());
            boolean visible = config == null ? field.getDefaultVisible() : Boolean.TRUE.equals(config.getVisible());
            boolean editable = config == null ? field.getDefaultEditable() : Boolean.TRUE.equals(config.getEditable());
            if (BooleanUtil.isFalse(visible) || Boolean.TRUE.equals(field.getEditableLocked())) {
                editable = false;
            }
            return buildFieldConfigRespVO(field.getName(), field.getTitle(), field.getGroupName(), visible)
                    .setEditable(editable).setEditableLocked(field.getEditableLocked());
        });
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_CONFIG_TYPE, subType = HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUB_TYPE,
            bizNo = "{{#configType}}", success = HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUCCESS)
    public void saveEmployeeArchiveFieldConfig(HrmEmployeeArchiveFieldConfigSaveReqVO reqVO) {
        // 1. 校验字段配置
        Map<String, HrmEmployeeFieldConfigSaveReqVO> fieldConfigMap = validateFieldConfigList(reqVO.getFields());
        for (String name : fieldConfigMap.keySet()) {
            if (HrmEmployeeArchiveFieldEnum.valueOfName(name) == null) {
                throw exception(EMPLOYEE_FIELD_CONFIG_INVALID, name);
            }
        }
        HrmEmployeeArchiveFieldConfigSaveReqVO oldConfig = new HrmEmployeeArchiveFieldConfigSaveReqVO()
                .setFields(BeanUtils.toBean(getEmployeeArchiveFieldConfigList(),
                        HrmEmployeeFieldConfigSaveReqVO.class));

        // 2. 保存字段配置
        List<String> configValues = convertList(HrmEmployeeArchiveFieldEnum.values(), field -> {
            HrmEmployeeFieldConfigSaveReqVO fieldConfig = fieldConfigMap.get(field.getName());
            boolean visible = fieldConfig == null ? field.getDefaultVisible()
                    : Boolean.TRUE.equals(fieldConfig.getVisible());
            boolean editable = fieldConfig == null ? field.getDefaultEditable()
                    : Boolean.TRUE.equals(fieldConfig.getEditable());
            if (BooleanUtil.isFalse(visible) || Boolean.TRUE.equals(field.getEditableLocked())) {
                editable = false;
            }
            return JsonUtils.toJsonString(
                    new HrmEmployeeFieldConfigValueVO(field.getName(), visible, editable));
        });
        configService.replaceConfigValueList(HrmConfigTypeEnum.EMPLOYEE_ARCHIVE_FIELD.getType(), configValues);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("configType", HrmConfigTypeEnum.EMPLOYEE_ARCHIVE_FIELD.getType());
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldConfig);
    }

    @Override
    public Set<String> getVisibleArchiveFieldNames() {
        return convertSet(getEmployeeArchiveFieldConfigList(), HrmEmployeeFieldConfigRespVO::getName,
                field -> Boolean.TRUE.equals(field.getVisible()));
    }

    @Override
    public Set<String> getEditableArchiveFieldNames() {
        return convertSet(getEmployeeArchiveFieldConfigList(), HrmEmployeeFieldConfigRespVO::getName,
                field -> Boolean.TRUE.equals(field.getEditable()));
    }

    private Integer getCreateFieldConfigType(Integer entryStatus) {
        if (HrmEmployeeEntryStatusEnum.ACTIVE.getStatus().equals(entryStatus)) {
            return HrmConfigTypeEnum.EMPLOYEE_CREATE_ACTIVE_FIELD.getType();
        }
        if (HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus().equals(entryStatus)) {
            return HrmConfigTypeEnum.EMPLOYEE_CREATE_PENDING_FIELD.getType();
        }
        throw exception(EMPLOYEE_FIELD_CONFIG_INVALID, "entryStatus");
    }

    private Map<String, HrmEmployeeFieldConfigSaveReqVO> validateFieldConfigList(
            List<HrmEmployeeFieldConfigSaveReqVO> fieldConfigs) {
        Map<String, HrmEmployeeFieldConfigSaveReqVO> fieldConfigMap = new LinkedHashMap<>();
        for (HrmEmployeeFieldConfigSaveReqVO fieldConfig : fieldConfigs) {
            if (fieldConfigMap.put(fieldConfig.getName(), fieldConfig) != null) {
                throw exception(EMPLOYEE_FIELD_CONFIG_INVALID, fieldConfig.getName());
            }
        }
        return fieldConfigMap;
    }

    private Map<String, HrmEmployeeFieldConfigValueVO> getFieldConfigMap(Integer configType) {
        Map<String, HrmEmployeeFieldConfigValueVO> configMap = new HashMap<>();
        for (String configValue : configService.getConfigValueList(configType)) {
            HrmEmployeeFieldConfigValueVO value = JsonUtils.parseObject(configValue,
                    HrmEmployeeFieldConfigValueVO.class);
            if (value != null && value.getName() != null) {
                configMap.put(value.getName(), value);
            }
        }
        return configMap;
    }

    private HrmEmployeeFieldConfigRespVO buildFieldConfigRespVO(String name, String title, String groupName,
                                                                 Boolean visible) {
        return new HrmEmployeeFieldConfigRespVO().setName(name).setTitle(title).setGroupName(groupName)
                .setVisible(visible).setVisibleLocked(false).setEditableLocked(false);
    }

}
