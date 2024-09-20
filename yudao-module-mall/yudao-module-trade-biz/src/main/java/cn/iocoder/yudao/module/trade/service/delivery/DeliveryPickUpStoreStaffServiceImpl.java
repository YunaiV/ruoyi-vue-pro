package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.*;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryPickUpStoreConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreStaffDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryPickUpStoreStaffMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PICK_UP_STORE_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PICK_UP_STORE_STAFF_NOT_EXISTS;
import static java.util.stream.Collectors.toList;

/**
 * 自提门店店员 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class DeliveryPickUpStoreStaffServiceImpl implements DeliveryPickUpStoreStaffService {


    @Resource
    private DeliveryPickUpStoreStaffMapper deliveryPickUpStoreStaffMapper;

    @Resource
    private AdminUserApi adminUserApi;


    @Override
    public DeliveryPickUpBindStoreStaffIdReqsVO getDeliveryPickUpStoreStaff(Long id, String name) {
        //1 查询绑定对应关系
        List<DeliveryPickUpStoreStaffDO> storeStaffDOS = deliveryPickUpStoreStaffMapper.selectList(new LambdaQueryWrapperX<DeliveryPickUpStoreStaffDO>()
                .eq(DeliveryPickUpStoreStaffDO::getStoreId, id)
                .eq(DeliveryPickUpStoreStaffDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
        List<Long> adminUserIds = storeStaffDOS.stream().map(DeliveryPickUpStoreStaffDO::getAdminUserId).collect(toList());
        //2 查询绑定用户信息
        List<AdminUserRespDTO> storeStaffs = adminUserApi.getUserList(adminUserIds);
        return DeliveryPickUpBindStoreStaffIdReqsVO.builder().storeStaffs(storeStaffs).name(name).id(id).build();
    }

    @Override
    public void deleteDeliveryPickUpStoreStaff(Long userId, Long storeId) {
        //通过用户编号和自提门店id查询
        Long StaffId = deliveryPickUpStoreStaffMapper.selectStaffIdByUserIdAndStoreId(Collections.singletonList(userId), storeId);
        // 校验存在
        validateDeliveryPickUpStoreStaffExists(StaffId);
        // 删除
        deliveryPickUpStoreStaffMapper.deleteById(StaffId);
    }

    @Override
    public List<DeliveryPickUpStoreStaffDO> selectStaffByUserId(Long userId) {
        return deliveryPickUpStoreStaffMapper.selectList(new LambdaQueryWrapper<DeliveryPickUpStoreStaffDO>().eq(DeliveryPickUpStoreStaffDO::getAdminUserId,userId));
    }

    private void validateDeliveryPickUpStoreStaffExists(Long id) {
        if (deliveryPickUpStoreStaffMapper.selectById(id) == null) {
            throw exception(PICK_UP_STORE_STAFF_NOT_EXISTS);
        }
    }
}
