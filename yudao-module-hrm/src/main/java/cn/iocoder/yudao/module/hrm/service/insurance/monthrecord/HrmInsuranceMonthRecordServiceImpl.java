package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.HrmInsuranceMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.monthrecord.HrmInsuranceMonthRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.monthrecord.HrmInsuranceMonthStatusEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_FIRST_MONTH_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_ARCHIVED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_CANNOT_DELETE_ONLY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_NOT_LATEST;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_TYPE;

/**
 * HRM 月度社保 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmInsuranceMonthRecordServiceImpl implements HrmInsuranceMonthRecordService {

    private static final String MONTH_RECORD_TITLE_TEMPLATE = "%d年%d月社保表";
    @Resource
    private HrmInsuranceMonthRecordMapper monthRecordMapper;

    @Resource
    private HrmInsuranceMonthEmployeeRecordService monthEmployeeRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_CREATE_SUB_TYPE,
            bizNo = "{{#monthRecord.id}}", success = HRM_INSURANCE_MONTH_CREATE_SUCCESS)
    public Long createFirstMonthRecord(HrmInsuranceMonthRecordCreateReqVO reqVO) {
        // 1. 校验尚未生成社保表
        if (monthRecordMapper.selectLastForUpdate() != null) {
            throw exception(INSURANCE_FIRST_MONTH_RECORD_EXISTS);
        }

        // 2.1 创建首月社保表
        HrmInsuranceMonthRecordDO monthRecord = createMonthRecord(reqVO);
        // 2.2 生成员工月记录并更新汇总
        computeMonthRecord(monthRecord);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("monthRecord", monthRecord);
        return monthRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_CREATE_SUB_TYPE,
            bizNo = "{{#monthRecord.id}}", success = HRM_INSURANCE_MONTH_CREATE_SUCCESS)
    public Long createNextMonthRecord() {
        // 1. 校验最近月度社保表
        HrmInsuranceMonthRecordDO lastMonthRecord = monthRecordMapper.selectLastForUpdate();
        if (lastMonthRecord == null) {
            throw exception(INSURANCE_MONTH_RECORD_NOT_EXISTS);
        }

        // 2. 归档最近月度社保表
        if (Objects.equals(lastMonthRecord.getStatus(), HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus())) {
            archiveMonthRecord(lastMonthRecord);
        }

        // 3.1 创建次月社保表
        YearMonth nextMonth = YearMonth.of(lastMonthRecord.getYear(), lastMonthRecord.getMonth()).plusMonths(1);
        HrmInsuranceMonthRecordDO monthRecord = createMonthRecord(new HrmInsuranceMonthRecordCreateReqVO()
                .setYear(nextMonth.getYear()).setMonth(nextMonth.getMonthValue()));
        // 3.2 生成员工月记录并更新汇总
        computeMonthRecord(monthRecord);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("monthRecord", monthRecord);
        return monthRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_DELETE_SUB_TYPE,
            bizNo = "{{#monthRecord.id}}", success = HRM_INSURANCE_MONTH_DELETE_SUCCESS)
    public void deleteMonthRecord(Long id) {
        // 1. 校验仅删除最新社保表，并至少保留一张社保表
        HrmInsuranceMonthRecordDO monthRecord = validateMonthRecordEditableForUpdate(id);
        HrmInsuranceMonthRecordDO lastMonthRecord = monthRecordMapper.selectLast();
        if (lastMonthRecord == null || ObjectUtil.notEqual(lastMonthRecord.getId(), monthRecord.getId())) {
            throw exception(INSURANCE_MONTH_RECORD_NOT_LATEST);
        }
        if (monthRecordMapper.selectCount() <= 1) {
            throw exception(INSURANCE_MONTH_RECORD_CANNOT_DELETE_ONLY);
        }

        // 2.1 删除当前月员工记录和月表
        monthEmployeeRecordService.deleteMonthEmployeeRecordListByMonthRecordId(id);
        monthRecordMapper.deleteById(id);
        // 2.2 将上一月调整为可编辑状态
        HrmInsuranceMonthRecordDO previousMonthRecord = monthRecordMapper.selectLast();
        monthRecordMapper.updateById(new HrmInsuranceMonthRecordDO()
                .setId(previousMonthRecord.getId())
                .setStatus(HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("monthRecord", monthRecord);
    }

    @Override
    public HrmInsuranceMonthRecordDO getMonthRecord(Long id) {
        return monthRecordMapper.selectById(id);
    }

    @Override
    public HrmInsuranceMonthRecordDO getLastMonthRecord() {
        return monthRecordMapper.selectLast();
    }

    @Override
    public List<HrmInsuranceMonthRecordDO> getMonthRecordList(Integer year) {
        return monthRecordMapper.selectListByYear(year);
    }

    @Override
    public HrmInsuranceMonthRecordDO validateMonthRecordExists(Long id) {
        HrmInsuranceMonthRecordDO monthRecord = monthRecordMapper.selectById(id);
        if (monthRecord == null) {
            throw exception(INSURANCE_MONTH_RECORD_NOT_EXISTS);
        }
        return monthRecord;
    }

    @Override
    public HrmInsuranceMonthRecordDO validateMonthRecordExists(Integer year, Integer month) {
        HrmInsuranceMonthRecordDO monthRecord = monthRecordMapper.selectByYearMonth(year, month);
        if (monthRecord == null) {
            throw exception(INSURANCE_MONTH_RECORD_NOT_EXISTS);
        }
        return monthRecord;
    }

    @Override
    public HrmInsuranceMonthRecordDO validateMonthRecordEditableForUpdate(Long id) {
        HrmInsuranceMonthRecordDO monthRecord = monthRecordMapper.selectByIdForUpdate(id);
        if (monthRecord == null) {
            throw exception(INSURANCE_MONTH_RECORD_NOT_EXISTS);
        }
        if (Objects.equals(monthRecord.getStatus(), HrmInsuranceMonthStatusEnum.ARCHIVED.getStatus())) {
            throw exception(INSURANCE_MONTH_RECORD_ARCHIVED);
        }
        return monthRecord;
    }

    /**
     * 根据员工月记录重新计算月度社保表汇总
     *
     * @param id 月度社保表编号
     */
    @Override
    public void updateMonthRecordSummary(Long id) {
        validateMonthRecordExists(id);
        List<HrmInsuranceMonthEmployeeRecordDO> employeeRecords =
                monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(id);
        BigDecimal personalInsuranceAmount = BigDecimal.ZERO;
        BigDecimal personalProvidentFundAmount = BigDecimal.ZERO;
        BigDecimal corporateInsuranceAmount = BigDecimal.ZERO;
        BigDecimal corporateProvidentFundAmount = BigDecimal.ZERO;
        int stoppedEmployeeCount = 0;
        for (HrmInsuranceMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            if (Objects.equals(employeeRecord.getStatus(), HrmInsuranceEmployeeStatusEnum.STOPPED.getStatus())) {
                stoppedEmployeeCount++;
                continue;
            }
            personalInsuranceAmount = personalInsuranceAmount.add(
                    ObjectUtil.defaultIfNull(employeeRecord.getPersonalInsuranceAmount(), BigDecimal.ZERO));
            personalProvidentFundAmount = personalProvidentFundAmount.add(
                    ObjectUtil.defaultIfNull(employeeRecord.getPersonalProvidentFundAmount(), BigDecimal.ZERO));
            corporateInsuranceAmount = corporateInsuranceAmount.add(
                    ObjectUtil.defaultIfNull(employeeRecord.getCorporateInsuranceAmount(), BigDecimal.ZERO));
            corporateProvidentFundAmount = corporateProvidentFundAmount.add(
                    ObjectUtil.defaultIfNull(employeeRecord.getCorporateProvidentFundAmount(), BigDecimal.ZERO));
        }
        monthRecordMapper.updateById(new HrmInsuranceMonthRecordDO().setId(id)
                .setInsuredEmployeeCount(employeeRecords.size() - stoppedEmployeeCount)
                .setStoppedEmployeeCount(stoppedEmployeeCount)
                .setPersonalInsuranceAmount(priceScale(personalInsuranceAmount))
                .setPersonalProvidentFundAmount(priceScale(personalProvidentFundAmount))
                .setCorporateInsuranceAmount(priceScale(corporateInsuranceAmount))
                .setCorporateProvidentFundAmount(priceScale(corporateProvidentFundAmount)));
    }

    private HrmInsuranceMonthRecordDO createMonthRecord(HrmInsuranceMonthRecordCreateReqVO reqVO) {
        // 1. 校验年月唯一性
        if (monthRecordMapper.selectByYearMonth(reqVO.getYear(), reqVO.getMonth()) != null) {
            throw exception(INSURANCE_MONTH_RECORD_EXISTS);
        }

        // 2. 创建月度社保表
        HrmInsuranceMonthRecordDO monthRecord = BeanUtils.toBean(reqVO, HrmInsuranceMonthRecordDO.class)
                .setTitle(String.format(MONTH_RECORD_TITLE_TEMPLATE, reqVO.getYear(), reqVO.getMonth()))
                .setInsuredEmployeeCount(0).setStoppedEmployeeCount(0)
                .setStatus(HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus())
                .setPersonalInsuranceAmount(BigDecimal.ZERO).setPersonalProvidentFundAmount(BigDecimal.ZERO)
                .setCorporateInsuranceAmount(BigDecimal.ZERO).setCorporateProvidentFundAmount(BigDecimal.ZERO);
        monthRecordMapper.insert(monthRecord);
        return monthRecord;
    }

    private void computeMonthRecord(HrmInsuranceMonthRecordDO monthRecord) {
        // 1. 生成员工月记录
        monthEmployeeRecordService.createMonthEmployeeRecordList(monthRecord);
        // 2. 更新月表汇总
        updateMonthRecordSummary(monthRecord.getId());
    }

    private void archiveMonthRecord(HrmInsuranceMonthRecordDO monthRecord) {
        monthRecordMapper.updateById(new HrmInsuranceMonthRecordDO()
                .setId(monthRecord.getId()).setStatus(HrmInsuranceMonthStatusEnum.ARCHIVED.getStatus()));
    }

}
