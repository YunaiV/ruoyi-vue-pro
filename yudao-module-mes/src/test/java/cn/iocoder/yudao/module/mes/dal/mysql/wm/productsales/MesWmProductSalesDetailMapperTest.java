package cn.iocoder.yudao.module.mes.dal.mysql.wm.productsales;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDetailDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MesWmProductSalesDetailMapper} 的单元测试
 * <p>
 * 覆盖：selectListByLineId、selectListBySalesId、deleteByLineId、deleteBySalesId
 */
public class MesWmProductSalesDetailMapperTest extends BaseDbUnitTest {

    @Resource
    private MesWmProductSalesDetailMapper detailMapper;

    // ==================== 辅助方法 ====================

    /**
     * 创建明细 DO，固定 BigDecimal 精度为 2 位，避免 H2 decimal(14,2) 精度不匹配
     */
    private MesWmProductSalesDetailDO createDetailPojo(Consumer<MesWmProductSalesDetailDO> consumer) {
        return randomPojo(MesWmProductSalesDetailDO.class, o -> {
            o.setQuantity(new BigDecimal("10.00"));
            consumer.accept(o);
        });
    }

    // ==================== selectListByLineId ====================

    @Test
    public void testSelectListByLineId_match() {
        Long lineId = 100L;
        MesWmProductSalesDetailDO match = createDetailPojo(o -> {
            o.setLineId(lineId);
            o.setSalesId(1L);
        });
        detailMapper.insert(match);
        // 插入另一条不同 lineId 的记录，不应被返回
        detailMapper.insert(cloneIgnoreId(match, o -> o.setLineId(999L)));

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListByLineId(lineId);

        assertEquals(1, result.size());
        assertPojoEquals(match, result.get(0));
    }

    @Test
    public void testSelectListByLineId_multipleRows() {
        Long lineId = 200L;
        MesWmProductSalesDetailDO detail1 = createDetailPojo(o -> {
            o.setLineId(lineId);
            o.setSalesId(2L);
        });
        MesWmProductSalesDetailDO detail2 = createDetailPojo(o -> {
            o.setLineId(lineId);
            o.setSalesId(2L);
        });
        detailMapper.insert(detail1);
        detailMapper.insert(detail2);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListByLineId(lineId);

        assertEquals(2, result.size());
    }

    @Test
    public void testSelectListByLineId_noMatch() {
        MesWmProductSalesDetailDO detail = createDetailPojo(o -> {
            o.setLineId(300L);
            o.setSalesId(3L);
        });
        detailMapper.insert(detail);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListByLineId(999L);

        assertTrue(result.isEmpty());
    }

    // ==================== selectListBySalesId ====================

    @Test
    public void testSelectListBySalesId_match() {
        Long salesId = 10L;
        MesWmProductSalesDetailDO match = createDetailPojo(o -> {
            o.setSalesId(salesId);
            o.setLineId(1L);
        });
        detailMapper.insert(match);
        // 插入另一条不同 salesId 的记录
        detailMapper.insert(cloneIgnoreId(match, o -> o.setSalesId(888L)));

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListBySalesId(salesId);

        assertEquals(1, result.size());
        assertPojoEquals(match, result.get(0));
    }

    @Test
    public void testSelectListBySalesId_multipleRows() {
        Long salesId = 20L;
        MesWmProductSalesDetailDO detail1 = createDetailPojo(o -> {
            o.setSalesId(salesId);
            o.setLineId(11L);
        });
        MesWmProductSalesDetailDO detail2 = createDetailPojo(o -> {
            o.setSalesId(salesId);
            o.setLineId(12L);
        });
        detailMapper.insert(detail1);
        detailMapper.insert(detail2);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListBySalesId(salesId);

        assertEquals(2, result.size());
    }

    @Test
    public void testSelectListBySalesId_noMatch() {
        MesWmProductSalesDetailDO detail = createDetailPojo(o -> {
            o.setSalesId(30L);
            o.setLineId(2L);
        });
        detailMapper.insert(detail);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListBySalesId(9999L);

        assertTrue(result.isEmpty());
    }

    // ==================== deleteByLineId ====================

    @Test
    public void testDeleteByLineId_deletesOnlyTargetLine() {
        Long lineId = 400L;
        MesWmProductSalesDetailDO toDelete = createDetailPojo(o -> {
            o.setLineId(lineId);
            o.setSalesId(4L);
        });
        MesWmProductSalesDetailDO toKeep = createDetailPojo(o -> {
            o.setLineId(500L);
            o.setSalesId(4L);
        });
        detailMapper.insert(toDelete);
        detailMapper.insert(toKeep);

        detailMapper.deleteByLineId(lineId);

        // lineId=400 的记录已被逻辑删除
        List<MesWmProductSalesDetailDO> deletedResult = detailMapper.selectListByLineId(lineId);
        assertTrue(deletedResult.isEmpty(), "lineId=400 的记录应被删除");

        // lineId=500 的记录保留
        List<MesWmProductSalesDetailDO> keptResult = detailMapper.selectListByLineId(500L);
        assertEquals(1, keptResult.size());
    }

    @Test
    public void testDeleteByLineId_multipleRows() {
        Long lineId = 600L;
        detailMapper.insert(createDetailPojo(o -> { o.setLineId(lineId); o.setSalesId(5L); }));
        detailMapper.insert(createDetailPojo(o -> { o.setLineId(lineId); o.setSalesId(5L); }));

        detailMapper.deleteByLineId(lineId);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListByLineId(lineId);
        assertTrue(result.isEmpty(), "同 lineId 下的所有记录均应被删除");
    }

    // ==================== deleteBySalesId ====================

    @Test
    public void testDeleteBySalesId_deletesOnlyTargetSales() {
        Long salesId = 700L;
        MesWmProductSalesDetailDO toDelete = createDetailPojo(o -> {
            o.setSalesId(salesId);
            o.setLineId(6L);
        });
        MesWmProductSalesDetailDO toKeep = createDetailPojo(o -> {
            o.setSalesId(800L);
            o.setLineId(7L);
        });
        detailMapper.insert(toDelete);
        detailMapper.insert(toKeep);

        detailMapper.deleteBySalesId(salesId);

        // salesId=700 的记录已被逻辑删除
        List<MesWmProductSalesDetailDO> deletedResult = detailMapper.selectListBySalesId(salesId);
        assertTrue(deletedResult.isEmpty(), "salesId=700 的记录应被删除");

        // salesId=800 的记录保留
        List<MesWmProductSalesDetailDO> keptResult = detailMapper.selectListBySalesId(800L);
        assertEquals(1, keptResult.size());
    }

    @Test
    public void testDeleteBySalesId_multipleRows() {
        Long salesId = 900L;
        detailMapper.insert(createDetailPojo(o -> { o.setSalesId(salesId); o.setLineId(8L); }));
        detailMapper.insert(createDetailPojo(o -> { o.setSalesId(salesId); o.setLineId(9L); }));
        detailMapper.insert(createDetailPojo(o -> { o.setSalesId(salesId); o.setLineId(10L); }));

        detailMapper.deleteBySalesId(salesId);

        List<MesWmProductSalesDetailDO> result = detailMapper.selectListBySalesId(salesId);
        assertTrue(result.isEmpty(), "同 salesId 下的所有记录均应被删除");
    }

}
