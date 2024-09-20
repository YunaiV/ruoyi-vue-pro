package cn.iocoder.yudao.module.trade.dal.mysql.delivery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreStaffDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface DeliveryPickUpStoreStaffMapper extends BaseMapperX<DeliveryPickUpStoreStaffDO> {

    default Long selectStaffIdByUserIdAndStoreId(List<Long> userId, Long storeId){
        return selectOne(new LambdaQueryWrapperX<DeliveryPickUpStoreStaffDO>()
                .inIfPresent(DeliveryPickUpStoreStaffDO::getAdminUserId, userId)
                .eqIfPresent(DeliveryPickUpStoreStaffDO::getStoreId, storeId))
                .getId();
    }

    default List<DeliveryPickUpStoreStaffDO> getUserIdsByStoreId(Long id){
        return selectList(new LambdaQueryWrapperX<DeliveryPickUpStoreStaffDO>()
                .eq(DeliveryPickUpStoreStaffDO::getStoreId,id));
    }

    default void deleteStaffByUserIdsAndStoreId(List<Long> userIds, Long storeId){
        Long StaffId = selectStaffIdByUserIdAndStoreId(userIds, storeId);
        deleteById(StaffId);
    }
}




