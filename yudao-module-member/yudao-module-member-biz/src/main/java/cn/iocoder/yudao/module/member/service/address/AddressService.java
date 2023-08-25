package cn.iocoder.yudao.module.member.service.address;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户收件地址 Service 接口
 *
 * @author 芋道源码
 */
public interface AddressService {

    /**
     * 创建用户收件地址
     *
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAddress(Long userId, @Valid AppAddressCreateReqVO createReqVO);

    /**
     * 更新用户收件地址
     *
     * @param userId 用户编号
     * @param updateReqVO 更新信息
     */
    void updateAddress(Long userId, @Valid AppAddressUpdateReqVO updateReqVO);

    /**
     * 删除用户收件地址
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteAddress(Long userId, Long id);

    /**
     * 获得用户收件地址
     *
     * @param id 编号
     * @return 用户收件地址
     */
    MemberAddressDO getAddress(Long userId, Long id);

    /**
     * 获得用户收件地址列表
     *
     * @param userId 用户编号
     * @return 用户收件地址列表
     */
    List<MemberAddressDO> getAddressList(Long userId);

    /**
     * 获得用户默认的收件地址
     *
     * @param userId 用户编号
     * @return 用户收件地址
     */
    MemberAddressDO getDefaultUserAddress(Long userId);

    /**
     * 创建用户收件地址
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAddress(@Valid AddressCreateReqVO createReqVO);

    /**
     * 更新用户收件地址
     *
     * @param updateReqVO 更新信息
     */
    void updateAddress(@Valid AddressUpdateReqVO updateReqVO);

    /**
     * 删除用户收件地址
     *
     * @param id 编号
     */
    void deleteAddress(Long id);

    /**
     * 获得用户收件地址
     *
     * @param id 编号
     * @return 用户收件地址
     */
    MemberAddressDO getAddress(Long id);

    /**
     * 获得用户收件地址列表
     *
     * @param ids 编号
     * @return 用户收件地址列表
     */
    List<MemberAddressDO> getAddressList(Collection<Long> ids);

    /**
     * 获得用户收件地址分页
     *
     * @param pageReqVO 分页查询
     * @return 用户收件地址分页
     */
    PageResult<MemberAddressDO> getAddressPage(AddressPageReqVO pageReqVO);

    /**
     * 获得用户收件地址列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户收件地址列表
     */
    List<MemberAddressDO> getAddressList(AddressExportReqVO exportReqVO);
}
