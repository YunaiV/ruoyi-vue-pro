package cn.iocoder.yudao.module.member.service.address;

import org.springframework.stereotype.Service;
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

    @Override
    public Long createAddress(AppAddressCreateReqVO createReqVO) {
        // 插入
        AddressDO address = AddressConvert.INSTANCE.convert(createReqVO);
        addressMapper.insert(address);
        // 返回
        return address.getId();
    }

    @Override
    public void updateAddress(AppAddressUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateAddressExists(updateReqVO.getId());
        // 更新
        AddressDO updateObj = AddressConvert.INSTANCE.convert(updateReqVO);
        addressMapper.updateById(updateObj);
    }

    @Override
    public void deleteAddress(Long id) {
        // 校验存在
        this.validateAddressExists(id);
        // 删除
        addressMapper.deleteById(id);
    }

    private void validateAddressExists(Long id) {
        if (addressMapper.selectById(id) == null) {
            throw exception(ADDRESS_NOT_EXISTS);
        }
    }

    @Override
    public AddressDO getAddress(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public List<AddressDO> getAddressList(Collection<Long> ids) {
        return addressMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<AddressDO> getAddressPage(AppAddressPageReqVO pageReqVO) {
        return addressMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AddressDO> getAddressList(AppAddressExportReqVO exportReqVO) {
        return addressMapper.selectList(exportReqVO);
    }

}
