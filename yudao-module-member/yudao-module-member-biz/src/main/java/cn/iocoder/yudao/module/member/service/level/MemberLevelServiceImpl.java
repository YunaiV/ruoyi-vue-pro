package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.level.MemberLevelConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.LEVEL_NOT_EXISTS;

/**
 * 会员等级 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class MemberLevelServiceImpl implements MemberLevelService {

    @Resource
    private MemberLevelMapper levelMapper;

    @Override
    public Long createLevel(MemberLevelCreateReqVO createReqVO) {
        // 插入
        MemberLevelDO level = MemberLevelConvert.INSTANCE.convert(createReqVO);
        levelMapper.insert(level);
        // 返回
        return level.getId();
    }

    @Override
    public void updateLevel(MemberLevelUpdateReqVO updateReqVO) {
        // 校验存在
        validateLevelExists(updateReqVO.getId());
        // 更新
        MemberLevelDO updateObj = MemberLevelConvert.INSTANCE.convert(updateReqVO);
        levelMapper.updateById(updateObj);
    }

    @Override
    public void deleteLevel(Long id) {
        // 校验存在
        validateLevelExists(id);
        // 删除
        levelMapper.deleteById(id);
    }

    private void validateLevelExists(Long id) {
        if (levelMapper.selectById(id) == null) {
            throw exception(LEVEL_NOT_EXISTS);
        }
    }

    @Override
    public MemberLevelDO getLevel(Long id) {
        return levelMapper.selectById(id);
    }

    @Override
    public List<MemberLevelDO> getLevelList(Collection<Long> ids) {
        return levelMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberLevelDO> getLevelPage(MemberLevelPageReqVO pageReqVO) {
        return levelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberLevelDO> getLevelListByStatus(Integer status) {
        return levelMapper.selectListByStatus(status);
    }
}
