package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.LEVEL_LOG_NOT_EXISTS;

/**
 * 会员等级记录 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class MemberLevelRecordServiceImpl implements MemberLevelRecordService {

    @Resource
    private MemberLevelRecordMapper levelLogMapper;

    @Override
    public void deleteLevelLog(Long id) {
        // 校验存在
        validateLevelLogExists(id);
        // 删除
        levelLogMapper.deleteById(id);
    }

    private void validateLevelLogExists(Long id) {
        if (levelLogMapper.selectById(id) == null) {
            throw exception(LEVEL_LOG_NOT_EXISTS);
        }
    }

    @Override
    public MemberLevelRecordDO getLevelLog(Long id) {
        return levelLogMapper.selectById(id);
    }

    @Override
    public List<MemberLevelRecordDO> getLevelLogList(Collection<Long> ids) {
        return levelLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberLevelRecordDO> getLevelLogPage(MemberLevelRecordPageReqVO pageReqVO) {
        return levelLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void createCancelLog(Long userId, String reason) {
        MemberLevelRecordDO levelLogDO = new MemberLevelRecordDO();
        levelLogDO.setUserId(userId);
        levelLogDO.setRemark(reason);
        levelLogDO.setDescription("管理员取消");
        levelLogMapper.insert(levelLogDO);

        // 给会员发送等级变动消息
        notifyMember(userId, levelLogDO);
    }

    @Override
    public void createAdjustLog(MemberUserDO user, MemberLevelDO level, int experience, String reason) {
        MemberLevelRecordDO levelLogDO = new MemberLevelRecordDO();
        levelLogDO.setUserId(user.getId());
        levelLogDO.setLevelId(level.getId());
        levelLogDO.setLevel(level.getLevel());
        levelLogDO.setDiscount(level.getDiscount());
        levelLogDO.setUserExperience(level.getExperience());
        levelLogDO.setExperience(experience);
        levelLogDO.setRemark(reason);
        levelLogDO.setDescription("管理员调整为：" + level.getName());
        levelLogMapper.insert(levelLogDO);

        // 给会员发送等级变动消息
        notifyMember(user.getId(), levelLogDO);
    }

    @Override
    public void createAutoUpgradeLog(MemberUserDO user, MemberLevelDO level) {
        MemberLevelRecordDO levelLogDO = new MemberLevelRecordDO();
        levelLogDO.setUserId(user.getId());
        levelLogDO.setLevelId(level.getId());
        levelLogDO.setLevel(level.getLevel());
        levelLogDO.setDiscount(level.getDiscount());
        levelLogDO.setExperience(level.getExperience());
        levelLogDO.setUserExperience(user.getExperience());
        levelLogDO.setDescription("成为：" + level.getName());
        levelLogMapper.insert(levelLogDO);

        // 给会员发送等级变动消息
        notifyMember(user.getId(), levelLogDO);
    }

    private void notifyMember(Long userId, MemberLevelRecordDO level) {
        //todo: 给会员发消息
    }

}
