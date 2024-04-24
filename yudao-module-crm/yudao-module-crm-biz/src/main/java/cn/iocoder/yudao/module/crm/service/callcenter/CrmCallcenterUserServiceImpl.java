package cn.iocoder.yudao.module.crm.service.callcenter;

import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import cn.iocoder.yudao.module.crm.dal.mysql.callcenter.CrmCallcenterUserMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;


import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_CALLCENTER_USER_NOT_EXISTS;

/**
 * 用户与呼叫中心用户绑定关系 Service 实现类
 *
 * @author inao
 */
@Service
@Validated
public class CrmCallcenterUserServiceImpl implements CrmCallcenterUserService {

    @Resource
    private CrmCallcenterUserMapper callcenterUserMapper;

    @Override
    public Long createCallcenterUser(CrmCallcenterUserSaveReqVO createReqVO) {
        // 插入
        CrmCallcenterUserDO callcenterUser = BeanUtils.toBean(createReqVO, CrmCallcenterUserDO.class);
        callcenterUserMapper.insert(callcenterUser);
        // 返回
        return callcenterUser.getId();
    }

    @Override
    public void updateCallcenterUser(CrmCallcenterUserSaveReqVO updateReqVO) {
        // 校验存在
        validateCallcenterUserExists(updateReqVO.getId());
        // 更新
        CrmCallcenterUserDO updateObj = BeanUtils.toBean(updateReqVO, CrmCallcenterUserDO.class);
        callcenterUserMapper.updateById(updateObj);
    }

    @Override
    public void deleteCallcenterUser(Long id) {
        // 校验存在
        validateCallcenterUserExists(id);
        // 删除
        callcenterUserMapper.deleteById(id);
    }

    private void validateCallcenterUserExists(Long id) {
        if (callcenterUserMapper.selectById(id) == null) {
            throw exception(CRM_CALLCENTER_USER_NOT_EXISTS);
        }
    }

    @Override
    public CrmCallcenterUserDO getCallcenterUser(Long id) {
        return callcenterUserMapper.selectById(id);
    }

    @Override
    public PageResult<CrmCallcenterUserDO> getCallcenterUserPage(CrmCallcenterUserPageReqVO pageReqVO) {
        return callcenterUserMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmCallcenterUserDO> getList() {
        return callcenterUserMapper.selectList();
    }

}