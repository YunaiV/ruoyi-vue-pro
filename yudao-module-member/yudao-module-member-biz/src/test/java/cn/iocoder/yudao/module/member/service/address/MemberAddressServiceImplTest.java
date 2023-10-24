package cn.iocoder.yudao.module.member.service.address;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;
import cn.iocoder.yudao.module.member.dal.mysql.address.MemberAddressMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.ADDRESS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link AddressServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(AddressServiceImpl.class)
public class MemberAddressServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AddressServiceImpl addressService;

    @Resource
    private MemberAddressMapper addressMapper;

    @Test
    public void testCreateAddress_success() {
        // 准备参数
        AppAddressCreateReqVO reqVO = randomPojo(AppAddressCreateReqVO.class);

        // 调用
        Long addressId = addressService.createAddress(randomLongId(), reqVO);
        // 断言
        assertNotNull(addressId);
        // 校验记录的属性是否正确
        MemberAddressDO address = addressMapper.selectById(addressId);
        assertPojoEquals(reqVO, address);
    }

    @Test
    public void testUpdateAddress_success() {
        // mock 数据
        MemberAddressDO dbAddress = randomPojo(MemberAddressDO.class);
        addressMapper.insert(dbAddress);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AppAddressUpdateReqVO reqVO = randomPojo(AppAddressUpdateReqVO.class, o -> {
            o.setId(dbAddress.getId()); // 设置更新的 ID
        });

        // 调用
        addressService.updateAddress(dbAddress.getUserId(), reqVO);
        // 校验是否更新正确
        MemberAddressDO address = addressMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, address);
    }

    @Test
    public void testUpdateAddress_notExists() {
        // 准备参数
        AppAddressUpdateReqVO reqVO = randomPojo(AppAddressUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> addressService.updateAddress(randomLongId(), reqVO), ADDRESS_NOT_EXISTS);
    }

    @Test
    public void testDeleteAddress_success() {
        // mock 数据
        MemberAddressDO dbAddress = randomPojo(MemberAddressDO.class);
        addressMapper.insert(dbAddress);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbAddress.getId();

        // 调用
        addressService.deleteAddress(dbAddress.getUserId(), id);
        // 校验数据不存在了
        assertNull(addressMapper.selectById(id));
    }

    @Test
    public void testDeleteAddress_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> addressService.deleteAddress(randomLongId(), id), ADDRESS_NOT_EXISTS);
    }

}
