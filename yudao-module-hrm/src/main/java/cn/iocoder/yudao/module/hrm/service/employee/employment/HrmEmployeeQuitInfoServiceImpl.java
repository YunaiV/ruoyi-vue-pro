package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.quitinfo.HrmEmployeeQuitInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeQuitInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_INFO_NOT_EXISTS;

/**
 * HRM 员工离职信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeQuitInfoServiceImpl implements HrmEmployeeQuitInfoService {

    @Resource
    private HrmEmployeeQuitInfoMapper quitInfoMapper;

    @Override
    public Long saveEmployeeQuitInfo(HrmEmployeeQuitInfoSaveReqVO saveReqVO) {
        // 1. 校验员工离职信息是否存在
        HrmEmployeeQuitInfoDO quitInfo = BeanUtils.toBean(saveReqVO, HrmEmployeeQuitInfoDO.class);
        HrmEmployeeQuitInfoDO dbQuitInfo = quitInfoMapper.selectByEmployeeId(saveReqVO.getEmployeeId());
        // 2. 保存员工离职信息
        quitInfo.setId(dbQuitInfo == null ? null : dbQuitInfo.getId());
        quitInfoMapper.insertOrUpdate(quitInfo);
        return quitInfo.getId();
    }

    @Override
    public void deleteEmployeeQuitInfo(Long employeeId) {
        quitInfoMapper.deleteByEmployeeId(employeeId);
    }

    @Override
    public HrmEmployeeQuitInfoDO getQuitInfoByEmployeeId(Long employeeId) {
        return quitInfoMapper.selectByEmployeeId(employeeId);
    }

    @Override
    public List<HrmEmployeeQuitInfoDO> getQuitInfoListByEmployeeIds(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return quitInfoMapper.selectListByEmployeeIds(employeeIds);
    }

    @Override
    public HrmEmployeeQuitInfoDO validateQuitInfoByEmployeeId(Long employeeId) {
        HrmEmployeeQuitInfoDO quitInfo = getQuitInfoByEmployeeId(employeeId);
        if (quitInfo == null) {
            throw exception(EMPLOYEE_QUIT_INFO_NOT_EXISTS);
        }
        return quitInfo;
    }

    @Override
    public List<HrmEmployeeQuitInfoDO> getDueQuitInfoList(LocalDateTime deadlineTime) {
        return quitInfoMapper.selectListByPlanQuitTimeBeforeOrEqual(deadlineTime);
    }

}
