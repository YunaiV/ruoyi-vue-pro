package cn.iocoder.yudao.module.jl.service.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Competitor;
import cn.iocoder.yudao.module.jl.mapper.CompetitorMapper;
import cn.iocoder.yudao.module.jl.repository.CompetitorRepository;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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
    private CompetitorRepository competitorRepository;

    @Resource
    private CompetitorMapper competitorMapper;

    @Override
    public Long createCompetitor(CompetitorCreateReq createReqVO) {
        // 插入
        Competitor competitorDo = competitorMapper.toEntity(createReqVO);
        Competitor result = competitorRepository.save(competitorDo);
        // 返回
        return result.getId();
    }

    @Override
    public void updateCompetitor(CompetitorDto updateReqVO) {
        // 校验存在
        validateCompetitorExists(updateReqVO.getId());
        // 更新
        Competitor updateObj = competitorMapper.toEntity(updateReqVO);
        competitorRepository.save(updateObj);
    }

    @Override
    public void deleteCompetitor(Long id) {
        // 校验存在
        validateCompetitorExists(id);
        // 删除
        competitorRepository.deleteById(id);
    }

    private void validateCompetitorExists(Long id) {
        competitorRepository.findById(id).orElseThrow(() -> exception(COMPETITOR_NOT_EXISTS));
    }

    @Override
    public CompetitorDto getCompetitor(Long id) {
        return competitorRepository.findById(id).map(competitorMapper::toDto).orElse(null);
    }

    @Override
    public List<CompetitorDto> getCompetitorList(Collection<Long> ids) {
        return StreamSupport.stream(competitorRepository.findAllById(ids).spliterator(), false)
                .map(competitorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CompetitorDto> getCompetitorPage(CompetitorPageReqVO pageReqVO) {
        return null;
    }

    @Override
    public List<CompetitorDto> getCompetitorList(CompetitorExportReqVO exportReqVO) {
        return null;
    }

}
