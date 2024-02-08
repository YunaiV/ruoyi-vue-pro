package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErpCustomerServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ErpCustomerServiceImpl.class)
public class ErpCustomerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpCustomerServiceImpl customerService;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Test
    public void testCreateCustomer_success() {
        // 准备参数
        ErpCustomerSaveReqVO createReqVO = randomPojo(ErpCustomerSaveReqVO.class).setId(null);

        // 调用
        Long customerId = customerService.createCustomer(createReqVO);
        // 断言
        assertNotNull(customerId);
        // 校验记录的属性是否正确
        ErpCustomerDO customer = customerMapper.selectById(customerId);
        assertPojoEquals(createReqVO, customer, "id");
    }

    @Test
    public void testUpdateCustomer_success() {
        // mock 数据
        ErpCustomerDO dbCustomer = randomPojo(ErpCustomerDO.class);
        customerMapper.insert(dbCustomer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ErpCustomerSaveReqVO updateReqVO = randomPojo(ErpCustomerSaveReqVO.class, o -> {
            o.setId(dbCustomer.getId()); // 设置更新的 ID
        });

        // 调用
        customerService.updateCustomer(updateReqVO);
        // 校验是否更新正确
        ErpCustomerDO customer = customerMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, customer);
    }

    @Test
    public void testUpdateCustomer_notExists() {
        // 准备参数
        ErpCustomerSaveReqVO updateReqVO = randomPojo(ErpCustomerSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> customerService.updateCustomer(updateReqVO), CUSTOMER_NOT_EXISTS);
    }

    @Test
    public void testDeleteCustomer_success() {
        // mock 数据
        ErpCustomerDO dbCustomer = randomPojo(ErpCustomerDO.class);
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
       ErpCustomerDO dbCustomer = randomPojo(ErpCustomerDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setMobile(null);
           o.setTelephone(null);
       });
       customerMapper.insert(dbCustomer);
       // 测试 name 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setName(null)));
       // 测试 mobile 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setMobile(null)));
       // 测试 telephone 不匹配
       customerMapper.insert(cloneIgnoreId(dbCustomer, o -> o.setTelephone(null)));
       // 准备参数
       ErpCustomerPageReqVO reqVO = new ErpCustomerPageReqVO();
       reqVO.setName(null);
       reqVO.setMobile(null);
       reqVO.setTelephone(null);

       // 调用
       PageResult<ErpCustomerDO> pageResult = customerService.getCustomerPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCustomer, pageResult.getList().get(0));
    }

}