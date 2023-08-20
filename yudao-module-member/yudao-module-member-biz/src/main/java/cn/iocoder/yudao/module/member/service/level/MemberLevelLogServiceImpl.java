package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelLogMapper;
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
public class MemberLevelLogServiceImpl implements MemberLevelLogService {

    @Resource
    private MemberLevelLogMapper levelLogMapper;

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
    public MemberLevelLogDO getLevelLog(Long id) {
        return levelLogMapper.selectById(id);
    }

    @Override
    public List<MemberLevelLogDO> getLevelLogList(Collection<Long> ids) {
        return levelLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberLevelLogDO> getLevelLogPage(MemberLevelLogPageReqVO pageReqVO) {
        return levelLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberLevelLogDO> getLevelLogList(MemberLevelLogExportReqVO exportReqVO) {
        return levelLogMapper.selectList(exportReqVO);
    }

}
