package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Customer;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDto;

/**
 * 客户 Service 接口
 *
 * @author 惟象科技
 */
public interface CustomerService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomer(@Valid CustomerCreateReq createReqVO);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid CustomerDto updateReqVO);

//    void updateCustomerSalesLead(@Valid CustomerUpdateSalesLeadVO updateReqVO);
//
//    void updateCustomerFollowup(@Valid CustomerUpdateFollowupVO updateReqVO);

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
    CustomerDto getCustomer(Long id);


    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    PageResult<Customer> getCustomerPage(CustomerPageReqVO pageReqVO);

    /**
     * 获得客户列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 客户列表
     */
    List<Customer> getCustomerList(CustomerExportReqVO exportReqVO);

}
