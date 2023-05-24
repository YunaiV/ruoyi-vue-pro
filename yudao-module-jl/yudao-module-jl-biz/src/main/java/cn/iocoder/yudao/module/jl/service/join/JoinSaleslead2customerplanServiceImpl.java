package cn.iocoder.yudao.module.jl.service.join;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2customerplanConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2customerplanMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中的客户方案 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class JoinSaleslead2customerplanServiceImpl implements JoinSaleslead2customerplanService {

    @Resource
    private JoinSaleslead2customerplanMapper joinSaleslead2customerplanMapper;

    @Override
    public Long createJoinSaleslead2customerplan(JoinSaleslead2customerplanCreateReqVO createReqVO) {
        // 插入
        JoinSaleslead2customerplanDO joinSaleslead2customerplan = JoinSaleslead2customerplanConvert.INSTANCE.convert(createReqVO);
        joinSaleslead2customerplanMapper.insert(joinSaleslead2customerplan);
        // 返回
        return joinSaleslead2customerplan.getId();
    }

    /**
     * 根据销售id，获得销售线索中的方案
     *
     * @param id 方案id
     * @return 销售线索中的方案
     */
    @Override
    public List<JoinSaleslead2customerplanDO> getCustomerPlanBySalesleadId(Long id) {
        return joinSaleslead2customerplanMapper.selectBySalesleadId(id);
    }

    @Override
    public void updateJoinSaleslead2customerplan(JoinSaleslead2customerplanUpdateReqVO updateReqVO) {
        // 校验存在
        validateJoinSaleslead2customerplanExists(updateReqVO.getId());
        // 更新
        JoinSaleslead2customerplanDO updateObj = JoinSaleslead2customerplanConvert.INSTANCE.convert(updateReqVO);
        joinSaleslead2customerplanMapper.updateById(updateObj);
    }

    @Override
    public void deleteJoinSaleslead2customerplan(Long id) {
        // 校验存在
        validateJoinSaleslead2customerplanExists(id);
        // 删除
        joinSaleslead2customerplanMapper.deleteById(id);
    }

    private void validateJoinSaleslead2customerplanExists(Long id) {
        if (joinSaleslead2customerplanMapper.selectById(id) == null) {
            throw exception(JOIN_SALESLEAD2CUSTOMERPLAN_NOT_EXISTS);
        }
    }

    @Override
    public JoinSaleslead2customerplanDO getJoinSaleslead2customerplan(Long id) {
        return joinSaleslead2customerplanMapper.selectById(id);
    }

    @Override
    public List<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanList(Collection<Long> ids) {
        return joinSaleslead2customerplanMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanPage(JoinSaleslead2customerplanPageReqVO pageReqVO) {
        return joinSaleslead2customerplanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanList(JoinSaleslead2customerplanExportReqVO exportReqVO) {
        return joinSaleslead2customerplanMapper.selectList(exportReqVO);
    }

}
