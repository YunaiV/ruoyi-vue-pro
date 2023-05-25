package cn.iocoder.yudao.module.jl.service.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2competitorMapper;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2customerplanMapper;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2managerMapper;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2reportMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.SalesleadConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.SalesleadMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class SalesleadServiceImpl implements SalesleadService {

    @Resource
    private SalesleadMapper salesleadMapper;

    @Resource
    private JoinSaleslead2competitorMapper joinSaleslead2competitorMapper;

    @Resource
    private JoinSaleslead2customerplanMapper joinSaleslead2customerplanMapper;

    @Resource
    private JoinSaleslead2reportMapper joinSaleslead2reportMapper;

    @Resource
    private JoinSaleslead2managerMapper joinSaleslead2managerMapper;

    @Override
    public Long createSaleslead(SalesleadCreateReqVO createReqVO) {
        // 插入
        SalesleadDO saleslead = SalesleadConvert.INSTANCE.convert(createReqVO);
        salesleadMapper.insert(saleslead);

        // 写入关联表
        // 将 joinSaleslead2report 写入
        if (createReqVO.getReportUrl() != null) {
            JoinSaleslead2reportDO joinSaleslead2report = new JoinSaleslead2reportDO();
            joinSaleslead2report.setSalesleadId(saleslead.getId());
            joinSaleslead2report.setFileName(createReqVO.getReportDocName());
            joinSaleslead2report.setFileUrl(createReqVO.getReportUrl());
            joinSaleslead2reportMapper.insert(joinSaleslead2report);
        }

        // 将 joinSaleslead2manager 写入
        if (createReqVO.getManagerId() != null) {
            JoinSaleslead2managerDO joinSaleslead2manager = new JoinSaleslead2managerDO();
            joinSaleslead2manager.setSalesleadId(saleslead.getId());
            joinSaleslead2manager.setManagerId(createReqVO.getManagerId());
            joinSaleslead2managerMapper.insert(joinSaleslead2manager);
        }

        // 将 joinSaleslead2customerplan 写入
        if(createReqVO.getPlanUrl() != null) {
            JoinSaleslead2customerplanDO joinSaleslead2customerplan = new JoinSaleslead2customerplanDO();
            joinSaleslead2customerplan.setSalesleadId(saleslead.getId());
            joinSaleslead2customerplan.setFileUrl(createReqVO.getPlanUrl());
            joinSaleslead2customerplan.setFileName(createReqVO.getPlanDocName());
            joinSaleslead2customerplanMapper.insert(joinSaleslead2customerplan);
        }

        // 将 joinSaleslead2competitorDO 写入，createReqVO.getCompetitorQuotations() 是数组，需要循环写入
        if(createReqVO.getCompetitorQuotations() != null) {
            for (SalesleadCompetitorQuotation competitorQuotation : createReqVO.getCompetitorQuotations()) {
                if(competitorQuotation.getCompetitorId() != null) {
                    JoinSaleslead2competitorDO joinSaleslead2competitorDO = new JoinSaleslead2competitorDO();
                    joinSaleslead2competitorDO.setSalesleadId(saleslead.getId());
                    joinSaleslead2competitorDO.setCompetitorId(competitorQuotation.getCompetitorId());
                    joinSaleslead2competitorDO.setCompetitorQuotation(competitorQuotation.getCompetitorQuotation());
                    joinSaleslead2competitorMapper.insert(joinSaleslead2competitorDO);
                }

            }
        }

        // 返回
        return saleslead.getId();
    }

    @Override
    public void updateSaleslead(SalesleadUpdateReqVO updateReqVO) {
        // 校验存在
        validateSalesleadExists(updateReqVO.getId());
        // 更新
        SalesleadDO updateObj = SalesleadConvert.INSTANCE.convert(updateReqVO);

        // 更新关联表（插入新数据即可，查找时，会将最新数据放在上面）
        // 写入 joinSaleslead2report
        if (updateReqVO.getReportUrl() != null) {
            JoinSaleslead2reportDO joinSaleslead2report = new JoinSaleslead2reportDO();
            joinSaleslead2report.setSalesleadId(updateReqVO.getId());
            joinSaleslead2report.setFileName(updateReqVO.getReportDocName());
            joinSaleslead2report.setFileUrl(updateReqVO.getReportUrl());
            joinSaleslead2reportMapper.insert(joinSaleslead2report);
        }

        // 写入 joinSaleslead2manager
        if (updateReqVO.getManagerId() != null) {
            JoinSaleslead2managerDO joinSaleslead2manager = new JoinSaleslead2managerDO();
            joinSaleslead2manager.setSalesleadId(updateReqVO.getId());
            joinSaleslead2manager.setManagerId(updateReqVO.getManagerId());
            joinSaleslead2managerMapper.insert(joinSaleslead2manager);
        }

        // 写入 joinSaleslead2customerplan
        if(updateReqVO.getPlanUrl() != null) {
            JoinSaleslead2customerplanDO joinSaleslead2customerplan = new JoinSaleslead2customerplanDO();
            joinSaleslead2customerplan.setSalesleadId(updateReqVO.getId());
            joinSaleslead2customerplan.setFileUrl(updateReqVO.getPlanUrl());
            joinSaleslead2customerplan.setFileName(updateReqVO.getPlanDocName());
            joinSaleslead2customerplanMapper.insert(joinSaleslead2customerplan);
        }

        // 写入 joinSaleslead2competitorDO
        if(updateReqVO.getCompetitorQuotations() != null) {
            // 删除旧的数据
            joinSaleslead2competitorMapper.deleteByMap(new HashMap<String, Object>() {{
                put("saleslead_id", updateReqVO.getId());
            }});

            for (SalesleadCompetitorQuotation competitorQuotation : updateReqVO.getCompetitorQuotations()) {
                if(competitorQuotation.getCompetitorId() != null) {
                    // 插入新数据
                    JoinSaleslead2competitorDO joinSaleslead2competitorDO = new JoinSaleslead2competitorDO();
                    joinSaleslead2competitorDO.setSalesleadId(updateReqVO.getId());
                    joinSaleslead2competitorDO.setCompetitorId(competitorQuotation.getCompetitorId());
                    joinSaleslead2competitorDO.setCompetitorQuotation(competitorQuotation.getCompetitorQuotation());
                    joinSaleslead2competitorMapper.insert(joinSaleslead2competitorDO);
                }
            }
        }

        salesleadMapper.updateById(updateObj);
    }

    @Override
    public void deleteSaleslead(Long id) {
        // 校验存在
        validateSalesleadExists(id);
        // 删除
        salesleadMapper.deleteById(id);
    }

    private void validateSalesleadExists(Long id) {
        if (salesleadMapper.selectById(id) == null) {
            throw exception(SALESLEAD_NOT_EXISTS);
        }
    }

    @Override
    public SalesleadDO getSaleslead(Long id) {
        return salesleadMapper.selectById(id);
    }

    @Override
    public List<SalesleadDO> getSalesleadList(Collection<Long> ids) {
        return salesleadMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SalesleadDO> getSalesleadPage(SalesleadPageReqVO pageReqVO) {
        return salesleadMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesleadDO> getSalesleadList(SalesleadExportReqVO exportReqVO) {
        return salesleadMapper.selectList(exportReqVO);
    }

}
