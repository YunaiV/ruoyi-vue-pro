package cn.iocoder.yudao.module.mes.service.wm.productsales;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productsales.MesWmProductSalesLineMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcCheckResultEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmProductSalesLineServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(MesWmProductSalesLineServiceImpl.class)
public class MesWmProductSalesLineServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmProductSalesLineServiceImpl productSalesLineService;

    @Resource
    private MesWmProductSalesLineMapper productSalesLineMapper;

    @MockitoBean
    private MesWmProductSalesService productSalesService;

    @MockitoBean
    private MesWmProductSalesDetailService productSalesDetailService;

    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesWmBatchService batchService;

    @Test
    public void testUpdateProductSalesLineWhenOqcFinish_pass() {
        // 准备参数
        Long oqcId = randomLongId();
        Integer checkResult = MesQcCheckResultEnum.PASS.getType();

        // mock 数据
        MesWmProductSalesLineDO lineDO = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setOqcCheckFlag(true);         // 需要 OQC 检验
            o.setQualityStatus(null);    // 初始为空
        });
        productSalesLineMapper.insert(lineDO);

        // 调用
        productSalesLineService.updateProductSalesLineWhenOqcFinish(lineDO.getId(), oqcId, checkResult);

        // 断言：验证行字段更新
        MesWmProductSalesLineDO updateLine = productSalesLineMapper.selectById(lineDO.getId());
        assertEquals(oqcId, updateLine.getOqcId());
        assertEquals(MesWmQualityStatusEnum.PASS.getStatus(), updateLine.getQualityStatus());
    }

    @Test
    public void testUpdateProductSalesLineWhenOqcFinish_fail() {
        // 准备参数
        Long oqcId = randomLongId();
        Integer checkResult = MesQcCheckResultEnum.FAIL.getType();

        // mock 数据
        MesWmProductSalesLineDO lineDO = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setOqcCheckFlag(true);
            o.setQualityStatus(null);
        });
        productSalesLineMapper.insert(lineDO);

        // 调用
        productSalesLineService.updateProductSalesLineWhenOqcFinish(lineDO.getId(), oqcId, checkResult);

        // 断言：验证行字段更新
        MesWmProductSalesLineDO updateLine = productSalesLineMapper.selectById(lineDO.getId());
        assertEquals(oqcId, updateLine.getOqcId());
        assertEquals(MesWmQualityStatusEnum.FAIL.getStatus(), updateLine.getQualityStatus());
    }

    @Test
    public void testUpdateProductSalesLineWhenOqcFinish_cancelWhenNg() {
        // 准备数据：同一出库单下 2 行，第 1 行已合格，第 2 行即将变更为不合格
        Long salesId = randomLongId();
        MesWmProductSalesLineDO line1 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(true);
            o.setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus());
        });
        productSalesLineMapper.insert(line1);

        MesWmProductSalesLineDO line2 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(true);
            o.setQualityStatus(null); // 待检
        });
        productSalesLineMapper.insert(line2);

        // 调用：第 2 行质检不合格
        productSalesLineService.updateProductSalesLineWhenOqcFinish(
                line2.getId(), randomLongId(), MesQcCheckResultEnum.FAIL.getType());

        // 断言：应该调用 cancelProductSales
        verify(productSalesService).cancelProductSales(eq(salesId));
    }

    @Test
    public void testUpdateProductSalesLineWhenOqcFinish_noCancelWhenAllPass() {
        // 准备数据：同一出库单下 2 行，第 1 行已合格，第 2 行即将变更为合格
        Long salesId = randomLongId();
        MesWmProductSalesLineDO line1 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(true);
            o.setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus());
        });
        productSalesLineMapper.insert(line1);

        MesWmProductSalesLineDO line2 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(true);
            o.setQualityStatus(null);
        });
        productSalesLineMapper.insert(line2);

        // 调用：第 2 行质检合格
        productSalesLineService.updateProductSalesLineWhenOqcFinish(
                line2.getId(), randomLongId(), MesQcCheckResultEnum.PASS.getType());

        // 断言：不应该调用 cancelProductSales
        verify(productSalesService, never()).cancelProductSales(anyLong());
    }

    @Test
    public void testUpdateProductSalesLineWhenOqcFinish_skipNonOqcCheckLines() {
        // 准备数据：同一出库单下 2 行，第 1 行不需要 OQC 检验（oqcCheckFlag=false），第 2 行需要检验且不合格
        Long salesId = randomLongId();
        MesWmProductSalesLineDO line1 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(false); // 不需要 OQC 检验
            o.setQualityStatus(null);
        });
        productSalesLineMapper.insert(line1);

        MesWmProductSalesLineDO line2 = randomPojo(MesWmProductSalesLineDO.class, o -> {
            o.setSalesId(salesId);
            o.setOqcCheckFlag(true);
            o.setQualityStatus(null);
        });
        productSalesLineMapper.insert(line2);

        // 调用：第 2 行质检不合格
        productSalesLineService.updateProductSalesLineWhenOqcFinish(
                line2.getId(), randomLongId(), MesQcCheckResultEnum.FAIL.getType());

        // 断言：第 2 行不合格 → 应该取消出库单
        verify(productSalesService).cancelProductSales(eq(salesId));
    }

}
