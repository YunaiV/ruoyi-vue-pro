package cn.iocoder.yudao.module.jl.service.join;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2reportConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.join.JoinSaleslead2reportMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中的方案 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class JoinSaleslead2reportServiceImpl implements JoinSaleslead2reportService {

    @Resource
    private JoinSaleslead2reportMapper joinSaleslead2reportMapper;

    @Override
    public Long createJoinSaleslead2report(JoinSaleslead2reportCreateReqVO createReqVO) {
        // 插入
        JoinSaleslead2reportDO joinSaleslead2report = JoinSaleslead2reportConvert.INSTANCE.convert(createReqVO);
        joinSaleslead2reportMapper.insert(joinSaleslead2report);
        // 返回
        return joinSaleslead2report.getId();
    }

    @Override
    public void updateJoinSaleslead2report(JoinSaleslead2reportUpdateReqVO updateReqVO) {
        // 校验存在
        validateJoinSaleslead2reportExists(updateReqVO.getId());
        // 更新
        JoinSaleslead2reportDO updateObj = JoinSaleslead2reportConvert.INSTANCE.convert(updateReqVO);
        joinSaleslead2reportMapper.updateById(updateObj);
    }

    @Override
    public void deleteJoinSaleslead2report(Long id) {
        // 校验存在
        validateJoinSaleslead2reportExists(id);
        // 删除
        joinSaleslead2reportMapper.deleteById(id);
    }

    private void validateJoinSaleslead2reportExists(Long id) {
        if (joinSaleslead2reportMapper.selectById(id) == null) {
            throw exception(JOIN_SALESLEAD2REPORT_NOT_EXISTS);
        }
    }

    @Override
    public JoinSaleslead2reportDO getJoinSaleslead2report(Long id) {
        return joinSaleslead2reportMapper.selectById(id);
    }

    @Override
    public List<JoinSaleslead2reportDO> getReportBySalesleadId(Long id) {
        return joinSaleslead2reportMapper.selectBySalesleadId(id);
    }

    @Override
    public List<JoinSaleslead2reportDO> getJoinSaleslead2reportList(Collection<Long> ids) {
        return joinSaleslead2reportMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JoinSaleslead2reportDO> getJoinSaleslead2reportPage(JoinSaleslead2reportPageReqVO pageReqVO) {
        return joinSaleslead2reportMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JoinSaleslead2reportDO> getJoinSaleslead2reportList(JoinSaleslead2reportExportReqVO exportReqVO) {
        return joinSaleslead2reportMapper.selectList(exportReqVO);
    }

}
