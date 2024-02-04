package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehousePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpWarehouseMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErpWarehouseServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ErpWarehouseServiceImpl.class)
public class ErpWarehouseServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpWarehouseServiceImpl warehouseService;

    @Resource
    private ErpWarehouseMapper warehouseMapper;

    @Test
    public void testCreateWarehouse_success() {
        // 准备参数
        ErpWarehouseSaveReqVO createReqVO = randomPojo(ErpWarehouseSaveReqVO.class).setId(null);

        // 调用
        Long warehouseId = warehouseService.createWarehouse(createReqVO);
        // 断言
        assertNotNull(warehouseId);
        // 校验记录的属性是否正确
        ErpWarehouseDO warehouse = warehouseMapper.selectById(warehouseId);
        assertPojoEquals(createReqVO, warehouse, "id");
    }

    @Test
    public void testUpdateWarehouse_success() {
        // mock 数据
        ErpWarehouseDO dbWarehouse = randomPojo(ErpWarehouseDO.class);
        warehouseMapper.insert(dbWarehouse);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ErpWarehouseSaveReqVO updateReqVO = randomPojo(ErpWarehouseSaveReqVO.class, o -> {
            o.setId(dbWarehouse.getId()); // 设置更新的 ID
        });

        // 调用
        warehouseService.updateWarehouse(updateReqVO);
        // 校验是否更新正确
        ErpWarehouseDO warehouse = warehouseMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, warehouse);
    }

    @Test
    public void testUpdateWarehouse_notExists() {
        // 准备参数
        ErpWarehouseSaveReqVO updateReqVO = randomPojo(ErpWarehouseSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> warehouseService.updateWarehouse(updateReqVO), WAREHOUSE_NOT_EXISTS);
    }

    @Test
    public void testDeleteWarehouse_success() {
        // mock 数据
        ErpWarehouseDO dbWarehouse = randomPojo(ErpWarehouseDO.class);
        warehouseMapper.insert(dbWarehouse);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbWarehouse.getId();

        // 调用
        warehouseService.deleteWarehouse(id);
       // 校验数据不存在了
       assertNull(warehouseMapper.selectById(id));
    }

    @Test
    public void testDeleteWarehouse_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> warehouseService.deleteWarehouse(id), WAREHOUSE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetWarehousePage() {
       // mock 数据
       ErpWarehouseDO dbWarehouse = randomPojo(ErpWarehouseDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
       });
       warehouseMapper.insert(dbWarehouse);
       // 测试 name 不匹配
       warehouseMapper.insert(cloneIgnoreId(dbWarehouse, o -> o.setName(null)));
       // 测试 status 不匹配
       warehouseMapper.insert(cloneIgnoreId(dbWarehouse, o -> o.setStatus(null)));
       // 准备参数
       ErpWarehousePageReqVO reqVO = new ErpWarehousePageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);

       // 调用
       PageResult<ErpWarehouseDO> pageResult = warehouseService.getWarehousePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbWarehouse, pageResult.getList().get(0));
    }

}