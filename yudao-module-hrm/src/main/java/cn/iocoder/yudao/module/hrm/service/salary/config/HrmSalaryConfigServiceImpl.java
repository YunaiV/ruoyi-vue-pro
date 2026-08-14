package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryConfigMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_CONFIG_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_CONFIG_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_CONFIG_UPDATE_SUCCESS;

/**
 * HRM 计薪配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryConfigServiceImpl implements HrmSalaryConfigService {

    @Resource
    private HrmSalaryConfigMapper salaryConfigMapper;

    @Resource
    @Lazy // 延迟加载
    private HrmSalaryMonthRecordService salaryMonthRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSalaryConfig(HrmSalaryConfigCreateReqVO createReqVO) {
        // 1. 校验计薪配置不存在
        validateSalaryConfigNotExists();

        // 2. 创建计薪配置，结束日由开始日推导
        HrmSalaryConfigDO config = BeanUtils.toBean(createReqVO, HrmSalaryConfigDO.class);
        config.setCycleEndDay(config.getCycleStartDay() == 1 ? 31 : config.getCycleStartDay() - 1);
        salaryConfigMapper.insert(config);

        // 3. 创建启用月份的工资表
        HrmSalaryMonthRecordCreateReqVO monthRecordCreateReqVO = new HrmSalaryMonthRecordCreateReqVO()
                .setYear(config.getStartYear()).setMonth(config.getStartMonth());
        salaryMonthRecordService.createMonthRecord(monthRecordCreateReqVO);
        return config.getId();
    }

    @Override
    @LogRecord(type = HRM_SALARY_CONFIG_TYPE, subType = HRM_SALARY_CONFIG_UPDATE_SUB_TYPE,
            bizNo = "{{#salaryConfig.id}}", success = HRM_SALARY_CONFIG_UPDATE_SUCCESS)
    public void updateSalaryConfig(HrmSalaryConfigUpdateReqVO updateReqVO) {
        // 1. 校验计薪配置存在
        HrmSalaryConfigDO config = validateSalaryConfigExists();

        // 2. 更新计薪配置
        salaryConfigMapper.updateById(new HrmSalaryConfigDO().setId(config.getId())
                .setSocialSecurityMonthType(updateReqVO.getSocialSecurityMonthType()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salaryConfig", config);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(config, HrmSalaryConfigUpdateReqVO.class));
    }

    @Override
    public HrmSalaryConfigDO getSalaryConfig() {
        return salaryConfigMapper.selectFirst();
    }

    private void validateSalaryConfigNotExists() {
        if (salaryConfigMapper.selectFirst() != null) {
            throw exception(SALARY_CONFIG_EXISTS);
        }
    }

    private HrmSalaryConfigDO validateSalaryConfigExists() {
        HrmSalaryConfigDO config = salaryConfigMapper.selectFirst();
        if (config == null) {
            throw exception(SALARY_CONFIG_NOT_EXISTS);
        }
        return config;
    }

}
