package cn.iocoder.yudao.module.member.service.address;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;
import cn.iocoder.yudao.module.member.dal.mysql.address.MemberAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
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
    private MemberAddressMapper memberAddressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, AppAddressCreateReqVO createReqVO) {
        // 如果添加的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(createReqVO.getDefaultStatus())) {
            List<MemberAddressDO> addresses = memberAddressMapper.selectListByUserIdAndDefaulted(userId, true);
            addresses.forEach(address -> memberAddressMapper.updateById(new MemberAddressDO().setId(address.getId()).setDefaultStatus(false)));
        }

        // 插入
        MemberAddressDO address = AddressConvert.INSTANCE.convert(createReqVO);
        address.setUserId(userId);
        memberAddressMapper.insert(address);
        // 返回
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, AppAddressUpdateReqVO updateReqVO) {
        // 校验存在,校验是否能够操作
        validAddressExists(userId, updateReqVO.getId());

        // 如果修改的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(updateReqVO.getDefaultStatus())) {
            List<MemberAddressDO> addresses = memberAddressMapper.selectListByUserIdAndDefaulted(userId, true);
            addresses.stream().filter(u -> !u.getId().equals(updateReqVO.getId())) // 排除自己
                    .forEach(address -> memberAddressMapper.updateById(new MemberAddressDO().setId(address.getId()).setDefaultStatus(false)));
        }

        // 更新
        MemberAddressDO updateObj = AddressConvert.INSTANCE.convert(updateReqVO);
        memberAddressMapper.updateById(updateObj);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        // 校验存在,校验是否能够操作
        validAddressExists(userId, id);
        // 删除
        memberAddressMapper.deleteById(id);
    }

    private void validAddressExists(Long userId, Long id) {
        MemberAddressDO addressDO = getAddress(userId, id);
        if (addressDO == null) {
            throw exception(ADDRESS_NOT_EXISTS);
        }
    }

    @Override
    public MemberAddressDO getAddress(Long userId, Long id) {
        return memberAddressMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    public List<MemberAddressDO> getAddressList(Long userId) {
        return memberAddressMapper.selectListByUserIdAndDefaulted(userId, null);
    }

    @Override
    public MemberAddressDO getDefaultUserAddress(Long userId) {
        List<MemberAddressDO> addresses = memberAddressMapper.selectListByUserIdAndDefaulted(userId, true);
        return CollUtil.getFirst(addresses);
    }

}
