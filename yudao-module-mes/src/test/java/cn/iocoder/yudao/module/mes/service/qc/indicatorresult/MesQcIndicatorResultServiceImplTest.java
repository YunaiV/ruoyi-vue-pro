package cn.iocoder.yudao.module.mes.service.qc.indicatorresult;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.indicatorresult.MesQcIndicatorResultMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcResultValueTypeEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import cn.iocoder.yudao.module.mes.service.qc.ipqc.MesQcIpqcService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcService;
import cn.iocoder.yudao.module.mes.service.qc.rqc.MesQcRqcService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * {@link MesQcIndicatorResultServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesQcIndicatorResultServiceImpl.class)
public class MesQcIndicatorResultServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesQcIndicatorResultServiceImpl indicatorResultService;

    @Resource
    private MesQcIndicatorResultMapper resultMapper;

    @MockitoBean
    private MesQcIndicatorResultDetailService resultDetailService;
    @MockitoBean
    private MesQcIndicatorService indicatorService;
    @MockitoBean
    private MesQcIqcService iqcService;
    @MockitoBean
    private MesQcIpqcService ipqcService;
    @MockitoBean
    private MesQcOqcService oqcService;
    @MockitoBean
    private MesQcRqcService rqcService;
    @MockitoBean
    private DictDataApi dictDataApi;

    @Test
    public void testCreateIndicatorResult_success() {
        // 准备参数
        MesQcIndicatorResultSaveReqVO reqVO = buildReqVO("plain text value", MesQcResultValueTypeEnum.TEXT.getType());
        // mock 方法
        mockIqcAndIndicator(reqVO, MesQcResultValueTypeEnum.TEXT.getType());

        // 调用
        Long resultId = indicatorResultService.createIndicatorResult(reqVO);

        // 断言
        assertNotNull(resultId);
        MesQcIndicatorResultDO dbResult = resultMapper.selectById(resultId);
        assertNotNull(dbResult);
        assertEquals(reqVO.getCode(), dbResult.getCode());
        assertEquals(reqVO.getQcId(), dbResult.getQcId());
        assertEquals(reqVO.getQcType(), dbResult.getQcType());
        assertEquals(100L, dbResult.getItemId()); // 从 mock IQC 获取
        verify(resultDetailService).createDetailList(anyList());
    }

    @Test
    public void testCreateIndicatorResult_fileValueInvalid() {
        // 准备参数
        MesQcIndicatorResultSaveReqVO reqVO = buildReqVO("not-a-url", MesQcResultValueTypeEnum.FILE.getType());
        // mock 方法
        mockIqcAndIndicator(reqVO, MesQcResultValueTypeEnum.FILE.getType());

        // 调用，并断言异常
        assertServiceException(() -> indicatorResultService.createIndicatorResult(reqVO),
                QC_RESULT_VALUE_FORMAT_INVALID, "检测项[检测项A]要求文件 URL，实际值=not-a-url");

        // 断言：未入库
        assertEquals(0, resultMapper.selectCount());
        verify(resultDetailService, never()).createDetailList(anyList());
    }

    @Test
    public void testUpdateIndicatorResult_success() {
        // mock 数据
        MesQcIndicatorResultDO dbResult = randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcType(MesQcTypeEnum.IQC.getType());
        });
        resultMapper.insert(dbResult);
        // 准备参数
        MesQcIndicatorResultSaveReqVO reqVO = buildReqVO("updated text", MesQcResultValueTypeEnum.TEXT.getType());
        reqVO.setId(dbResult.getId());
        // mock 方法
        MesQcIndicatorDO indicator = new MesQcIndicatorDO();
        indicator.setId(reqVO.getItems().get(0).getIndicatorId());
        indicator.setName("检测项A");
        indicator.setResultType(MesQcResultValueTypeEnum.TEXT.getType());
        when(indicatorService.validateIndicatorListExists(anySet()))
                .thenReturn(MapUtil.of(indicator.getId(), indicator));

        // 调用
        indicatorResultService.updateIndicatorResult(reqVO);

        // 断言
        MesQcIndicatorResultDO updatedResult = resultMapper.selectById(dbResult.getId());
        assertEquals(reqVO.getCode(), updatedResult.getCode());
        // qcId/qcType/itemId 不允许改挂，应保持原值
        assertEquals(dbResult.getQcId(), updatedResult.getQcId());
        assertEquals(dbResult.getQcType(), updatedResult.getQcType());
        assertEquals(dbResult.getItemId(), updatedResult.getItemId());
        verify(resultDetailService).createOrUpdateDetailList(anyList());
    }

    @Test
    public void testUpdateIndicatorResult_notExists() {
        // 准备参数
        MesQcIndicatorResultSaveReqVO reqVO = buildReqVO("text", MesQcResultValueTypeEnum.TEXT.getType());
        reqVO.setId(randomLongId());

        // 调用，并断言异常
        assertServiceException(() -> indicatorResultService.updateIndicatorResult(reqVO),
                QC_RESULT_NOT_EXISTS);
    }

    @Test
    public void testDeleteIndicatorResult_success() {
        // mock 数据
        MesQcIndicatorResultDO dbResult = randomPojo(MesQcIndicatorResultDO.class);
        resultMapper.insert(dbResult);

        // 调用
        indicatorResultService.deleteIndicatorResult(dbResult.getId());

        // 断言
        assertNull(resultMapper.selectById(dbResult.getId()));
        verify(resultDetailService).deleteDetailByResultId(dbResult.getId());
    }

    @Test
    public void testDeleteIndicatorResult_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> indicatorResultService.deleteIndicatorResult(randomLongId()),
                QC_RESULT_NOT_EXISTS);
    }

    @Test
    public void testGetIndicatorResult() {
        // mock 数据
        MesQcIndicatorResultDO dbResult = randomPojo(MesQcIndicatorResultDO.class);
        resultMapper.insert(dbResult);

        // 调用
        MesQcIndicatorResultDO result = indicatorResultService.getIndicatorResult(dbResult.getId());

        // 断言
        assertPojoEquals(dbResult, result);
    }

    @Test
    public void testGetIndicatorResultPage() {
        // mock 数据
        MesQcIndicatorResultDO dbResult = randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcId(1L);
            o.setQcType(MesQcTypeEnum.IQC.getType());
            o.setCode("SPL-001");
            o.setItemId(100L);
        });
        resultMapper.insert(dbResult);
        // 测试 qcId 不匹配
        resultMapper.insert(cloneIgnoreId(dbResult, o -> o.setQcId(2L)));
        // 测试 qcType 不匹配
        resultMapper.insert(cloneIgnoreId(dbResult, o -> o.setQcType(MesQcTypeEnum.IPQC.getType())));
        // 测试 code 不匹配
        resultMapper.insert(cloneIgnoreId(dbResult, o -> o.setCode("SPL-999")));
        // 测试 itemId 不匹配
        resultMapper.insert(cloneIgnoreId(dbResult, o -> o.setItemId(999L)));
        // 准备参数
        MesQcIndicatorResultPageReqVO pageReqVO = new MesQcIndicatorResultPageReqVO();
        pageReqVO.setQcId(1L);
        pageReqVO.setQcType(MesQcTypeEnum.IQC.getType());
        pageReqVO.setCode("SPL-001");
        pageReqVO.setItemId(100L);

        // 调用
        PageResult<MesQcIndicatorResultDO> pageResult = indicatorResultService.getIndicatorResultPage(pageReqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbResult, pageResult.getList().get(0));
    }

    @Test
    public void testGetIndicatorResultCountByQcIdAndType() {
        // mock 数据
        MesQcIndicatorResultDO result1 = randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcId(1L);
            o.setQcType(MesQcTypeEnum.IQC.getType());
        });
        resultMapper.insert(result1);
        MesQcIndicatorResultDO result2 = randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcId(1L);
            o.setQcType(MesQcTypeEnum.IQC.getType());
        });
        resultMapper.insert(result2);
        // 不匹配
        resultMapper.insert(randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcId(2L);
            o.setQcType(MesQcTypeEnum.IPQC.getType());
        }));

        // 调用
        Long count = indicatorResultService.getIndicatorResultCountByQcIdAndType(1L, MesQcTypeEnum.IQC.getType());

        // 断言
        assertEquals(2L, count);
    }

    @Test
    public void testValidateIndicatorResultExistsByQcIdAndType_success() {
        // mock 数据
        resultMapper.insert(randomPojo(MesQcIndicatorResultDO.class, o -> {
            o.setQcId(1L);
            o.setQcType(MesQcTypeEnum.IQC.getType());
        }));

        // 调用（不抛异常即通过）
        indicatorResultService.validateIndicatorResultExistsByQcIdAndType(1L, MesQcTypeEnum.IQC.getType());
    }

    @Test
    public void testValidateIndicatorResultExistsByQcIdAndType_notExists() {
        // 调用，并断言异常
        assertServiceException(
                () -> indicatorResultService.validateIndicatorResultExistsByQcIdAndType(randomLongId(), MesQcTypeEnum.IQC.getType()),
                QC_FINISH_INDICATOR_RESULT_REQUIRED);
    }

    // ==================== 辅助方法 ====================

    private void mockIqcAndIndicator(MesQcIndicatorResultSaveReqVO reqVO, Integer resultType) {
        MesQcIqcDO iqc = new MesQcIqcDO();
        iqc.setId(reqVO.getQcId());
        iqc.setItemId(100L);
        when(iqcService.validateIqcExists(reqVO.getQcId())).thenReturn(iqc);

        MesQcIndicatorDO indicator = new MesQcIndicatorDO();
        indicator.setId(reqVO.getItems().get(0).getIndicatorId());
        indicator.setName("检测项A");
        indicator.setResultType(resultType);
        when(indicatorService.validateIndicatorListExists(anySet()))
                .thenReturn(MapUtil.of(indicator.getId(), indicator));
    }

    private MesQcIndicatorResultSaveReqVO buildReqVO(String value, Integer resultType) {
        MesQcIndicatorResultSaveReqVO.Item item = new MesQcIndicatorResultSaveReqVO.Item();
        item.setIndicatorId(10L);
        item.setValue(value);

        MesQcIndicatorResultSaveReqVO reqVO = new MesQcIndicatorResultSaveReqVO();
        reqVO.setCode("SPL-001");
        reqVO.setQcId(1L);
        reqVO.setQcType(MesQcTypeEnum.IQC.getType());
        reqVO.setItems(ListUtil.of(item));
        return reqVO;
    }

}
