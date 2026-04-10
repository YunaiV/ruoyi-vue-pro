package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodeRecordMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodePartStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesMdAutoCodeRecordServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class MesMdAutoCodeRecordServiceImplTest {

    @InjectMocks
    private MesMdAutoCodeRecordServiceImpl recordService;

    @Mock
    private MesMdAutoCodeRecordMapper recordMapper;

    @Mock
    private MesMdAutoCodeRuleService ruleService;
    @Mock
    private MesMdAutoCodePartService partService;

    @Mock
    private MesMdAutoCodePartStrategy fixedCharStrategy;
    @Mock
    private MesMdAutoCodePartStrategy serialNumberStrategy;
    @Mock
    private MesMdAutoCodePartStrategy inputCharStrategy;
    @Mock
    private MesMdAutoCodePartStrategy dateStrategy;

    @BeforeEach
    public void setUp() {
        // 使用反射注入 strategyList
        List<MesMdAutoCodePartStrategy> strategyList = Arrays.asList(
                fixedCharStrategy, serialNumberStrategy, inputCharStrategy, dateStrategy
        );
        when(fixedCharStrategy.getType()).thenReturn(MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType());
        when(serialNumberStrategy.getType()).thenReturn(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType());
        when(inputCharStrategy.getType()).thenReturn(MesMdAutoCodePartTypeEnum.INPUT_CHAR.getType());
        when(dateStrategy.getType()).thenReturn(MesMdAutoCodePartTypeEnum.DATE.getType());
        ReflectUtil.setFieldValue(recordService, "strategyList", strategyList);
        // 手动调用 init 方法初始化策略 map
        recordService.init();
    }

    @Test
    public void testGenerateAutoCode_fixedCharAndSerialNumber() {
        // 准备参数
        String ruleCode = "ITEM_CODE";
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(1L).setCode(ruleCode).setPadded(false);
        MesMdAutoCodePartDO part1 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType())
                .setFixCharacter("ITEM_").setLength(5).setSort(1);
        MesMdAutoCodePartDO part2 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType())
                .setSerialStartNo(1).setSerialStep(1).setLength(4).setSort(2).setCycleFlag(false);
        // mock
        when(ruleService.getAutoCodeRuleByCode(ruleCode)).thenReturn(rule);
        when(partService.getAutoCodePartListByRuleId(1L)).thenReturn(Arrays.asList(part1, part2));
        when(fixedCharStrategy.generate(eq(part1), any(MesMdAutoCodeContext.class))).thenReturn("ITEM_");
        when(serialNumberStrategy.generate(eq(part2), any(MesMdAutoCodeContext.class))).thenAnswer(invocation -> {
            MesMdAutoCodeContext context = invocation.getArgument(1);
            context.setSerialNo(1L);
            return "0001";
        });
        when(recordMapper.selectByResult("ITEM_0001")).thenReturn(null);

        // 调用
        String result = recordService.generateAutoCode(ruleCode, null);
        // 断言
        assertEquals("ITEM_0001", result);
        ArgumentCaptor<MesMdAutoCodeRecordDO> captor = ArgumentCaptor.forClass(MesMdAutoCodeRecordDO.class);
        verify(recordMapper).insert(captor.capture());
        MesMdAutoCodeRecordDO record = captor.getValue();
        assertEquals(1L, record.getRuleId());
        assertEquals("ITEM_0001", record.getResult());
        assertEquals(1, record.getSerialNo());
    }

    @Test
    public void testGenerateAutoCode_withInputChar() {
        // 准备参数
        String ruleCode = "CLIENT_CODE";
        String inputChar = "A";
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(2L).setCode(ruleCode).setPadded(false);
        MesMdAutoCodePartDO part1 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.INPUT_CHAR.getType())
                .setLength(1).setSort(1);
        MesMdAutoCodePartDO part2 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType())
                .setSerialStartNo(1).setSerialStep(1).setLength(4).setSort(2).setCycleFlag(false);
        // mock
        when(ruleService.getAutoCodeRuleByCode(ruleCode)).thenReturn(rule);
        when(partService.getAutoCodePartListByRuleId(2L)).thenReturn(Arrays.asList(part1, part2));
        when(inputCharStrategy.generate(eq(part1), any(MesMdAutoCodeContext.class))).thenReturn("A");
        when(serialNumberStrategy.generate(eq(part2), any(MesMdAutoCodeContext.class))).thenReturn("0001");
        when(recordMapper.selectByResult("A0001")).thenReturn(null);

        // 调用
        String result = recordService.generateAutoCode(ruleCode, inputChar);
        // 断言
        assertEquals("A0001", result);
        ArgumentCaptor<MesMdAutoCodeRecordDO> captor = ArgumentCaptor.forClass(MesMdAutoCodeRecordDO.class);
        verify(recordMapper).insert(captor.capture());
        MesMdAutoCodeRecordDO record = captor.getValue();
        assertEquals(inputChar, record.getInputChar());
        assertEquals("A0001", record.getResult());
    }

    @Test
    public void testGenerateAutoCode_withPadding() {
        // 准备参数
        String ruleCode = "PADDED_CODE";
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(3L).setCode(ruleCode)
                .setPadded(true).setMaxLength(10).setPaddedChar("0").setPaddedMethod(1);
        MesMdAutoCodePartDO part1 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType())
                .setFixCharacter("IT").setLength(2).setSort(1);
        // mock
        when(ruleService.getAutoCodeRuleByCode(ruleCode)).thenReturn(rule);
        when(partService.getAutoCodePartListByRuleId(3L)).thenReturn(Arrays.asList(part1));
        when(fixedCharStrategy.generate(eq(part1), any(MesMdAutoCodeContext.class))).thenReturn("IT");
        when(recordMapper.selectByResult("00000000IT")).thenReturn(null);

        // 调用
        String result = recordService.generateAutoCode(ruleCode, null);
        // 断言：IT 补齐到 10 位，左补 0
        assertEquals("00000000IT", result);
        verify(recordMapper).insert(any(MesMdAutoCodeRecordDO.class));
    }

    @Test
    public void testGenerateAutoCode_withDatePart() {
        // 准备参数
        String ruleCode = "DATE_CODE";
        MesMdAutoCodeRuleDO rule = new MesMdAutoCodeRuleDO().setId(4L).setCode(ruleCode).setPadded(false);
        MesMdAutoCodePartDO part1 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.DATE.getType())
                .setDateFormat("yyyyMMdd").setLength(8).setSort(1);
        MesMdAutoCodePartDO part2 = new MesMdAutoCodePartDO().setType(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType())
                .setSerialStartNo(1).setSerialStep(1).setLength(4).setSort(2).setCycleFlag(false);

        // mock
        when(ruleService.getAutoCodeRuleByCode(ruleCode)).thenReturn(rule);
        when(partService.getAutoCodePartListByRuleId(4L)).thenReturn(Arrays.asList(part1, part2));
        when(dateStrategy.generate(eq(part1), any(MesMdAutoCodeContext.class))).thenReturn("20260304");
        when(serialNumberStrategy.generate(eq(part2), any(MesMdAutoCodeContext.class))).thenReturn("0001");
        when(recordMapper.selectByResult("202603040001")).thenReturn(null);

        // 调用
        String result = recordService.generateAutoCode(ruleCode, null);
        // 断言：日期 8 位 + 流水号 4 位 = 12 位
        assertEquals(12, result.length());
        assertEquals("202603040001", result);
        verify(recordMapper).insert(any(MesMdAutoCodeRecordDO.class));
    }

}
