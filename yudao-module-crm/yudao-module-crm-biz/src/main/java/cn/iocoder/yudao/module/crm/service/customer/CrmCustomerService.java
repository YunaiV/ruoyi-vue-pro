package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.customer.bo.CrmCustomerCreateReqBO;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * 更新客户的跟进状态
     *
     * @param id        编号
     * @param dealStatus 跟进状态
     */
    void updateCustomerDealStatus(Long id, Boolean dealStatus);

    /**
     * 更新客户相关的跟进信息
     *
     * @param id 编号
     * @param contactNextTime 下次联系时间
     * @param contactLastContent 最后联系内容
     */
    void updateCustomerFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent);

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
     * 获得客户 Map
     *
     * @param ids 客户编号数组
     * @return 客户 Map
     */
    default Map<Long, CrmCustomerDO> getCustomerMap(Collection<Long> ids) {
        return convertMap(getCustomerList(ids), CrmCustomerDO::getId);
    }

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 客户分页
     */
    PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO, Long userId);

    /**
     * 获得放入公海提醒的客户分页
     *
     * @param pageVO       分页查询
     * @param userId       用户编号
     * @return 客户分页
     */
    PageResult<CrmCustomerDO> getPutPoolRemindCustomerPage(CrmCustomerPageReqVO pageVO, Long userId);

    /**
     * 获得待进入公海的客户数量
     *
     * @param userId       用户编号
     * @return 提醒数量
     */
    Long getPutPoolRemindCustomerCount(Long userId);

    /**
     * 获得今日需联系客户数量
     *
     * @param userId 用户编号
     * @return 提醒数量
     */
    Long getTodayContactCustomerCount(Long userId);

    /**
     * 获得分配给我的客户数量
     *
     * @param userId 用户编号
     * @return 提醒数量
     */
    Long getFollowCustomerCount(Long userId);

    /**
     * 校验客户是否存在
     *
     * @param id 编号
     */
    void validateCustomer(Long id);

    /**
     * 客户转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId);

    /**
     * 客户批量转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferCustomerBatch(CrmCustomerTransferListReqVO reqVO, Long userId);

    /**
     * 锁定/解锁客户
     *
     * @param lockReqVO 更新信息
     * @param userId    用户编号
     */
    void lockCustomer(@Valid CrmCustomerLockReqVO lockReqVO, Long userId);

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
     * @param isReceive   是/否领取；true - 领取；false - 分配
     */
    void receiveCustomer(List<Long> ids, Long ownerUserId, Boolean isReceive);

    /**
     * 【系统】客户自动掉入公海
     *
     * @return 掉入公海数量
     */
    int autoPutCustomerPool();

}
