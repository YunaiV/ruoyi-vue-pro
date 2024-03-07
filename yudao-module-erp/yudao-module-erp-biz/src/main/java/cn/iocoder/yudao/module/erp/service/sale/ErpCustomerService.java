package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 客户 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpCustomerService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomer(@Valid ErpCustomerSaveReqVO createReqVO);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid ErpCustomerSaveReqVO updateReqVO);

    /**
     * 删除客户
     *
     * @param id 编号
     */
    void deleteCustomer(Long id);

    /**
     * 获得客户
     *
     * @param id 编号
     * @return 客户
     */
    ErpCustomerDO getCustomer(Long id);

    /**
     * 校验客户
     *
     * @param id 编号
     * @return 客户
     */
    ErpCustomerDO validateCustomer(Long id);

    /**
     * 获得客户列表
     *
     * @param ids 编号列表
     * @return 客户列表
     */
    List<ErpCustomerDO> getCustomerList(Collection<Long> ids);

    /**
     * 获得客户 Map
     *
     * @param ids 编号列表
     * @return 客户 Map
     */
    default Map<Long, ErpCustomerDO> getCustomerMap(Collection<Long> ids) {
        return convertMap(getCustomerList(ids), ErpCustomerDO::getId);
    }

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    PageResult<ErpCustomerDO> getCustomerPage(ErpCustomerPageReqVO pageReqVO);

    /**
     * 获得指定状态的客户列表
     *
     * @param status 状态
     * @return 客户列表
     */
    List<ErpCustomerDO> getCustomerListByStatus(Integer status);

}