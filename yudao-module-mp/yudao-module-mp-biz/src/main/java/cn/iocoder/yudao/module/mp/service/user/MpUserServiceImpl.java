package cn.iocoder.yudao.module.mp.service.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.MpUserPageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.dal.mysql.accountfans.MpUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 微信公众号粉丝 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MpUserServiceImpl implements MpUserService {

    @Resource
    private MpUserMapper mpUserMapper;

    @Override
    public MpUserDO getUser(Long id) {
        return mpUserMapper.selectById(id);
    }

    @Override
    public List<MpUserDO> getUserList(Collection<Long> ids) {
        return mpUserMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MpUserDO> getUserPage(MpUserPageReqVO pageReqVO) {
        return mpUserMapper.selectPage(pageReqVO);
    }

}
