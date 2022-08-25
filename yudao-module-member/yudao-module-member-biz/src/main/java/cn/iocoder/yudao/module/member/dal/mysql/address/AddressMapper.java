package cn.iocoder.yudao.module.member.dal.mysql.address;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.module.member.enums.AddressTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户收件地址 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AddressMapper extends BaseMapperX<AddressDO> {

    /**
     * 获取当前地址 根据id和userId
     * @param userId
     * @param id
     * @return
     */
    default AddressDO getAddressByIdAndUserId(Long userId, Long id) {
        QueryWrapperX<AddressDO> queryWrapperX = new QueryWrapperX<>();
        queryWrapperX.eq("user_id", userId).eq("id", id);
        return selectList(queryWrapperX).stream().findFirst().orElse(null);
    }

    /**
     * 获取地址列表
     * @param userId
     * @param type
     * @return
     */
    default List<AddressDO> selectListByUserIdAndType(Long userId, Integer type) {
        QueryWrapperX<AddressDO> queryWrapperX = new QueryWrapperX<AddressDO>().eq("user_id", userId)
                .eqIfPresent("type", type);
        return selectList(queryWrapperX);
    }

    /**
     * 获取默认地址
     * @param userId
     * @return
     */
    default AddressDO getDefaultUserAddress(Long userId) {
        List<AddressDO> addressDOList = selectListByUserIdAndType(userId, AddressTypeEnum.DEFAULT.getType());
        return addressDOList.stream().findFirst().orElse(null);
    }

    /**
     * 获取默认地址
     * @param id
     * @param addressTypeEnum
     * @return
     */
    default int updateTypeById(Long id, AddressTypeEnum addressTypeEnum) {
       return updateById(new AddressDO().setId(id).setType(addressTypeEnum.getType()));
    }

}
