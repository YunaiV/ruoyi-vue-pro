package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.MesCalTeamPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.MesCalTeamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.team.MesCalTeamMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 班组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesCalTeamServiceImpl implements MesCalTeamService {

    @Resource
    private MesCalTeamMapper teamMapper;
    @Resource
    @Lazy
    private MesCalTeamMemberService teamMemberService;
    @Resource
    @Lazy
    private MesCalTeamShiftService teamShiftService;

    @Override
    public Long createTeam(MesCalTeamSaveReqVO createReqVO) {
        // 1. 校验编码唯一
        validateTeamCodeUnique(null, createReqVO.getCode());

        // 2. 插入
        MesCalTeamDO team = BeanUtils.toBean(createReqVO, MesCalTeamDO.class);
        teamMapper.insert(team);
        return team.getId();
    }

    @Override
    public void updateTeam(MesCalTeamSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateTeamExists(updateReqVO.getId());
        // 1.2 校验编码唯一
        validateTeamCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 2. 更新
        MesCalTeamDO updateObj = BeanUtils.toBean(updateReqVO, MesCalTeamDO.class);
        teamMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Long id) {
        // 1. 校验存在
        validateTeamExists(id);

        // 2.1 级联删除成员
        teamMemberService.deleteByTeamId(id);
        // 2.2 级联删除排班记录
        teamShiftService.deleteByTeamId(id);
        // 2.3 删除班组
        teamMapper.deleteById(id);
    }

    @Override
    public MesCalTeamDO getTeam(Long id) {
        return teamMapper.selectById(id);
    }

    @Override
    public PageResult<MesCalTeamDO> getTeamPage(MesCalTeamPageReqVO pageReqVO) {
        return teamMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesCalTeamDO> getTeamList() {
        return teamMapper.selectList();
    }

    @Override
    public List<MesCalTeamDO> getTeamList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return teamMapper.selectByIds(ids);
    }

    @Override
    public MesCalTeamDO validateTeamExists(Long id) {
        MesCalTeamDO team = teamMapper.selectById(id);
        if (team == null) {
            throw exception(CAL_TEAM_NOT_EXISTS);
        }
        return team;
    }

    private void validateTeamCodeUnique(Long id, String code) {
        MesCalTeamDO team = teamMapper.selectByCode(code);
        if (team == null) {
            return;
        }
        if (ObjUtil.notEqual(id, team.getId())) {
            throw exception(CAL_TEAM_CODE_DUPLICATE);
        }
    }

}
