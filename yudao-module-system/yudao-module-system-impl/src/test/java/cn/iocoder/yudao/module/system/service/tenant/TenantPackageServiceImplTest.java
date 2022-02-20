package cn.iocoder.yudao.module.system.service.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages.TenantPackageUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantPackageMapper;
import cn.iocoder.yudao.module.system.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.TENANT_PACKAGE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link TenantPackageServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(TenantPackageServiceImpl.class)
public class TenantPackageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TenantPackageServiceImpl tenantPackageService;

    @Resource
    private TenantPackageMapper tenantPackageMapper;

    @Test
    public void testCreateTenantPackage_success() {
        // 准备参数
        TenantPackageCreateReqVO reqVO = randomPojo(TenantPackageCreateReqVO.class);

        // 调用
        Long tenantPackageId = tenantPackageService.createTenantPackage(reqVO);
        // 断言
        assertNotNull(tenantPackageId);
        // 校验记录的属性是否正确
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(tenantPackageId);
        assertPojoEquals(reqVO, tenantPackage);
    }

    @Test
    public void testUpdateTenantPackage_success() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TenantPackageUpdateReqVO reqVO = randomPojo(TenantPackageUpdateReqVO.class, o -> {
            o.setId(dbTenantPackage.getId()); // 设置更新的 ID
        });

        // 调用
        tenantPackageService.updateTenantPackage(reqVO);
        // 校验是否更新正确
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, tenantPackage);
    }

    @Test
    public void testUpdateTenantPackage_notExists() {
        // 准备参数
        TenantPackageUpdateReqVO reqVO = randomPojo(TenantPackageUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.updateTenantPackage(reqVO), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteTenantPackage_success() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTenantPackage.getId();

        // 调用
        tenantPackageService.deleteTenantPackage(id);
       // 校验数据不存在了
       assertNull(tenantPackageMapper.selectById(id));
    }

    @Test
    public void testDeleteTenantPackage_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.deleteTenantPackage(id), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetTenantPackagePage() {
       // mock 数据
       TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       tenantPackageMapper.insert(dbTenantPackage);
       // 测试 name 不匹配
       tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setName(null)));
       // 测试 status 不匹配
       tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setCreateTime(null)));
       // 准备参数
       TenantPackagePageReqVO reqVO = new TenantPackagePageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<TenantPackageDO> pageResult = tenantPackageService.getTenantPackagePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTenantPackage, pageResult.getList().get(0));
    }

}
