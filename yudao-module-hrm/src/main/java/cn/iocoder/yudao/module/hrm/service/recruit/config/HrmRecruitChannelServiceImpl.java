package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitCandidateService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelDeleteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.config.HrmRecruitChannelMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_SYSTEM_NAME_UPDATE_FORBIDDEN;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_SYSTEM_DELETE_FORBIDDEN;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_TRANSFER_DISABLED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_TRANSFER_SELF;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * 招聘渠道 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmRecruitChannelServiceImpl implements HrmRecruitChannelService {

    @Resource
    private HrmRecruitChannelMapper recruitChannelMapper;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmRecruitCandidateService recruitCandidateService;

    @Override
    @LogRecord(type = HRM_RECRUIT_CHANNEL_TYPE, subType = HRM_RECRUIT_CHANNEL_CREATE_SUB_TYPE,
            bizNo = "{{#recruitChannelId}}", success = HRM_RECRUIT_CHANNEL_CREATE_SUCCESS)
    public Long createRecruitChannel(HrmRecruitChannelSaveReqVO createReqVO) {
        // 1. 插入招聘渠道
        HrmRecruitChannelDO recruitChannel = BeanUtils.toBean(createReqVO, HrmRecruitChannelDO.class)
                .setSystemFlag(Boolean.FALSE).setStatus(CommonStatusEnum.ENABLE.getStatus());
        recruitChannelMapper.insert(recruitChannel);

        // 2. 记录操作日志上下文
        LogRecordContext.putVariable("recruitChannelId", recruitChannel.getId());
        return recruitChannel.getId();
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_CHANNEL_TYPE, subType = HRM_RECRUIT_CHANNEL_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_RECRUIT_CHANNEL_UPDATE_SUCCESS)
    public void updateRecruitChannel(HrmRecruitChannelSaveReqVO updateReqVO) {
        // 1. 校验招聘渠道是否存在。系统内置渠道只允许调整排序和备注
        HrmRecruitChannelDO recruitChannel = validateRecruitChannelExists(updateReqVO.getId());
        if (Boolean.TRUE.equals(recruitChannel.getSystemFlag())
                && ObjUtil.notEqual(recruitChannel.getName(), updateReqVO.getName())) {
            throw exception(RECRUIT_CHANNEL_SYSTEM_NAME_UPDATE_FORBIDDEN);
        }

        // 2. 更新招聘渠道
        recruitChannelMapper.updateById(BeanUtils.toBean(updateReqVO, HrmRecruitChannelDO.class));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(recruitChannel, HrmRecruitChannelSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_CHANNEL_TYPE, subType = HRM_RECRUIT_CHANNEL_UPDATE_STATUS_SUB_TYPE,
            bizNo = "{{#statusReqVO.id}}", success = HRM_RECRUIT_CHANNEL_UPDATE_STATUS_SUCCESS)
    public void updateRecruitChannelStatus(HrmRecruitChannelStatusReqVO statusReqVO) {
        // 1. 校验招聘渠道是否存在
        HrmRecruitChannelDO recruitChannel = validateRecruitChannelExists(statusReqVO.getId());

        // 2. 更新招聘渠道状态
        recruitChannelMapper.updateById(new HrmRecruitChannelDO()
                .setId(statusReqVO.getId()).setStatus(statusReqVO.getStatus()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitChannel", recruitChannel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CHANNEL_TYPE, subType = HRM_RECRUIT_CHANNEL_DELETE_SUB_TYPE,
            bizNo = "{{#deleteReqVO.id}}", success = HRM_RECRUIT_CHANNEL_DELETE_SUCCESS)
    public void deleteRecruitChannel(HrmRecruitChannelDeleteReqVO deleteReqVO) {
        // 1.1 校验待删除招聘渠道
        HrmRecruitChannelDO recruitChannel = validateRecruitChannelExists(deleteReqVO.getId());
        if (Boolean.TRUE.equals(recruitChannel.getSystemFlag())) {
            throw exception(RECRUIT_CHANNEL_SYSTEM_DELETE_FORBIDDEN);
        }
        if (ObjUtil.equals(deleteReqVO.getId(), deleteReqVO.getTransferChannelId())) {
            throw exception(RECRUIT_CHANNEL_TRANSFER_SELF);
        }
        // 1.2 校验承接招聘渠道
        HrmRecruitChannelDO transferRecruitChannel = validateRecruitChannelExists(deleteReqVO.getTransferChannelId());
        if (!CommonStatusEnum.isEnable(transferRecruitChannel.getStatus())) {
            throw exception(RECRUIT_CHANNEL_TRANSFER_DISABLED);
        }

        // 2. 迁移员工、候选人的招聘渠道后，删除原渠道
        employeeService.updateEmployeeChannelByChannelId(recruitChannel.getId(), transferRecruitChannel.getId());
        recruitCandidateService.updateRecruitCandidateChannelByChannelId(
                recruitChannel.getId(), transferRecruitChannel.getId());
        recruitChannelMapper.deleteById(recruitChannel.getId());

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitChannel", recruitChannel);
        LogRecordContext.putVariable("transferRecruitChannel", transferRecruitChannel);
    }

    @Override
    public HrmRecruitChannelDO getRecruitChannel(Long id) {
        return recruitChannelMapper.selectById(id);
    }

    @Override
    public HrmRecruitChannelDO validateRecruitChannelExists(Long id) {
        HrmRecruitChannelDO recruitChannel = recruitChannelMapper.selectById(id);
        if (recruitChannel == null) {
            throw exception(RECRUIT_CHANNEL_NOT_EXISTS);
        }
        return recruitChannel;
    }

    @Override
    public List<HrmRecruitChannelDO> getRecruitChannelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return recruitChannelMapper.selectByIds(ids);
    }

    @Override
    public PageResult<HrmRecruitChannelDO> getRecruitChannelPage(HrmRecruitChannelPageReqVO pageReqVO) {
        return recruitChannelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmRecruitChannelDO> getRecruitChannelSimpleList() {
        return recruitChannelMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
