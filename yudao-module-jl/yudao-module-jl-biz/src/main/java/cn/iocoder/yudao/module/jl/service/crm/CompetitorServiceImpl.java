package cn.iocoder.yudao.module.jl.service.crm;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.CompetitorConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.CompetitorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 友商 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class CompetitorServiceImpl implements CompetitorService {

    @Resource
    private CompetitorMapper competitorMapper;

    @Override
    public Long createCompetitor(CompetitorCreateReqVO createReqVO) {
        // 插入
        CompetitorDO competitor = CompetitorConvert.INSTANCE.convert(createReqVO);
        competitorMapper.insert(competitor);
        // 返回
        return competitor.getId();
    }

    @Override
    public void updateCompetitor(CompetitorUpdateReqVO updateReqVO) {
        // 校验存在
        validateCompetitorExists(updateReqVO.getId());
        // 更新
        CompetitorDO updateObj = CompetitorConvert.INSTANCE.convert(updateReqVO);
        competitorMapper.updateById(updateObj);
    }

    @Override
    public void deleteCompetitor(Long id) {
        // 校验存在
        validateCompetitorExists(id);
        // 删除
        competitorMapper.deleteById(id);
    }

    private void validateCompetitorExists(Long id) {
        if (competitorMapper.selectById(id) == null) {
            throw exception(COMPETITOR_NOT_EXISTS);
        }
    }

    @Override
    public CompetitorDO getCompetitor(Long id) {
        return competitorMapper.selectById(id);
    }

    @Override
    public List<CompetitorDO> getCompetitorList(Collection<Long> ids) {
        return competitorMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CompetitorDO> getCompetitorPage(CompetitorPageReqVO pageReqVO) {
        return competitorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CompetitorDO> getCompetitorList(CompetitorExportReqVO exportReqVO) {
        return competitorMapper.selectList(exportReqVO);
    }

}
