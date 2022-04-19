package cn.iocoder.yudao.module.member.service.address;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.module.member.dal.mysql.address.AddressMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.ADDRESS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link AddressServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(AddressServiceImpl.class)
public class AddressServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AddressServiceImpl addressService;

    @Resource
    private AddressMapper addressMapper;

    @Test
    public void testCreateAddress_success() {
        // 准备参数
        AppAddressCreateReqVO reqVO = randomPojo(AppAddressCreateReqVO.class);

        // 调用
        Long addressId = addressService.createAddress(getLoginUserId(), reqVO);
        // 断言
        assertNotNull(addressId);
        // 校验记录的属性是否正确
        AddressDO address = addressMapper.selectById(addressId);
        assertPojoEquals(reqVO, address);
    }

    @Test
    public void testUpdateAddress_success() {
        // mock 数据
        AddressDO dbAddress = randomPojo(AddressDO.class);
        addressMapper.insert(dbAddress);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AppAddressUpdateReqVO reqVO = randomPojo(AppAddressUpdateReqVO.class, o -> {
            o.setId(dbAddress.getId()); // 设置更新的 ID
        });

        // 调用
        addressService.updateAddress(getLoginUserId(), reqVO);
        // 校验是否更新正确
        AddressDO address = addressMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, address);
    }

    @Test
    public void testUpdateAddress_notExists() {
        // 准备参数
        AppAddressUpdateReqVO reqVO = randomPojo(AppAddressUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> addressService.updateAddress(getLoginUserId(), reqVO), ADDRESS_NOT_EXISTS);
    }

    @Test
    public void testDeleteAddress_success() {
        // mock 数据
        AddressDO dbAddress = randomPojo(AddressDO.class);
        addressMapper.insert(dbAddress);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbAddress.getId();

        // 调用
        addressService.deleteAddress(getLoginUserId(), id);
       // 校验数据不存在了
       assertNull(addressMapper.selectById(id));
    }

    @Test
    public void testDeleteAddress_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> addressService.deleteAddress(getLoginUserId(), id), ADDRESS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void ins() {
       // mock 数据
       AddressDO dbAddress = randomPojo(AddressDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setName(null);
           o.setMobile(null);
           o.setAreaCode(null);
           o.setDetailAddress(null);
           o.setType(null);
           o.setCreateTime(null);
       });
       addressMapper.insert(dbAddress);
       // 测试 userId 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setUserId(null)));
       // 测试 name 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setName(null)));
       // 测试 mobile 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setMobile(null)));
       // 测试 areaCode 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setAreaCode(null)));
       // 测试 detailAddress 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setDetailAddress(null)));
       // 测试 type 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setType(null)));
       // 测试 createTime 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setCreateTime(null)));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAddressList() {
       // mock 数据
       AddressDO dbAddress = randomPojo(AddressDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setName(null);
           o.setMobile(null);
           o.setAreaCode(null);
           o.setDetailAddress(null);
           o.setType(null);
           o.setCreateTime(null);
       });
       addressMapper.insert(dbAddress);
       // 测试 userId 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setUserId(null)));
       // 测试 name 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setName(null)));
       // 测试 mobile 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setMobile(null)));
       // 测试 areaCode 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setAreaCode(null)));
       // 测试 detailAddress 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setDetailAddress(null)));
       // 测试 type 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setType(null)));
       // 测试 createTime 不匹配
       addressMapper.insert(cloneIgnoreId(dbAddress, o -> o.setCreateTime(null)));
    }

}
