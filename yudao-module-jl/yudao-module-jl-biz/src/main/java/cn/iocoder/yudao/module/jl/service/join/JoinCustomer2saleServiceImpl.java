package cn.iocoder.yudao.module.jl.service.join;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinCustomer2saleDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.join.JoinCustomer2saleConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinCustomer2saleMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 客户所属的销售人员 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class JoinCustomer2saleServiceImpl implements JoinCustomer2saleService {

    @Resource
    private JoinCustomer2saleMapper joinCustomer2saleMapper;

    @Override
    public Long createJoinCustomer2sale(JoinCustomer2saleCreateReqVO createReqVO) {
        // 插入
        JoinCustomer2saleDO joinCustomer2sale = JoinCustomer2saleConvert.INSTANCE.convert(createReqVO);
        joinCustomer2saleMapper.insert(joinCustomer2sale);
        // 返回
        return joinCustomer2sale.getId();
    }

    @Override
    public void updateJoinCustomer2sale(JoinCustomer2saleUpdateReqVO updateReqVO) {
        // 校验存在
        validateJoinCustomer2saleExists(updateReqVO.getId());
        // 更新
        JoinCustomer2saleDO updateObj = JoinCustomer2saleConvert.INSTANCE.convert(updateReqVO);
        joinCustomer2saleMapper.updateById(updateObj);
    }

    @Override
    public void deleteJoinCustomer2sale(Long id) {
        // 校验存在
        validateJoinCustomer2saleExists(id);
        // 删除
        joinCustomer2saleMapper.deleteById(id);
    }

    private void validateJoinCustomer2saleExists(Long id) {
        if (joinCustomer2saleMapper.selectById(id) == null) {
            throw exception(JOIN_CUSTOMER2SALE_NOT_EXISTS);
        }
    }

    @Override
    public JoinCustomer2saleDO getJoinCustomer2sale(Long id) {
        return joinCustomer2saleMapper.selectById(id);
    }

    @Override
    public List<JoinCustomer2saleDO> getJoinCustomer2saleList(Collection<Long> ids) {
        return joinCustomer2saleMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JoinCustomer2saleDO> getJoinCustomer2salePage(JoinCustomer2salePageReqVO pageReqVO) {
        return joinCustomer2saleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JoinCustomer2saleDO> getJoinCustomer2saleList(JoinCustomer2saleExportReqVO exportReqVO) {
        return joinCustomer2saleMapper.selectList(exportReqVO);
    }

}
