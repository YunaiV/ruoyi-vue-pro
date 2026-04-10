package cn.iocoder.yudao.module.mes.service.pro.workrecord;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workrecord.vo.MesProWorkRecordLogPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord.MesProWorkRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord.MesProWorkRecordLogDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.workrecord.MesProWorkRecordLogMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.workrecord.MesProWorkRecordMapper;
import cn.iocoder.yudao.module.mes.enums.pro.MesProWorkRecordTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WORK_RECORD_ALREADY_CLOCK_IN;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WORK_RECORD_NOT_CLOCK_IN;

/**
 * MES 工作记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProWorkRecordServiceImpl implements MesProWorkRecordService {

    @Resource
    private MesProWorkRecordLogMapper workRecordLogMapper;
    @Resource
    private MesProWorkRecordMapper workRecordMapper;
    @Resource
    private MesMdWorkstationService workstationService;

    @Override
    public PageResult<MesProWorkRecordLogDO> getWorkRecordLogPage(MesProWorkRecordLogPageReqVO pageReqVO) {
        return workRecordLogMapper.selectPage(pageReqVO);
    }

    @Override
    public MesProWorkRecordLogDO getWorkRecordLog(Long id) {
        return workRecordLogMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clockInWorkRecord(Long userId, Long workstationId) {
        // 1.1 校验工作站是否存在
        workstationService.validateWorkstationExists(workstationId);
        // 1.2 校验是否重复上线
        MesProWorkRecordDO record = workRecordMapper.selectByUserId(userId);
        if (record != null && ObjUtil.equal(MesProWorkRecordTypeEnum.CLOCK_IN.getType(), record.getType())) {
            throw exception(WORK_RECORD_ALREADY_CLOCK_IN);
        }

        // 2. 写入上线流水
        MesProWorkRecordLogDO log = MesProWorkRecordLogDO.builder().userId(userId)
                .workstationId(workstationId).type(MesProWorkRecordTypeEnum.CLOCK_IN.getType()).build();
        workRecordLogMapper.insert(log);

        // 3. 更新用户工作站状态：如果没有记录，或先删除旧记录
        if (record == null) {
            record = MesProWorkRecordDO.builder().userId(userId).workstationId(workstationId)
                    .type(MesProWorkRecordTypeEnum.CLOCK_IN.getType()).clockInTime(LocalDateTime.now()).build();
            workRecordMapper.insert(record);
        } else {
            workRecordMapper.updateById(new MesProWorkRecordDO().setId(record.getId())
                    .setWorkstationId(workstationId).setType(MesProWorkRecordTypeEnum.CLOCK_IN.getType())
                    .setClockInTime(LocalDateTime.now()).setClockOutTime(null));
        }
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clockOutWorkRecord(Long userId) {
        // 1. 校验是否有状态
        MesProWorkRecordDO record = workRecordMapper.selectByUserId(userId);
        if (record == null || ObjUtil.notEqual(MesProWorkRecordTypeEnum.CLOCK_IN.getType(), record.getType())) {
            throw exception(WORK_RECORD_NOT_CLOCK_IN);
        }

        // 2. 写入下线流水
        MesProWorkRecordLogDO log = MesProWorkRecordLogDO.builder().userId(userId)
                .workstationId(record.getWorkstationId()).type(MesProWorkRecordTypeEnum.CLOCK_OUT.getType())
                .build();
        workRecordLogMapper.insert(log);

        // 3. 更新用户状态
        workRecordMapper.updateById(new MesProWorkRecordDO().setId(record.getId())
                .setType(MesProWorkRecordTypeEnum.CLOCK_OUT.getType()).setClockOutTime(LocalDateTime.now()));
        return log.getId();
    }

    @Override
    public MesProWorkRecordDO getWorkRecord(Long userId) {
        return workRecordMapper.selectByUserId(userId);
    }

}
