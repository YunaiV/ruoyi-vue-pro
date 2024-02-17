package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import cn.iocoder.yudao.module.crm.service.customer.bo.CrmCustomerCreateReqBO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import jakarta.validation.Valid;

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
    Long createCustomer(@Valid CrmCustomerSaveReqVO createReqVO, Long userId);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid CrmCustomerSaveReqVO updateReqVO);

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
     */
    void validateCustomer(Long customerId);

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
     * @param lockReqVO 更新信息
     * @param userId    用户编号
     */
    void lockCustomer(@Valid CrmCustomerLockReqVO lockReqVO, Long userId);

    /**
     * 更新客户相关更进信息
     *
     * @param customerUpdateFollowUpReqBO 请求
     */
    void updateCustomerFollowUp(CrmUpdateFollowUpReqBO customerUpdateFollowUpReqBO);

    /**
     * 创建客户
     *
     * @param customerCreateReq 请求信息
     * @param userId            用户编号
     * @return 客户列表
     */
    Long createCustomer(CrmCustomerCreateReqBO customerCreateReq, Long userId);

    /**
     * 批量导入客户
     *
     * @param importCustomers 导入客户列表
     * @param importReqVO     请求
     * @return 导入结果
     */
    CrmCustomerImportRespVO importCustomerList(List<CrmCustomerImportExcelVO> importCustomers, CrmCustomerImportReqVO importReqVO);

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
     * @param isReceive   是/否领取
     */
    void receiveCustomer(List<Long> ids, Long ownerUserId, Boolean isReceive);

    /**
     * 【系统】客户自动掉入公海
     *
     * @return 掉入公海数量
     */
    int autoPutCustomerPool();

    PageResult<CrmCustomerDO> getPutInPoolRemindCustomerPage(CrmCustomerPageReqVO pageVO,
                                                             CrmCustomerPoolConfigDO poolConfigDO,
                                                             Long loginUserId);
}
