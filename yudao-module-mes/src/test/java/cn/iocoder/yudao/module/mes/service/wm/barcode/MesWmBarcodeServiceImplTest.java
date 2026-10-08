package cn.iocoder.yudao.module.mes.service.wm.barcode;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.MesWmBarcodeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.barcode.MesWmBarcodeMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_BARCODE_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_BARCODE_CONTENT_DUPLICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MesWmBarcodeServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class MesWmBarcodeServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MesWmBarcodeServiceImpl barcodeService;

    @Mock
    private MesWmBarcodeMapper barcodeMapper;
    @Mock
    private MesWmBarcodeConfigService barcodeConfigService;

    @Test
    public void testUpdateBarcode_sameBizObject() {
        // 准备参数：保持业务对象和条码内容不变
        MesWmBarcodeSaveReqVO reqVO = createReqVO().setId(1L);
        MesWmBarcodeDO barcode = new MesWmBarcodeDO().setId(1L);
        when(barcodeMapper.selectById(1L)).thenReturn(barcode);
        mockConfig();
        when(barcodeMapper.selectByBizTypeAndBizId(102, 10L)).thenReturn(barcode);
        when(barcodeMapper.selectByContent("WH001")).thenReturn(barcode);

        // 调用
        barcodeService.updateBarcode(reqVO);
        // 断言：排除自身后正常更新
        verify(barcodeMapper).updateById(argThat((MesWmBarcodeDO o) -> o.getId().equals(1L)
                && o.getContent().equals("WH001") && o.getConfigId().equals(5L)));
    }

    @Test
    public void testUpdateBarcode_bizObjectDuplicate() {
        // 准备参数：业务对象已被另一条条码占用
        MesWmBarcodeSaveReqVO reqVO = createReqVO().setId(1L);
        when(barcodeMapper.selectById(1L)).thenReturn(new MesWmBarcodeDO().setId(1L));
        mockConfig();
        when(barcodeMapper.selectByBizTypeAndBizId(102, 10L)).thenReturn(new MesWmBarcodeDO().setId(2L));

        // 调用并断言
        assertServiceException(() -> barcodeService.updateBarcode(reqVO), WM_BARCODE_ALREADY_EXISTS);
        verify(barcodeMapper, never()).updateById(any(MesWmBarcodeDO.class));
    }

    @Test
    public void testCreateBarcode_bizObjectDuplicate() {
        // 准备参数：新增时业务对象已有条码
        MesWmBarcodeSaveReqVO reqVO = createReqVO();
        mockConfig();
        when(barcodeMapper.selectByBizTypeAndBizId(102, 10L)).thenReturn(new MesWmBarcodeDO().setId(2L));

        // 调用并断言
        assertServiceException(() -> barcodeService.createBarcode(reqVO), WM_BARCODE_ALREADY_EXISTS);
        verify(barcodeMapper, never()).insert(any(MesWmBarcodeDO.class));
    }

    @Test
    public void testUpdateBarcode_contentDuplicate() {
        // 准备参数：业务对象未改变，条码内容已被另一条记录占用
        MesWmBarcodeSaveReqVO reqVO = createReqVO().setId(1L);
        MesWmBarcodeDO barcode = new MesWmBarcodeDO().setId(1L);
        when(barcodeMapper.selectById(1L)).thenReturn(barcode);
        mockConfig();
        when(barcodeMapper.selectByBizTypeAndBizId(102, 10L)).thenReturn(barcode);
        when(barcodeMapper.selectByContent("WH001")).thenReturn(new MesWmBarcodeDO().setId(2L));

        // 调用并断言
        assertServiceException(() -> barcodeService.updateBarcode(reqVO), WM_BARCODE_CONTENT_DUPLICATE);
        verify(barcodeMapper, never()).updateById(any(MesWmBarcodeDO.class));
    }

    private MesWmBarcodeSaveReqVO createReqVO() {
        return new MesWmBarcodeSaveReqVO().setBizType(102).setBizId(10L).setBizCode("WH001")
                .setContent("WH001");
    }

    private void mockConfig() {
        when(barcodeConfigService.validateBarcodeConfigByBizType(102))
                .thenReturn(new MesWmBarcodeConfigDO().setId(5L));
    }

}
