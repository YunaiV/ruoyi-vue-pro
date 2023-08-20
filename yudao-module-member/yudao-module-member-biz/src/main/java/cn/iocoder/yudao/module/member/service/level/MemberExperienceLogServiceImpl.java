package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceLogDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberExperienceLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.EXPERIENCE_LOG_NOT_EXISTS;

/**
 * 会员经验记录 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class MemberExperienceLogServiceImpl implements MemberExperienceLogService {

    @Resource
    private MemberExperienceLogMapper experienceLogMapper;

    @Override
    public void deleteExperienceLog(Long id) {
        // 校验存在
        validateExperienceLogExists(id);
        // 删除
        experienceLogMapper.deleteById(id);
    }

    private void validateExperienceLogExists(Long id) {
        if (experienceLogMapper.selectById(id) == null) {
            throw exception(EXPERIENCE_LOG_NOT_EXISTS);
        }
    }

    @Override
    public MemberExperienceLogDO getExperienceLog(Long id) {
        return experienceLogMapper.selectById(id);
    }

    @Override
    public List<MemberExperienceLogDO> getExperienceLogList(Collection<Long> ids) {
        return experienceLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberExperienceLogDO> getExperienceLogPage(MemberExperienceLogPageReqVO pageReqVO) {
        return experienceLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberExperienceLogDO> getExperienceLogList(MemberExperienceLogExportReqVO exportReqVO) {
        return experienceLogMapper.selectList(exportReqVO);
    }

}
