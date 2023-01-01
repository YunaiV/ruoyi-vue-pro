package cn.iocoder.yudao.module.member.service.address;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.module.member.dal.mysql.address.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.ADDRESS_NOT_EXISTS;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, AppAddressCreateReqVO createReqVO) {
        // 如果添加的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(createReqVO.getDefaulted())) {
            List<AddressDO> addresses = addressMapper.selectListByUserIdAndDefaulted(userId, true);
            addresses.forEach(address -> addressMapper.updateById(new AddressDO().setId(address.getId()).setDefaulted(false)));
        }

        // 插入
        AddressDO address = AddressConvert.INSTANCE.convert(createReqVO);
        address.setUserId(userId);
        addressMapper.insert(address);
        // 返回
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, AppAddressUpdateReqVO updateReqVO) {
        // 校验存在,校验是否能够操作
        validAddressExists(userId, updateReqVO.getId());

        // 如果修改的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(updateReqVO.getDefaulted())) {
            List<AddressDO> addresses = addressMapper.selectListByUserIdAndDefaulted(userId, true);
            addresses.stream().filter(u -> !u.getId().equals(updateReqVO.getId())) // 排除自己
                    .forEach(address -> addressMapper.updateById(new AddressDO().setId(address.getId()).setDefaulted(false)));
        }

        // 更新
        AddressDO updateObj = AddressConvert.INSTANCE.convert(updateReqVO);
        addressMapper.updateById(updateObj);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        // 校验存在,校验是否能够操作
        validAddressExists(userId, id);
        // 删除
        addressMapper.deleteById(id);
    }

    private void validAddressExists(Long userId, Long id) {
        AddressDO addressDO = getAddress(userId, id);
        if (addressDO == null) {
            throw exception(ADDRESS_NOT_EXISTS);
        }
    }

    @Override
    public AddressDO getAddress(Long userId, Long id) {
        return addressMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    public List<AddressDO> getAddressList(Long userId) {
        return addressMapper.selectListByUserIdAndDefaulted(userId, null);
    }

    @Override
    public AddressDO getDefaultUserAddress(Long userId) {
        List<AddressDO> addresses = addressMapper.selectListByUserIdAndDefaulted(userId, true);
        return CollUtil.getFirst(addresses);
    }

}
