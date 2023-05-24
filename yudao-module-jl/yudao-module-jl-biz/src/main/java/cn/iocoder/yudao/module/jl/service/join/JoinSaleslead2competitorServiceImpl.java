package cn.iocoder.yudao.module.jl.service.join;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2competitorConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2competitorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中竞争对手的报价 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class JoinSaleslead2competitorServiceImpl implements JoinSaleslead2competitorService {

    @Resource
    private JoinSaleslead2competitorMapper joinSaleslead2competitorMapper;

    @Override
    public Long createJoinSaleslead2competitor(JoinSaleslead2competitorCreateReqVO createReqVO) {
        // 插入
        JoinSaleslead2competitorDO joinSaleslead2competitor = JoinSaleslead2competitorConvert.INSTANCE.convert(createReqVO);
        joinSaleslead2competitorMapper.insert(joinSaleslead2competitor);
        // 返回
        return joinSaleslead2competitor.getId();
    }

    @Override
    public void updateJoinSaleslead2competitor(JoinSaleslead2competitorUpdateReqVO updateReqVO) {
        // 校验存在
        validateJoinSaleslead2competitorExists(updateReqVO.getId());
        // 更新
        JoinSaleslead2competitorDO updateObj = JoinSaleslead2competitorConvert.INSTANCE.convert(updateReqVO);
        joinSaleslead2competitorMapper.updateById(updateObj);
    }

    @Override
    public void deleteJoinSaleslead2competitor(Long id) {
        // 校验存在
        validateJoinSaleslead2competitorExists(id);
        // 删除
        joinSaleslead2competitorMapper.deleteById(id);
    }

    private void validateJoinSaleslead2competitorExists(Long id) {
        if (joinSaleslead2competitorMapper.selectById(id) == null) {
            throw exception(JOIN_SALESLEAD2COMPETITOR_NOT_EXISTS);
        }
    }

    @Override
    public JoinSaleslead2competitorDO getJoinSaleslead2competitor(Long id) {
        return joinSaleslead2competitorMapper.selectById(id);
    }

    @Override
    public List<JoinSaleslead2competitorDO> getCompetitorBySalesleadId(Long id) {
        return joinSaleslead2competitorMapper.getCompetitorsBySalesleadId(id);
    }

    @Override
    public List<JoinSaleslead2competitorDO> getJoinSaleslead2competitorList(Collection<Long> ids) {
        return joinSaleslead2competitorMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JoinSaleslead2competitorDO> getJoinSaleslead2competitorPage(JoinSaleslead2competitorPageReqVO pageReqVO) {
        return joinSaleslead2competitorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JoinSaleslead2competitorDO> getJoinSaleslead2competitorList(JoinSaleslead2competitorExportReqVO exportReqVO) {
        return joinSaleslead2competitorMapper.selectList(exportReqVO);
    }

}
