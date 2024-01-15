package cn.iocoder.yudao.module.crm.service.followup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.dal.mysql.followup.CrmFollowUpRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.FOLLOW_UP_RECORD_NOT_EXISTS;

/**
 * 跟进记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmFollowUpRecordServiceImpl implements CrmFollowUpRecordService {

    @Resource
    private CrmFollowUpRecordMapper crmFollowUpRecordMapper;

    // TODO @puhui999：数据权限
    @Override
    public Long createFollowUpRecord(CrmFollowUpRecordSaveReqVO createReqVO) {
        CrmFollowUpRecordDO followUpRecord = BeanUtils.toBean(createReqVO, CrmFollowUpRecordDO.class);
        crmFollowUpRecordMapper.insert(followUpRecord);
        // TODO @puhui999：需要更新 bizId 对应的记录；
        // TODO @puhui999：需要更新 businessIds、contactIds 对应的记录；
        return followUpRecord.getId();
    }

    // TODO @puhui999：不能编辑~~~
    @Override
    public void updateFollowUpRecord(CrmFollowUpRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateFollowUpRecordExists(updateReqVO.getId());
        // 更新
        CrmFollowUpRecordDO updateObj = BeanUtils.toBean(updateReqVO, CrmFollowUpRecordDO.class);
        crmFollowUpRecordMapper.updateById(updateObj);
    }

    // TODO @puhui999：数据权限
    @Override
    public void deleteFollowUpRecord(Long id) {
        // 校验存在
        validateFollowUpRecordExists(id);
        // 删除
        crmFollowUpRecordMapper.deleteById(id);
    }

    private void validateFollowUpRecordExists(Long id) {
        if (crmFollowUpRecordMapper.selectById(id) == null) {
            throw exception(FOLLOW_UP_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public CrmFollowUpRecordDO getFollowUpRecord(Long id) {
        return crmFollowUpRecordMapper.selectById(id);
    }

    // TODO @puhui999：数据权限
    @Override
    public PageResult<CrmFollowUpRecordDO> getFollowUpRecordPage(CrmFollowUpRecordPageReqVO pageReqVO) {
        return crmFollowUpRecordMapper.selectPage(pageReqVO);
    }

}