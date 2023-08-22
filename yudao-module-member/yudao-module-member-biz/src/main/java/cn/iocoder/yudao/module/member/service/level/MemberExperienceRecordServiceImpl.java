package cn.iocoder.yudao.module.member.service.level;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberExperienceRecordMapper;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 会员经验记录 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class MemberExperienceRecordServiceImpl implements MemberExperienceRecordService {

    @Resource
    private MemberExperienceRecordMapper experienceLogMapper;


    @Override
    public MemberExperienceRecordDO getExperienceLog(Long id) {
        return experienceLogMapper.selectById(id);
    }

    @Override
    public List<MemberExperienceRecordDO> getExperienceLogList(Collection<Long> ids) {
        return experienceLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberExperienceRecordDO> getExperienceLogPage(MemberExperienceRecordPageReqVO pageReqVO) {
        return experienceLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void createAdjustLog(Long userId, int experience, int totalExperience) {
        // 管理员调整时, 没有业务编号, 记录对应的枚举值
        String bizId = MemberExperienceBizTypeEnum.ADMIN.getValue() + "";
        this.createBizLog(userId, experience, totalExperience, MemberExperienceBizTypeEnum.ADMIN, bizId);
    }

    @Override
    public void createBizLog(Long userId, int experience, int totalExperience, MemberExperienceBizTypeEnum bizType, String bizId) {
        MemberExperienceRecordDO experienceLogDO = new MemberExperienceRecordDO();
        experienceLogDO.setUserId(userId);
        experienceLogDO.setExperience(experience);
        experienceLogDO.setTotalExperience(totalExperience);
        experienceLogDO.setBizId(bizId);
        experienceLogDO.setBizType(bizType.getValue());
        experienceLogDO.setTitle(bizType.getTitle());
        experienceLogDO.setDescription(StrUtil.format(bizType.getDesc(), experience));
        experienceLogMapper.insert(experienceLogDO);
    }

}
