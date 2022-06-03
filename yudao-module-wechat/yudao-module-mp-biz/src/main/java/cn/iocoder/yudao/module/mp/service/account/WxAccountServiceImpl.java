package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.account.WxAccountConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.mp.dal.mysql.account.WxAccountMapper;
import cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.mp.mq.producer.dict.WxMpConfigDataProducer;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;


/**
 * 公众号账户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxAccountServiceImpl implements WxAccountService {

    @Resource
    private WxAccountMapper wxAccountMapper;

    @Resource
    private WxMpConfigDataProducer wxMpConfigDataProducer;

    @Override
    public Long createWxAccount(WxAccountCreateReqVO createReqVO) {
        // 插入
        WxAccountDO wxAccount = WxAccountConvert.INSTANCE.convert(createReqVO);
        wxAccountMapper.insert(wxAccount);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
        // 返回
        return wxAccount.getId();
    }

    @Override
    public void updateWxAccount(WxAccountUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxAccountExists(updateReqVO.getId());
        // 更新
        WxAccountDO updateObj = WxAccountConvert.INSTANCE.convert(updateReqVO);
        wxAccountMapper.updateById(updateObj);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
    }

    @Override
    public void deleteWxAccount(Long id) {
        // 校验存在
        this.validateWxAccountExists(id);
        // 删除
        wxAccountMapper.deleteById(id);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
    }

    private void validateWxAccountExists(Long id) {
        if (wxAccountMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.WX_ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public WxAccountDO getWxAccount(Long id) {
        return wxAccountMapper.selectById(id);
    }

    @Override
    public List<WxAccountDO> getWxAccountList(Collection<Long> ids) {
        return wxAccountMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxAccountDO> getWxAccountPage(WxAccountPageReqVO pageReqVO) {
        return wxAccountMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxAccountDO> getWxAccountList(WxAccountExportReqVO exportReqVO) {
        return wxAccountMapper.selectList(exportReqVO);
    }

    @Override
    public WxAccountDO findBy(SFunction<WxAccountDO, ?> field, Object val) {
        return wxAccountMapper.selectOne(field, val);
    }
}
