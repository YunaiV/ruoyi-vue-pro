package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.account.ErpAccountPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.account.ErpAccountSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 结算账户 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpAccountService {

    /**
     * 创建结算账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAccount(@Valid ErpAccountSaveReqVO createReqVO);

    /**
     * 更新ERP 结算账户
     *
     * @param updateReqVO 更新信息
     */
    void updateAccount(@Valid ErpAccountSaveReqVO updateReqVO);

    /**
     * 更新结算账户默认状态
     *
     * @param id 编号
     * @param defaultStatus 默认状态
     */
    void updateAccountDefaultStatus(Long id, Boolean defaultStatus);

    /**
     * 删除结算账户
     *
     * @param id 编号
     */
    void deleteAccount(Long id);

    /**
     * 获得结算账户
     *
     * @param id 编号
     * @return 结算账户
     */
    ErpAccountDO getAccount(Long id);

    /**
     * 校验结算账户
     *
     * @param id 编号
     * @return 结算账户
     */
    ErpAccountDO validateAccount(Long id);

    /**
     * 获得指定状态的结算账户列表
     *
     * @param status 状态
     * @return 结算账户
     */
    List<ErpAccountDO> getAccountListByStatus(Integer status);

    /**
     * 获得结算账户列表
     *
     * @param ids 编号数组
     * @return 结算账户列表
     */
    List<ErpAccountDO> getAccountList(Collection<Long> ids);

    /**
     * 获得结算账户 Map
     *
     * @param ids 编号数组
     * @return 结算账户 Map
     */
    default Map<Long, ErpAccountDO> getAccountMap(Collection<Long> ids) {
        return convertMap(getAccountList(ids), ErpAccountDO::getId);
    }

    /**
     * 获得结算账户分页
     *
     * @param pageReqVO 分页查询
     * @return 结算账户分页
     */
    PageResult<ErpAccountDO> getAccountPage(ErpAccountPageReqVO pageReqVO);

}