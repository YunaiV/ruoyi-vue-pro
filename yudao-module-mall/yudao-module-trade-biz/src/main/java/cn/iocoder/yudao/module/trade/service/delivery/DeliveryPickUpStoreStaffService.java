package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreStaffDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreStaffMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 自提门店店员 Service 接口
 *
 * @author jason
 */
public interface DeliveryPickUpStoreStaffService {

    /**
     * 查询门店绑定用户信息数组
     *
     * @param id 门店编号
     * @return 用户信息数组
     */
    DeliveryPickUpBindStoreStaffIdReqsVO getDeliveryPickUpStoreStaff(Long id, String name);

    /**
     * 删除自提门店店员
     *
     * @param id 门店店员编号
     */
    void deleteDeliveryPickUpStoreStaff(Long id, Long storeId);

    /**
     * 根据用户id查询自提门店店员信息
     * @param userId
     * @return
     */
    List<DeliveryPickUpStoreStaffDO> selectStaffByUserId(Long userId);
}
