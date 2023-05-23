package cn.iocoder.yudao.module.jl.service.crm;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.FollowupConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.FollowupMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class FollowupServiceImpl implements FollowupService {

    @Resource
    private FollowupMapper followupMapper;

    @Override
    public Long createFollowup(FollowupCreateReqVO createReqVO) {
        // 插入
        FollowupDO followup = FollowupConvert.INSTANCE.convert(createReqVO);
        followupMapper.insert(followup);
        // 返回
        return followup.getId();
    }

    @Override
    public void updateFollowup(FollowupUpdateReqVO updateReqVO) {
        // 校验存在
        validateFollowupExists(updateReqVO.getId());
        // 更新
        FollowupDO updateObj = FollowupConvert.INSTANCE.convert(updateReqVO);
        followupMapper.updateById(updateObj);
    }

    @Override
    public void deleteFollowup(Long id) {
        // 校验存在
        validateFollowupExists(id);
        // 删除
        followupMapper.deleteById(id);
    }

    private void validateFollowupExists(Long id) {
        if (followupMapper.selectById(id) == null) {
            throw exception(FOLLOWUP_NOT_EXISTS);
        }
    }

    @Override
    public FollowupDO getFollowup(Long id) {
        return followupMapper.selectById(id);
    }

    @Override
    public List<FollowupDO> getFollowupList(Collection<Long> ids) {
        return followupMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<FollowupDO> getFollowupPage(FollowupPageReqVO pageReqVO) {
        return followupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<FollowupDO> getFollowupList(FollowupExportReqVO exportReqVO) {
        return followupMapper.selectList(exportReqVO);
    }

}
