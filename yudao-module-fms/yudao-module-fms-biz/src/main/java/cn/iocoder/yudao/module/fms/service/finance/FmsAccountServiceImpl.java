package cn.iocoder.yudao.module.fms.service.finance;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.account.FmsAccountPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.account.FmsAccountSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsAccountDO;
import cn.iocoder.yudao.module.fms.dal.mysql.finance.FmsAccountMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.ACCOUNT_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.ACCOUNT_NOT_EXISTS;

/**
 * ERP 结算账户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAccountServiceImpl implements FmsAccountService {

    @Resource
    private FmsAccountMapper accountMapper;

    @Override
    public Long createAccount(FmsAccountSaveReqVO createReqVO) {
        // 插入
        FmsAccountDO account = BeanUtils.toBean(createReqVO, FmsAccountDO.class);
        accountMapper.insert(account);
        // 返回
        return account.getId();
    }

    @Override
    public void updateAccount(FmsAccountSaveReqVO updateReqVO) {
        // 校验存在
        validateAccountExists(updateReqVO.getId());
        // 更新
        FmsAccountDO updateObj = BeanUtils.toBean(updateReqVO, FmsAccountDO.class);
        accountMapper.updateById(updateObj);
    }

    @Override
    public void updateAccountDefaultStatus(Long id, Boolean defaultStatus) {
        // 1. 校验存在
        validateAccountExists(id);

        // 2.1 如果开启，则需要关闭所有其它的默认
        if (defaultStatus) {
            FmsAccountDO account = accountMapper.selectByDefaultStatus();
            if (account != null) {
                accountMapper.updateById(new FmsAccountDO().setId(account.getId()).setDefaultStatus(false));
            }
        }
        // 2.2 更新对应的默认状态
        accountMapper.updateById(new FmsAccountDO().setId(id).setDefaultStatus(defaultStatus));
    }

    @Override
    public void deleteAccount(Long id) {
        // 校验存在
        validateAccountExists(id);
        // 删除
        accountMapper.deleteById(id);
    }

    private void validateAccountExists(Long id) {
        if (accountMapper.selectById(id) == null) {
            throw exception(ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public FmsAccountDO getAccount(Long id) {
        return accountMapper.selectById(id);
    }

    @Override
    public FmsAccountDO validateAccount(Long id) {
        FmsAccountDO account = accountMapper.selectById(id);
        if (account == null) {
            throw exception(ACCOUNT_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(account.getStatus())) {
            throw exception(ACCOUNT_NOT_ENABLE, account.getName());
        }
        return account;
    }

    @Override
    public List<FmsAccountDO> getAccountListByStatus(Integer status) {
        return accountMapper.selectListByStatus(status);
    }

    @Override
    public List<FmsAccountDO> getAccountList(Collection<Long> ids) {
        return accountMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<FmsAccountDO> getAccountPage(FmsAccountPageReqVO pageReqVO) {
        return accountMapper.selectPage(pageReqVO);
    }

}