package cn.iocoder.yudao.adminserver.modules.pay.service.merchant;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
     *
     * @param id     商户编号
     * @param status 状态
     */
    void updateMerchantStatus(Long id, Integer status);

    /**
     * 根据商户名称模糊查询商户集合
     *
     * @param merchantName 商户名称
     * @return 商户集合
     */
    List<PayMerchantDO> getMerchantListByName(String merchantName);

    // TODO aquan：暂时不用提供这样的检索。商户不多的。
    /**
     * 根据商户名称模糊查询一定数量的商户集合
     * @param merchantName 商户名称
     * @return 商户集合
     */
    List<PayMerchantDO> getMerchantListByNameLimit(String merchantName);

    /**
     * 获得指定编号的商户列表
     *
     * @param merchantIds 商户编号数组
     * @return 商户列表
     */
    // TODO @aquan：和 getMerchantList 重复了
    List<PayMerchantDO> getSimpleMerchants(Collection<Long> merchantIds);

    /**
     * 获得指定编号的商户 Map
     *
     * @param merchantIds 商户编号数组
     * @return 商户 Map
     */
    default Map<Long, PayMerchantDO> getMerchantMap(Collection<Long> merchantIds) {
        // TODO @aquan：可以不用判空，交给 getMerchantList 解决
        if (CollUtil.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        List<PayMerchantDO> list = getSimpleMerchants(merchantIds);
        return CollectionUtils.convertMap(list, PayMerchantDO::getId);
    }

}
