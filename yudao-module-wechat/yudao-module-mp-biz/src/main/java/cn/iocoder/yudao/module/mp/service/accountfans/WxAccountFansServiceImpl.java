package cn.iocoder.yudao.module.mp.service.accountfans;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.accountfans.vo.WxAccountFansCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.accountfans.vo.WxAccountFansExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.accountfans.vo.WxAccountFansPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.accountfans.vo.WxAccountFansUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.accountfans.WxAccountFansConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfans.WxAccountFansDO;
import cn.iocoder.yudao.module.mp.dal.mysql.accountfans.WxAccountFansMapper;
import cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
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
public class WxAccountFansServiceImpl implements WxAccountFansService {

    @Resource
    private WxAccountFansMapper wxAccountFansMapper;

    @Override
    public Long createWxAccountFans(WxAccountFansCreateReqVO createReqVO) {
        // 插入
        WxAccountFansDO wxAccountFans = WxAccountFansConvert.INSTANCE.convert(createReqVO);
        wxAccountFansMapper.insert(wxAccountFans);
        // 返回
        return wxAccountFans.getId();
    }

    @Override
    public void updateWxAccountFans(WxAccountFansUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxAccountFansExists(updateReqVO.getId());
        // 更新
        WxAccountFansDO updateObj = WxAccountFansConvert.INSTANCE.convert(updateReqVO);
        wxAccountFansMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxAccountFans(Long id) {
        // 校验存在
        this.validateWxAccountFansExists(id);
        // 删除
        wxAccountFansMapper.deleteById(id);
    }

    private void validateWxAccountFansExists(Long id) {
        if (wxAccountFansMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.WX_ACCOUNT_FANS_NOT_EXISTS);
        }
    }

    @Override
    public WxAccountFansDO getWxAccountFans(Long id) {
        return wxAccountFansMapper.selectById(id);
    }

    @Override
    public List<WxAccountFansDO> getWxAccountFansList(Collection<Long> ids) {
        return wxAccountFansMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxAccountFansDO> getWxAccountFansPage(WxAccountFansPageReqVO pageReqVO) {
        return wxAccountFansMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxAccountFansDO> getWxAccountFansList(WxAccountFansExportReqVO exportReqVO) {
        return wxAccountFansMapper.selectList(exportReqVO);
    }

    @Override
    public WxAccountFansDO findBy(SFunction<WxAccountFansDO, ?> sFunction, Object val) {
        return wxAccountFansMapper.selectOne(sFunction, val);
    }
}
