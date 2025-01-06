package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.erp.config.ErpIntegrationConfig;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json.GuidePriceJson;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.config.ConfigApiImpl;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.infra.api.file.FileApiImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApiImpl;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApiImpl;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;
import cn.iocoder.yudao.module.system.service.dept.PostServiceImpl;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.PermissionServiceImpl;
import cn.iocoder.yudao.module.system.service.permission.RoleServiceImpl;
import cn.iocoder.yudao.module.system.service.user.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErpProductServiceImpl} 的单元测试类
 *
 * @author 王奇辉
 */
@Import({ErpProductServiceImpl.class,
        ErpProductCategoryServiceImpl.class,
        ErpProductUnitServiceImpl.class,
        DeptApiImpl.class,
        AdminUserApiImpl.class,
        AdminUserServiceImpl.class,
        MessageChannel.class,
        ApplicationContext.class,
        ErpIntegrationConfig.class,
        DeptServiceImpl.class,
        PostServiceImpl.class,
        PermissionServiceImpl.class,
        RoleServiceImpl.class,
        PasswordEncoder.class,
        FileApiImpl.class,
        ConfigApiImpl.class})
public class ErpProductServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpProductServiceImpl productService;
    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpProductCategoryServiceImpl productCategoryService;
    @Resource
    private ErpProductUnitServiceImpl productUnitService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi userApi;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private FileApi fileApi;
    @MockBean
    private ConfigApi configApi;

    public static ErpProductSaveReqVO generateMock() {
        ErpProductSaveReqVO productVO = new ErpProductSaveReqVO();
        //productVO.setId(731L);
        productVO.setName("测试产品"+RandomUtil.randomInt(10000, 99999));
        productVO.setCategoryId(88L);
        productVO.setDeptId(50006L);
        productVO.setBarCode("TEST-SKU-"+RandomUtil.randomInt(10000, 99999));
        productVO.setUnitId(3L);
        productVO.setMaterial("测试材料"+RandomUtil.randomInt(10000, 99999));
        productVO.setStatus(true);
        productVO.setRemark("这是一个测试备注"+ LocalTime.now());
        productVO.setSeries("测试系列");
        productVO.setColor("红色");
        productVO.setModel("测试型号");
        //productVO.setSerial(1);
        productVO.setProductionNo("TEST-PROD-001");
        productVO.setWidth(1);
        productVO.setLength(1);
        productVO.setHeight(1);
        productVO.setProductOwnerId(50024L);
        productVO.setIndustrialDesignerId(50024L);
        productVO.setResearchDeveloperId(50024L);
        productVO.setMaintenanceEngineerId(50024L);


        // 创建 GuidePriceJson 列表
        List<GuidePriceJson> guidePriceList = Arrays.asList(
                new GuidePriceJson(1, new BigDecimal("1000.0")),
                new GuidePriceJson(1, new BigDecimal("2000.0"))
        );
        productVO.setGuidePriceList(guidePriceList);
        return productVO;
    }

    @Test
    public void testCreateProduct_success() {
        // 准备参数
        ErpProductSaveReqVO createReqVO = generateMock();
        // 调用
        Long productId = productService.createProduct(createReqVO);
        // 断言
        assertNotNull(productId);
    }

    @Test
    public void testUpdateProduct_success() {
        // 新增一条数据
        // 准备参数
        ErpProductSaveReqVO updateReqVO = generateMock();
        updateReqVO.setId(null);
        // 调用
        Long productId = productService.createProduct(updateReqVO);
        updateReqVO.setId(productId);
        updateReqVO.setRemark("更新成功的数据");
        // 调用
        productService.updateProduct(updateReqVO);
    }

    @Test
    public void testUpdateProduct_notExists() {
        // 准备参数
        ErpProductSaveReqVO updateReqVO = generateMock();
        updateReqVO.setId(randomLongId());
        // 调用, 并断言异常
        assertServiceException(() -> productService.updateProduct(updateReqVO), PRODUCT_NOT_EXISTS);
    }

    @Test
    public void testDeleteProduct_success() {
        // 准备参数
        ErpProductSaveReqVO updateReqVO = generateMock();
        updateReqVO.setId(null);
        // 调用
        Long productId = productService.createProduct(updateReqVO);
        // 调用
        productService.deleteProduct(productId);
        // 校验数据不存在了
        assertNull(productMapper.selectById(productId));
    }

    @Test
    public void testDeleteProduct_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_NOT_EXISTS);
    }

}