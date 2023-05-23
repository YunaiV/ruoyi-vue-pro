package cn.iocoder.yudao.module.jl.service.crm;

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

    @Override
    public Long createSaleslead(SalesleadCreateReqVO createReqVO) {
        // 插入
        SalesleadDO saleslead = SalesleadConvert.INSTANCE.convert(createReqVO);
        salesleadMapper.insert(saleslead);
        // 返回
        return saleslead.getId();
    }

    @Override
    public void updateSaleslead(SalesleadUpdateReqVO updateReqVO) {
        // 校验存在
        validateSalesleadExists(updateReqVO.getId());
        // 更新
        SalesleadDO updateObj = SalesleadConvert.INSTANCE.convert(updateReqVO);
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
