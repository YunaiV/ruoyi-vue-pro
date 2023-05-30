package cn.iocoder.yudao.module.jl.service.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Customer;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDto;
import cn.iocoder.yudao.module.jl.mapper.CustomerMapper;
import cn.iocoder.yudao.module.jl.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.CustomerConvert;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 客户 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;

    @Resource
    private CustomerRepository customerRepository;

    @Override
    public Long createCustomer(CustomerCreateReq createReqVO) {
        // 插入
        Customer customer = customerMapper.toEntity(createReqVO);
        customerRepository.save(customer);
        // 返回
        return customer.getId();
    }

    @Override
    public void updateCustomer(CustomerDto updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());
        // 更新
        Customer customer = customerMapper.toEntity(updateReqVO);
        customerRepository.save(customer);
    }

    /**
     * @param
     */
//    @Override
//    public void updateCustomerSalesLead(CustomerUpdateSalesLeadVO updateReqVO) {
//        // 校验存在
//        validateCustomerExists(updateReqVO.getId());
//        // 更新
//        CustomerDO updateObj = CustomerConvert.INSTANCE.convert(updateReqVO);
////        customerMapper.updateById(updateObj);
//    }
//
//    /**
//     * @param updateReqVO
//     */
//    @Override
//    public void updateCustomerFollowup(CustomerUpdateFollowupVO updateReqVO) {
//        // 校验存在
//        validateCustomerExists(updateReqVO.getId());
//        // 更新
//        CustomerDO updateObj = CustomerConvert.INSTANCE.convert(updateReqVO);
////        customerMapper.updateById(updateObj);
//    }

    @Override
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);
        // 删除
        customerRepository.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
//        if (customerMapper.selectById(id) == null) {
//            throw exception(CUSTOMER_NOT_EXISTS);
//        }
    }

    @Override
    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        CustomerDto customerDto = customerMapper.toDto(customer);
        return customerDto;
    }

    @Override
    public PageResult<Customer> getCustomerPage(CustomerPageReqVO pageReqVO) {
//        return customerMapper.selectPage(pageReqVO);
        return null;
    }

    @Override
    public List<Customer> getCustomerList(CustomerExportReqVO exportReqVO) {
//        return customerMapper.selectList(exportReqVO);
        return null;
    }

}
