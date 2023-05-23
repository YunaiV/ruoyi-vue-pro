package cn.iocoder.yudao.module.jl.service.crm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.CustomerMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link CustomerServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(CustomerServiceImpl.class)
public class CustomerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CustomerServiceImpl customerService;

    @Resource
    private CustomerMapper customerMapper;

    @Test
    public void testCreateCustomer_success() {
        // 准备参数
        CustomerCreateReqVO reqVO = randomPojo(CustomerCreateReqVO.class);

        // 调用
        Long customerId = customerService.createCustomer(reqVO);
        // 断言
        assertNotNull(customerId);
        // 校验记录的属性是否正确
        CustomerDO customer = customerMapper.selectById(customerId);
        assertPojoEquals(reqVO, customer);
    }

    @Test
    public void testUpdateCustomer_success() {
        // mock 数据
        CustomerDO dbCustomer = randomPojo(CustomerDO.class);
        customerMapper.insert(dbCustomer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CustomerUpdateReqVO reqVO = randomPojo(CustomerUpdateReqVO.class, o -> {
            o.setId(dbCustomer.getId()); // 设置更新的 ID
        });

        // 调用
        customerService.updateCustomer(reqVO);
        // 校验是否更新正确
        CustomerDO customer = customerMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, customer);
    }

    @Test
    public void testUpdateCustomer_notExists() {
        // 准备参数
        CustomerUpdateReqVO reqVO = randomPojo(CustomerUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> customerService.updateCustomer(reqVO), CUSTOMER_NOT_EXISTS);
    }

    @Test
    public void testDeleteCustomer_success() {
        // mock 数据
        CustomerDO dbCustomer = randomPojo(CustomerDO.class);
        customerMapper.insert(dbCustomer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCustomer.getId();

        // 调用
        customerService.deleteCustomer(id);
       // 校验数据不存在了
       assertNull(customerMapper.selectById(id));
    }

    @Test
    public void testDeleteCustomer_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> customerService.deleteCustomer(id), CUSTOMER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCustomerPage() {
       // mock 数据
       CustomerDO dbCustomer = randomPojo(CustomerDO.class, o -> { // 等会查询到
           o.setCreator(null);
           o.setCreateTime(null);
           o.setName(null);
           o.setSource(null);
           o.setPhone(null);
           o.setEmail(null);
           o.setMark(null);
           o.setWechat(null);
           o.setDoctorProfessionalRank(null);
           o.setHospitalDepartment(null);
           o.setAcademicTitle(null);
           o.setAcademicCredential(null);
           o.setHospitalId(null);
           o.setUniversityId(null);
           o.setCompanyId(null);
       });
       customerMapper.insert(dbCustomer);
       // 测试 creator 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCreator(null)));
       // 测试 createTime 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCreateTime(null)));
       // 测试 name 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setName(null)));
       // 测试 source 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setSource(null)));
       // 测试 phone 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setPhone(null)));
       // 测试 email 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setEmail(null)));
       // 测试 mark 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setMark(null)));
       // 测试 wechat 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setWechat(null)));
       // 测试 doctorProfessionalRank 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setDoctorProfessionalRank(null)));
       // 测试 hospitalDepartment 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setHospitalDepartment(null)));
       // 测试 academicTitle 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setAcademicTitle(null)));
       // 测试 academicCredential 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setAcademicCredential(null)));
       // 测试 hospitalId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setHospitalId(null)));
       // 测试 universityId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setUniversityId(null)));
       // 测试 companyId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCompanyId(null)));
       // 准备参数
       CustomerPageReqVO reqVO = new CustomerPageReqVO();
       reqVO.setCreator(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setName(null);
       reqVO.setSource(null);
       reqVO.setPhone(null);
       reqVO.setEmail(null);
       reqVO.setMark(null);
       reqVO.setWechat(null);
       reqVO.setDoctorProfessionalRank(null);
       reqVO.setHospitalDepartment(null);
       reqVO.setAcademicTitle(null);
       reqVO.setAcademicCredential(null);
       reqVO.setHospitalId(null);
       reqVO.setUniversityId(null);
       reqVO.setCompanyId(null);

       // 调用
       PageResult<CustomerDO> pageResult = customerService.getCustomerPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCustomer, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCustomerList() {
       // mock 数据
       CustomerDO dbCustomer = randomPojo(CustomerDO.class, o -> { // 等会查询到
           o.setCreator(null);
           o.setCreateTime(null);
           o.setName(null);
           o.setSource(null);
           o.setPhone(null);
           o.setEmail(null);
           o.setMark(null);
           o.setWechat(null);
           o.setDoctorProfessionalRank(null);
           o.setHospitalDepartment(null);
           o.setAcademicTitle(null);
           o.setAcademicCredential(null);
           o.setHospitalId(null);
           o.setUniversityId(null);
           o.setCompanyId(null);
       });
       customerMapper.insert(dbCustomer);
       // 测试 creator 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCreator(null)));
       // 测试 createTime 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCreateTime(null)));
       // 测试 name 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setName(null)));
       // 测试 source 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setSource(null)));
       // 测试 phone 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setPhone(null)));
       // 测试 email 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setEmail(null)));
       // 测试 mark 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setMark(null)));
       // 测试 wechat 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setWechat(null)));
       // 测试 doctorProfessionalRank 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setDoctorProfessionalRank(null)));
       // 测试 hospitalDepartment 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setHospitalDepartment(null)));
       // 测试 academicTitle 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setAcademicTitle(null)));
       // 测试 academicCredential 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setAcademicCredential(null)));
       // 测试 hospitalId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setHospitalId(null)));
       // 测试 universityId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setUniversityId(null)));
       // 测试 companyId 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setCompanyId(null)));
       // 准备参数
       CustomerExportReqVO reqVO = new CustomerExportReqVO();
       reqVO.setCreator(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setName(null);
       reqVO.setSource(null);
       reqVO.setPhone(null);
       reqVO.setEmail(null);
       reqVO.setMark(null);
       reqVO.setWechat(null);
       reqVO.setDoctorProfessionalRank(null);
       reqVO.setHospitalDepartment(null);
       reqVO.setAcademicTitle(null);
       reqVO.setAcademicCredential(null);
       reqVO.setHospitalId(null);
       reqVO.setUniversityId(null);
       reqVO.setCompanyId(null);

       // 调用
       List<CustomerDO> list = customerService.getCustomerList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbCustomer, list.get(0));
    }

}
