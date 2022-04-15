package cn.iocoder.yudao.module.member.service.address;

import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.member.enums.AddressTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.member.controller.app.address.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.dal.mysql.address.AddressMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 用户收件地址 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AddressServiceImpl implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Override // TODO @shuaidawang：事务要加下哈
    public Long createAddress(Long userId, AppAddressCreateReqVO createReqVO) {
        // 如果添加的是默认收件地址，则将原默认地址修改为非默认
        if (AddressTypeEnum.DEFAULT.getType().equals(createReqVO.getType())) {
            // TODO @shuaidawang：查询到一个，然后进行 update
            List<AddressDO> addressDOs = selectListByUserIdAndType(userId, AddressTypeEnum.DEFAULT.getType());
            if (!CollectionUtils.isEmpty(addressDOs)) {
                addressDOs.forEach(userAddressDO -> addressMapper.updateById(new AddressDO()
                        .setId(userAddressDO.getId()).setType(AddressTypeEnum.NORMAL.getType())));
            }
        }
        // 插入
        AddressDO address = AddressConvert.INSTANCE.convert(createReqVO);
        address.setUserId(userId);
        addressMapper.insert(address);
        // 返回
        return address.getId();
    }

    @Override // TODO @shuaidawang：事务要加下哈
    public void updateAddress(Long userId, AppAddressUpdateReqVO updateReqVO) {
        // 校验存在,校验是否能够操作 TODO shuaidawang：改成基于 id + userId 查询，以前的做法不太好；
        check(userId, updateReqVO.getId());
        // 如果修改的是默认收件地址，则将原默认地址修改为非默认
        if (AddressTypeEnum.DEFAULT.getType().equals(updateReqVO.getType())) {
            // TODO @shuaidawang：查询到一个，然后进行 update，需要排除自己哈
            List<AddressDO> addressDOs = selectListByUserIdAndType(
                    userId, AddressTypeEnum.DEFAULT.getType());
            if (!CollectionUtils.isEmpty(addressDOs)) {
                addressDOs.stream().filter(userAddressDO -> !userAddressDO.getId().equals(updateReqVO.getId())) // 过滤掉更新的收件地址
                        .forEach(userAddressDO -> addressMapper.updateById(new AddressDO()
                                .setId(userAddressDO.getId()).setType(AddressTypeEnum.NORMAL.getType())));
            }
        }
        // 更新
        AddressDO updateObj = AddressConvert.INSTANCE.convert(updateReqVO);
        updateObj.setUserId(userId); // TODO @shuaidawang：不用加 userId
        addressMapper.updateById(updateObj);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        // 校验存在,校验是否能够操作 TODO shuaidawang：改成基于 id + userId 查询，以前的做法不太好；
        check(userId, id);
        // 删除
        addressMapper.deleteById(id);
    }

    /**
     * 校验用户收件地址是不是属于该用户
     *
     * @param userId 用户编号
     * @param userAddressId 用户收件地址
     */
    private void check(Long userId, Long userAddressId) {
        AddressDO addressDO = getAddress(userAddressId);
        if(null == addressDO){
            throw exception(ADDRESS_NOT_EXISTS);
        }
        if (!addressDO.getUserId().equals(userId)) {
            throw exception(ADDRESS_FORBIDDEN);
        }
    }

    @Override
    public AddressDO getAddress(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public List<AddressDO> getAddressList(Long userId) {
        return selectListByUserIdAndType(userId, null);
    }

    @Override
    public AddressDO getAddress(Long userId, Long id) {
        AddressDO address = getAddress(id); // TODO shuaidawang：改成基于 id + userId 查询，以前的做法不太好；
        check(userId, id);
        return address;
    }

    /**
     * 获取默认地址
     * @param userId
     * @return
     */
    @Override
    public AddressDO getDefaultUserAddress(Long userId) {
        // TODO @shuaidawang：查询，都抽到 mapper 中
        List<AddressDO> addressDOList = selectListByUserIdAndType(userId, AddressTypeEnum.DEFAULT.getType());
        return addressDOList.stream().findFirst().orElse(null);
    }

    // TODO @shuaidawang：查询，都抽到 mapper 中
    /**
     * 根据类型获取地址列表
     * @param userId
     * @param type null则查询全部
     * @return
     */
    public List<AddressDO> selectListByUserIdAndType(Long userId, Integer type) {
        QueryWrapperX<AddressDO> queryWrapperX = new QueryWrapperX<AddressDO>().eq("user_id", userId)
                .eqIfPresent("type", type);
        return addressMapper.selectList(queryWrapperX);
    }


}
