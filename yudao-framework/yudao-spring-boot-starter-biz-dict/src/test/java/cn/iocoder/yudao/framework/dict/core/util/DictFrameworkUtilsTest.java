package cn.iocoder.yudao.framework.dict.core.util;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link DictFrameworkUtils} 的单元测试
 */
public class DictFrameworkUtilsTest extends BaseMockitoUnitTest {

    @Mock
    private DictDataApi dictDataApi;

    @BeforeEach
    public void setUp() {
        DictFrameworkUtils.init(dictDataApi);
    }

    @Test
    public void getDictDataLabelTest() {
        DictDataRespDTO dataRespDTO = randomPojo();
        when(dictDataApi.getDictData(dataRespDTO.getDictType(), dataRespDTO.getValue())).thenReturn(dataRespDTO);
        String dictDataLabel = DictFrameworkUtils.getDictDataLabel(dataRespDTO.getDictType(), dataRespDTO.getValue());
        assertEquals(dataRespDTO.getLabel(), dictDataLabel);
    }

    @Test
    public void parseDictDataValueTest() {
        DictDataRespDTO resp = randomPojo();
        when(dictDataApi.parseDictData(resp.getDictType(), resp.getLabel())).thenReturn(resp);
        String value = DictFrameworkUtils.parseDictDataValue(resp.getDictType(), resp.getLabel());
        assertEquals(resp.getValue(), value);
    }

    private DictDataRespDTO randomPojo() {
        DictDataRespDTO dataRespDTO = new DictDataRespDTO();
        dataRespDTO.setLabel(RandomUtils.randomString());
        dataRespDTO.setValue(RandomUtils.randomString());
        dataRespDTO.setDictType(RandomUtils.randomString());
        dataRespDTO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        return dataRespDTO;
    }
}
