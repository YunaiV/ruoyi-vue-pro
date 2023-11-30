package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：单测后续补

/**
 * {@link CrmCustomerServiceImpl} 的单元测试类
 *
 * @author Wanwan
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CrmCustomerServiceImpl.class)
public class CrmCustomerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmCustomerServiceImpl customerService;

    @Resource
    private CrmCustomerMapper customerMapper;
    @MockBean
    private CrmPermissionService permissionService;

    @Test
    public void testCreateCustomer_success() {
        // 准备参数
        CrmCustomerCreateReqVO reqVO = randomPojo(CrmCustomerCreateReqVO.class);

        // 调用
        Long customerId = customerService.createCustomer(reqVO, getLoginUserId());
        // 断言
        assertNotNull(customerId);
        // 校验记录的属性是否正确
        CrmCustomerDO customer = customerMapper.selectById(customerId);
        assertPojoEquals(reqVO, customer);
    }

    @Test
    public void testUpdateCustomer_success() {
        // mock 数据
        CrmCustomerDO dbCustomer = randomPojo(CrmCustomerDO.class);
        customerMapper.insert(dbCustomer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmCustomerUpdateReqVO reqVO = randomPojo(CrmCustomerUpdateReqVO.class, o -> {
            o.setId(dbCustomer.getId()); // 设置更新的 ID
        });

        // 调用
        customerService.updateCustomer(reqVO);
        // 校验是否更新正确
        CrmCustomerDO customer = customerMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, customer);
    }

    @Test
    public void testUpdateCustomer_notExists() {
        // 准备参数
        CrmCustomerUpdateReqVO reqVO = randomPojo(CrmCustomerUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> customerService.updateCustomer(reqVO), CUSTOMER_NOT_EXISTS);
    }

    @Test
    public void testDeleteCustomer_success() {
        // mock 数据
        CrmCustomerDO dbCustomer = randomPojo(CrmCustomerDO.class);
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
    public void testGetCustomerPage() {
        // mock 数据
        CrmCustomerDO dbCustomer = randomPojo(CrmCustomerDO.class, o -> { // 等会查询到
            o.setName("张三");
            o.setMobile("13888888888");
            o.setTelephone("13888888888");
            o.setWebsite("https://yudao.com");
        });
        //customerMapper.insert(dbCustomer);
        // 测试 name 不匹配
        //customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setName("")));
        // 测试 mobile 不匹配
        //customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setMobile(null)));
        // 测试 telephone 不匹配
        //customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setTelephone(null)));
        // 测试 website 不匹配
        //customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setWebsite(null)));
        // 准备参数
        CrmCustomerPageReqVO reqVO = new CrmCustomerPageReqVO();
        reqVO.setName("张三");
        reqVO.setMobile("13888888888");
        //reqVO.setTelephone(null);
        //reqVO.setWebsite(null);

        // 调用
        PageResult<CrmCustomerDO> pageResult = customerService.getCustomerPage(reqVO, 1L);
        // 断言
        //assertEquals(1, pageResult.getTotal());
        //assertEquals(1, pageResult.getList().size());
        //assertPojoEquals(dbCustomer, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCustomerList() {
        // mock 数据
        CrmCustomerDO dbCustomer = randomPojo(CrmCustomerDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setMobile(null);
            o.setTelephone(null);
            o.setWebsite(null);
        });
        customerMapper.insert(dbCustomer);
        // 测试 name 不匹配
        customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setName(null)));
        // 测试 mobile 不匹配
        customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setMobile(null)));
        // 测试 telephone 不匹配
        customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setTelephone(null)));
        // 测试 website 不匹配
        customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setWebsite(null)));
        // 准备参数
        CrmCustomerPageReqVO reqVO = new CrmCustomerPageReqVO();
        reqVO.setName("张三");
        reqVO.setMobile("13888888888");
        reqVO.setPageSize(PAGE_SIZE_NONE);
        //reqVO.setTelephone(null);
        //reqVO.setWebsite(null);

        // 调用
        PageResult<CrmCustomerDO> pageResult = customerService.getCustomerPage(reqVO, 1L);
        // 断言
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbCustomer, pageResult.getList().get(0));
    }

}
