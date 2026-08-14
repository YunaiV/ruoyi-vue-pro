package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipRemarkReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipMapper;
import cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipReadStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_ALREADY_SENT;

/**
 * HRM 工资条 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalarySlipServiceImpl implements HrmSalarySlipService {

    @Resource
    private HrmSalarySlipMapper salarySlipMapper;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSalarySlipList(Long sendRecordId,
                                     List<HrmSalaryMonthEmployeeRecordDO> employeeRecords,
                                     HrmSalarySlipTemplateDO template) {
        // 1. 获得员工和模板快照
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
        List<HrmSalarySlipTemplateDO.Option> templateOptions = template.getOptions();

        // 2. 构建员工工资条，并保证同一员工月记录只存在一张有效工资条
        List<HrmSalarySlipDO> salarySlips = convertList(employeeRecords, employeeRecord ->
                HrmSalarySlipDO.builder()
                    .sendRecordId(sendRecordId).monthEmployeeRecordId(employeeRecord.getId())
                    .employeeId(employeeRecord.getEmployeeId()).year(employeeRecord.getYear())
                    .month(employeeRecord.getMonth()).readStatus(HrmSalarySlipReadStatusEnum.UNREAD.getStatus())
                    .realPaySalary(employeeRecord.getRealPaySalary()).remark("")
                    .options(buildSalarySlipOptions(employeeRecord, template, templateOptions)).build());
        HrmSalaryMonthEmployeeRecordDO firstEmployeeRecord = CollUtil.getFirst(employeeRecords);
        Set<Long> employeeIds = convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        List<HrmSalarySlipDO> sentEmployeeSlips = salarySlipMapper.selectListByEmployeeIdsAndYearMonth(
                employeeIds,
                firstEmployeeRecord.getYear(), firstEmployeeRecord.getMonth());
        if (employeeIds.size() != employeeRecords.size() || CollUtil.isNotEmpty(sentEmployeeSlips)) {
            throw exception(SALARY_SLIP_ALREADY_SENT);
        }
        for (HrmSalarySlipDO salarySlip : salarySlips) {
            salarySlipMapper.insert(salarySlip);
        }

        // 3. 逐个发送工资条通知
        for (HrmSalaryMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            sendSalarySlipMessage(employeeMap.get(employeeRecord.getEmployeeId()), employeeRecord);
        }
    }

    @Override
    public void deleteSalarySlipListBySendRecordId(Long sendRecordId) {
        salarySlipMapper.deleteBySendRecordId(sendRecordId);
    }

    @Override
    public Map<Long, Long> getSalarySlipReadCountMap(Collection<Long> sendRecordIds) {
        if (CollUtil.isEmpty(sendRecordIds)) {
            return Collections.emptyMap();
        }
        return salarySlipMapper.selectCountMapBySendRecordIdsAndReadStatus(sendRecordIds, HrmSalarySlipReadStatusEnum.READ.getStatus());
    }

    @Override
    public PageResult<HrmSalarySlipDO> getSalarySlipPage(HrmSalarySlipPageReqVO reqVO) {
        return salarySlipMapper.selectPage(reqVO);
    }

    @Override
    public HrmSalarySlipDO getSalarySlip(Long id) {
        return salarySlipMapper.selectById(id);
    }

    @Override
    public List<HrmSalarySlipDO> getSalarySlipListByEmployeeId(
            Long employeeId, YearMonth startMonth, YearMonth endMonth,
            Integer orderType, Integer order) {
        return salarySlipMapper.selectListByEmployeeId(
                employeeId, startMonth, endMonth, orderType, order);
    }

    @Override
    public HrmSalarySlipDO getSalarySlipByIdAndEmployeeId(Long id, Long employeeId) {
        return validateSalarySlipByEmployeeId(id, employeeId);
    }

    @Override
    public Set<Long> getSentMonthEmployeeRecordIdSet(Collection<Long> monthEmployeeRecordIds) {
        if (CollUtil.isEmpty(monthEmployeeRecordIds)) {
            return Collections.emptySet();
        }
        List<HrmSalarySlipDO> salarySlips =
                salarySlipMapper.selectListByMonthEmployeeRecordIds(monthEmployeeRecordIds);
        return convertSet(salarySlips, HrmSalarySlipDO::getMonthEmployeeRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSalarySlipListRead(Long employeeId, List<Long> ids) {
        // 1. 校验工资条均属于指定员工
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(ids));
        List<HrmSalarySlipDO> salarySlips = salarySlipMapper.selectByIds(distinctIds);
        if (salarySlips.size() != distinctIds.size()
                || anyMatch(salarySlips, slip -> ObjectUtil.notEqual(slip.getEmployeeId(), employeeId))) {
            throw exception(SALARY_SLIP_NOT_EXISTS);
        }

        // 2. 更新阅读状态
        salarySlipMapper.updateByIds(distinctIds,
                new HrmSalarySlipDO().setReadStatus(HrmSalarySlipReadStatusEnum.READ.getStatus()));
    }

    @Override
    public void updateSalarySlipRemark(HrmSalarySlipRemarkReqVO reqVO) {
        // 1. 校验工资条存在
        validateSalarySlipExists(reqVO.getId());

        // 2. 更新工资条备注
        salarySlipMapper.updateById(new HrmSalarySlipDO().setId(reqVO.getId()).setRemark(reqVO.getRemark()));
    }

    private void validateSalarySlipExists(Long id) {
        if (salarySlipMapper.selectById(id) == null) {
            throw exception(SALARY_SLIP_NOT_EXISTS);
        }
    }

    private HrmSalarySlipDO validateSalarySlipByEmployeeId(Long id, Long employeeId) {
        HrmSalarySlipDO salarySlip = salarySlipMapper.selectById(id);
        if (salarySlip == null || ObjectUtil.notEqual(salarySlip.getEmployeeId(), employeeId)) {
            throw exception(SALARY_SLIP_NOT_EXISTS);
        }
        return salarySlip;
    }

    private List<HrmSalarySlipDO.Option> buildSalarySlipOptions(
            HrmSalaryMonthEmployeeRecordDO employeeRecord, HrmSalarySlipTemplateDO template,
            List<HrmSalarySlipTemplateDO.Option> templateOptions) {
        // 1. 构建薪资项值 Map
        Map<Integer, HrmSalaryMonthEmployeeRecordDO.OptionValue> valueMap = convertMap(
                employeeRecord.getOptionValues(), HrmSalaryMonthEmployeeRecordDO.OptionValue::getCode);

        // 2. 获得并排序模板项；空模板按本月薪资项生成默认明细
        List<HrmSalarySlipTemplateDO.Option> options = convertList(templateOptions, option -> option);
        if (CollUtil.isEmpty(options)) {
            options = convertList(valueMap.values(), value -> HrmSalarySlipTemplateDO.Option.builder()
                            .name(value.getName()).type(HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType()).code(value.getCode())
                            .hidden(false).sort(value.getCode()).children(Collections.emptyList()).build());
        }
        options.sort(Comparator.comparing(item -> item.getSort() == null
                ? Integer.MAX_VALUE : item.getSort()));

        // 3. 按模板结构生成工资条项
        boolean hideEmpty = Boolean.TRUE.equals(template.getHideEmpty());
        List<HrmSalarySlipDO.Option> salarySlipOptions = new ArrayList<>();
        for (HrmSalarySlipTemplateDO.Option option : options) {
            if (Boolean.TRUE.equals(option.getHidden())) {
                continue;
            }
            if (ObjectUtil.notEqual(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType())) {
                addSalarySlipOptionIfVisible(salarySlipOptions, option, valueMap, hideEmpty);
                continue;
            }
            List<HrmSalarySlipDO.Option> children = new ArrayList<>();
            List<HrmSalarySlipTemplateDO.Option> templateChildren =
                    convertList(option.getChildren(), child -> child);
            templateChildren.sort(Comparator.comparing(item -> item.getSort() == null
                    ? Integer.MAX_VALUE : item.getSort()));
            for (HrmSalarySlipTemplateDO.Option child : templateChildren) {
                addSalarySlipOptionIfVisible(children, child, valueMap, hideEmpty);
            }
            if (CollUtil.isNotEmpty(children)) {
                salarySlipOptions.add(buildSalarySlipOption(option, BigDecimal.ZERO, children));
            }
        }
        return salarySlipOptions;
    }

    private void addSalarySlipOptionIfVisible(List<HrmSalarySlipDO.Option> options,
                                              HrmSalarySlipTemplateDO.Option templateOption,
                                              Map<Integer, HrmSalaryMonthEmployeeRecordDO.OptionValue> valueMap,
                                              boolean hideEmpty) {
        if (Boolean.TRUE.equals(templateOption.getHidden())) {
            return;
        }
        HrmSalaryMonthEmployeeRecordDO.OptionValue value = valueMap.get(templateOption.getCode());
        if (hideEmpty && (value == null || value.getValue().signum() == 0)) {
            return;
        }
        options.add(buildSalarySlipOption(templateOption,
                value == null ? BigDecimal.ZERO : value.getValue(), Collections.emptyList()));
    }

    private HrmSalarySlipDO.Option buildSalarySlipOption(HrmSalarySlipTemplateDO.Option option,
                                                         BigDecimal value,
                                                         List<HrmSalarySlipDO.Option> children) {
        return HrmSalarySlipDO.Option.builder()
                .name(option.getName()).type(option.getType()).code(option.getCode()).value(value)
                .remark(option.getRemark()).sort(option.getSort()).children(children).build();
    }

    private void sendSalarySlipMessage(HrmEmployeeDO employee,
                                       HrmSalaryMonthEmployeeRecordDO employeeRecord) {
        if (employee == null || employee.getUserId() == null) {
            return;
        }
        Map<String, Object> templateParams = new HashMap<>(4);
        templateParams.put("employeeName", employee.getName());
        templateParams.put("year", employeeRecord.getYear());
        templateParams.put("month", employeeRecord.getMonth());
        templateParams.put("realSalary", employeeRecord.getRealPaySalary().toPlainString());
        notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                .setUserId(employee.getUserId()).setTemplateCode(MessageTemplateConstants.SALARY_SLIP_SENT)
                .setTemplateParams(templateParams));
    }

}
