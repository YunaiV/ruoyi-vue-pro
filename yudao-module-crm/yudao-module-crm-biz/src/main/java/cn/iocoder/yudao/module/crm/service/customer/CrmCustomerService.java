package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 客户 Service 接口
 *
 * @author Wanwan
 */
public interface CrmCustomerService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createCustomer(@Valid CrmCustomerCreateReqVO createReqVO, Long userId);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid CrmCustomerUpdateReqVO updateReqVO);

    /**
     * 删除客户
     *
     * @param id 编号
     */
    void deleteCustomer(Long id);

    /**
     * 获得客户
     *
     * @param id 编号
     * @return 客户
     */
    CrmCustomerDO getCustomer(Long id);

    /**
     * 获得客户列表
     *
     * @param ids 客户编号数组
     * @return 客户列表
     * @author ljlleo
     */
    List<CrmCustomerDO> getCustomerList(Collection<Long> ids);

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 客户分页
     */
    PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO, Long userId);

    /**
     * 校验客户是否存在
     *
     * @param customerId 客户 id
     * @return 客户
     */
    CrmCustomerDO validateCustomer(Long customerId);

    /**
     * 客户转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId);

    /**
     * 锁定/解锁客户
     *
     * @param updateReqVO 更新信息
     */
    void lockCustomer(@Valid CrmCustomerUpdateReqVO updateReqVO);

    // ==================== 公海相关操作 ====================

    /**
     * 客户放入公海
     *
     * @param id 客户编号
     */
    void putCustomerPool(Long id);

    /**
     * 领取公海客户
     *
     * @param ids         要领取的客户编号数组
     * @param ownerUserId 负责人
     */
    void receiveCustomer(List<Long> ids, Long ownerUserId);

    /**
     * 获取客户列表
     *
     * @return 客户列表
     * @author zyna
     */
    List<CrmCustomerDO> getCustomerList();

}
