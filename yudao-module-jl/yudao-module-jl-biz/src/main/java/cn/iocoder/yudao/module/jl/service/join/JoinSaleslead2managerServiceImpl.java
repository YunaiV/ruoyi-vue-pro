package cn.iocoder.yudao.module.jl.service.join;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2managerConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2managerMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中的项目售前支持人员 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class JoinSaleslead2managerServiceImpl implements JoinSaleslead2managerService {

    @Resource
    private JoinSaleslead2managerMapper joinSaleslead2managerMapper;

    @Override
    public Long createJoinSaleslead2manager(JoinSaleslead2managerCreateReqVO createReqVO) {
        // 插入
        JoinSaleslead2managerDO joinSaleslead2manager = JoinSaleslead2managerConvert.INSTANCE.convert(createReqVO);
        joinSaleslead2managerMapper.insert(joinSaleslead2manager);
        // 返回
        return joinSaleslead2manager.getId();
    }

    @Override
    public void updateJoinSaleslead2manager(JoinSaleslead2managerUpdateReqVO updateReqVO) {
        // 校验存在
        validateJoinSaleslead2managerExists(updateReqVO.getId());
        // 更新
        JoinSaleslead2managerDO updateObj = JoinSaleslead2managerConvert.INSTANCE.convert(updateReqVO);
        joinSaleslead2managerMapper.updateById(updateObj);
    }

    @Override
    public void deleteJoinSaleslead2manager(Long id) {
        // 校验存在
        validateJoinSaleslead2managerExists(id);
        // 删除
        joinSaleslead2managerMapper.deleteById(id);
    }

    private void validateJoinSaleslead2managerExists(Long id) {
        if (joinSaleslead2managerMapper.selectById(id) == null) {
            throw exception(JOIN_SALESLEAD2MANAGER_NOT_EXISTS);
        }
    }

    @Override
    public JoinSaleslead2managerDO getJoinSaleslead2manager(Long id) {
        return joinSaleslead2managerMapper.selectById(id);
    }

    @Override
    public List<JoinSaleslead2managerDO> getJoinSaleslead2managerList(Collection<Long> ids) {
        return joinSaleslead2managerMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JoinSaleslead2managerDO> getJoinSaleslead2managerPage(JoinSaleslead2managerPageReqVO pageReqVO) {
        return joinSaleslead2managerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JoinSaleslead2managerDO> getJoinSaleslead2managerList(JoinSaleslead2managerExportReqVO exportReqVO) {
        return joinSaleslead2managerMapper.selectList(exportReqVO);
    }

    /**
     * @param salesleadId
     * @return
     */
    @Override
    public List<JoinSaleslead2managerDO> getBySalesleadId(Long salesleadId) {
        return joinSaleslead2managerMapper.selectBySalesleadId(salesleadId);
    }

}
