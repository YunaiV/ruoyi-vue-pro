package cn.iocoder.yudao.module.member.service.address;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.member.controller.app.address.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户收件地址 Service 接口
 *
 * @author 芋道源码
 */
public interface AddressService {

    /**
     * 创建用户收件地址
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAddress(@Valid AppAddressCreateReqVO createReqVO);

    /**
     * 更新用户收件地址
     *
     * @param updateReqVO 更新信息
     */
    void updateAddress(@Valid AppAddressUpdateReqVO updateReqVO);

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
    AddressDO getAddress(Long id);

    /**
     * 获得用户收件地址列表
     *
     * @param ids 编号
     * @return 用户收件地址列表
     */
    List<AddressDO> getAddressList(Collection<Long> ids);

    /**
     * 获得用户收件地址分页
     *
     * @param pageReqVO 分页查询
     * @return 用户收件地址分页
     */
    PageResult<AddressDO> getAddressPage(AppAddressPageReqVO pageReqVO);

    /**
     * 获得用户收件地址列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户收件地址列表
     */
    List<AddressDO> getAddressList(AppAddressExportReqVO exportReqVO);

}
