package cn.iocoder.yudao.adminserver.modules.pay.service.merchant;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.*;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 支付商户信息 Service 接口
 *
 * @author 芋艿
 */
public interface PayMerchantService {

    /**
     * 创建支付商户信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMerchant(@Valid PayMerchantCreateReqVO createReqVO);

    /**
     * 更新支付商户信息
     *
     * @param updateReqVO 更新信息
     */
    void updateMerchant(@Valid PayMerchantUpdateReqVO updateReqVO);

    /**
     * 删除支付商户信息
     *
     * @param id 编号
     */
    void deleteMerchant(Long id);

    /**
     * 获得支付商户信息
     *
     * @param id 编号
     * @return 支付商户信息
     */
    PayMerchantDO getMerchant(Long id);

    /**
     * 获得支付商户信息列表
     *
     * @param ids 编号
     * @return 支付商户信息列表
     */
    List<PayMerchantDO> getMerchantList(Collection<Long> ids);

    /**
     * 获得支付商户信息分页
     *
     * @param pageReqVO 分页查询
     * @return 支付商户信息分页
     */
    PageResult<PayMerchantDO> getMerchantPage(PayMerchantPageReqVO pageReqVO);

    /**
     * 获得支付商户信息列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付商户信息列表
     */
    List<PayMerchantDO> getMerchantList(PayMerchantExportReqVO exportReqVO);

    /**
     * 修改商户状态
     * @param id 商户编号
     * @param status 状态
     */
    void updateMerchantStatus(Long id, Integer status);

}
