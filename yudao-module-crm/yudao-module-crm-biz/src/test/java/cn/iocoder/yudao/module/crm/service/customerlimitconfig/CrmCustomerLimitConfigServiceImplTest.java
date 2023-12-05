package cn.iocoder.yudao.module.crm.service.customerlimitconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerLimitConfigMapper;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerLimitConfigServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_LIMIT_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：单测后面搞
/**
 * {@link CrmCustomerLimitConfigServiceImpl} 的单元测试类
 *
 * @author Wanwan
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CrmCustomerLimitConfigServiceImpl.class)
public class CrmCustomerLimitConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmCustomerLimitConfigServiceImpl customerLimitConfigService;

    @Resource
    private CrmCustomerLimitConfigMapper customerLimitConfigMapper;

    @Test
    public void testCreateCustomerLimitConfig_success() {
        // 准备参数
        CrmCustomerLimitConfigCreateReqVO reqVO = randomPojo(CrmCustomerLimitConfigCreateReqVO.class);

        // 调用
        Long customerLimitConfigId = customerLimitConfigService.createCustomerLimitConfig(reqVO);
        // 断言
        assertNotNull(customerLimitConfigId);
        // 校验记录的属性是否正确
        CrmCustomerLimitConfigDO customerLimitConfig = customerLimitConfigMapper.selectById(customerLimitConfigId);
        assertPojoEquals(reqVO, customerLimitConfig);
    }

    @Test
    public void testUpdateCustomerLimitConfig_success() {
        // mock 数据
        CrmCustomerLimitConfigDO dbCustomerLimitConfig = randomPojo(CrmCustomerLimitConfigDO.class);
        customerLimitConfigMapper.insert(dbCustomerLimitConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmCustomerLimitConfigUpdateReqVO reqVO = randomPojo(CrmCustomerLimitConfigUpdateReqVO.class, o -> {
            o.setId(dbCustomerLimitConfig.getId()); // 设置更新的 ID
        });

        // 调用
        customerLimitConfigService.updateCustomerLimitConfig(reqVO);
        // 校验是否更新正确
        CrmCustomerLimitConfigDO customerLimitConfig = customerLimitConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, customerLimitConfig);
    }

    @Test
    public void testUpdateCustomerLimitConfig_notExists() {
        // 准备参数
        CrmCustomerLimitConfigUpdateReqVO reqVO = randomPojo(CrmCustomerLimitConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> customerLimitConfigService.updateCustomerLimitConfig(reqVO), CUSTOMER_LIMIT_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteCustomerLimitConfig_success() {
        // mock 数据
        CrmCustomerLimitConfigDO dbCustomerLimitConfig = randomPojo(CrmCustomerLimitConfigDO.class);
        customerLimitConfigMapper.insert(dbCustomerLimitConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCustomerLimitConfig.getId();

        // 调用
        customerLimitConfigService.deleteCustomerLimitConfig(id);
        // 校验数据不存在了
        assertNull(customerLimitConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteCustomerLimitConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> customerLimitConfigService.deleteCustomerLimitConfig(id), CUSTOMER_LIMIT_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCustomerLimitConfigPage() {
        // mock 数据
        CrmCustomerLimitConfigDO dbCustomerLimitConfig = randomPojo(CrmCustomerLimitConfigDO.class, o -> { // 等会查询到
        });
        customerLimitConfigMapper.insert(dbCustomerLimitConfig);
        // 准备参数
        CrmCustomerLimitConfigPageReqVO reqVO = new CrmCustomerLimitConfigPageReqVO();

        // 调用
        PageResult<CrmCustomerLimitConfigDO> pageResult = customerLimitConfigService.getCustomerLimitConfigPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbCustomerLimitConfig, pageResult.getList().get(0));
    }

}
