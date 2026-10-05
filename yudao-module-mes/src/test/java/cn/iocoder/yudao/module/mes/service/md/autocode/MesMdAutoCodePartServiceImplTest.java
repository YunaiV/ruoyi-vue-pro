package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodePartMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodeRuleMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MesMdAutoCodePartServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class MesMdAutoCodePartServiceImplTest {

    @InjectMocks
    private MesMdAutoCodePartServiceImpl partService;

    @Mock
    private MesMdAutoCodePartMapper partMapper;
    @Mock
    private MesMdAutoCodeRuleMapper ruleMapper;
    @Mock
    private MesMdAutoCodeRuleService ruleService;

    private static MesMdAutoCodePartDO fixedPart(Long id, Long ruleId, Integer sort, String fixCharacter, Integer length) {
        return MesMdAutoCodePartDO.builder()
                .id(id).ruleId(ruleId).sort(sort)
                .type(MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType())
                .length(length).fixCharacter(fixCharacter).build();
    }

    private static MesMdAutoCodePartSaveReqVO fixedReqVO(Long ruleId, Integer sort, String fixCharacter, Integer length) {
        MesMdAutoCodePartSaveReqVO reqVO = new MesMdAutoCodePartSaveReqVO();
        reqVO.setRuleId(ruleId);
        reqVO.setSort(sort);
        reqVO.setType(MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType());
        reqVO.setLength(length);
        reqVO.setFixCharacter(fixCharacter);
        return reqVO;
    }

    @Test
    public void testCreateAutoCodePart_fixedCharDuplicate() {
        // 准备：其他规则已占用相同前缀 "ITEM_"
        when(partMapper.selectFixedCharPartList()).thenReturn(Arrays.asList(
                fixedPart(1L, 1L, 1, "ITEM_", 5)));
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        // 断言：保存被拒，且未插入
        ServiceException ex = assertThrows(ServiceException.class,
                () -> partService.createAutoCodePart(fixedReqVO(10L, 1, "ITEM_", 5)));
        assertEquals(1040110006, ex.getCode());
        verify(partMapper, never()).insert(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

    @Test
    public void testCreateAutoCodePart_fixedCharUnique() {
        // 准备：其他规则前缀 "ITEM_"，本次保存 "SN_"
        when(partMapper.selectFixedCharPartList()).thenReturn(Arrays.asList(
                fixedPart(1L, 1L, 1, "ITEM_", 5)));
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        // 断言：保存成功
        partService.createAutoCodePart(fixedReqVO(10L, 1, "SN_", 3));
        verify(partMapper).insert(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

    @Test
    public void testCreateAutoCodePart_fixedCharTruncatedDuplicate() {
        // 准备：其他规则固定字符 "IT_X" 长度 2（生成时截断为 "IT"）
        when(partMapper.selectFixedCharPartList()).thenReturn(Arrays.asList(
                fixedPart(1L, 1L, 1, "IT_X", 2)));
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        // 断言：本次保存 "IT" 截断后同为 "IT"，保存被拒
        ServiceException ex = assertThrows(ServiceException.class,
                () -> partService.createAutoCodePart(fixedReqVO(10L, 1, "IT", 4)));
        assertEquals(1040110006, ex.getCode());
        verify(partMapper, never()).insert(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

    @Test
    public void testCreateAutoCodePart_bothRulesWithoutFixedChar() {
        // 准备：租户内另一规则无任何固定字符分段（前缀为空串），本次保存流水号分段（本规则也无固定字符）
        when(partMapper.selectFixedCharPartList()).thenReturn(Collections.emptyList());
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        MesMdAutoCodePartSaveReqVO reqVO = new MesMdAutoCodePartSaveReqVO();
        reqVO.setRuleId(10L);
        reqVO.setSort(1);
        reqVO.setType(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType());
        reqVO.setLength(4);
        reqVO.setSerialStartNo(1);

        // 断言：两条规则前缀均为空（生成的编码无法区分业务），保存被拒
        ServiceException ex = assertThrows(ServiceException.class,
                () -> partService.createAutoCodePart(reqVO));
        assertEquals(1040110006, ex.getCode());
        verify(partMapper, never()).insert(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

    @Test
    public void testUpdateAutoCodePart_fixedCharBecomesDuplicate() {
        // 准备：本规则已有固定字符 "OLD_"，修改为与其他规则相同的 "ITEM_"
        MesMdAutoCodePartDO myPart = fixedPart(100L, 10L, 1, "OLD_", 4);
        when(partMapper.selectById(100L)).thenReturn(myPart);
        when(partMapper.selectFixedCharPartList()).thenReturn(Arrays.asList(
                myPart, fixedPart(1L, 1L, 1, "ITEM_", 5)));
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        // 断言：保存被拒，且未更新
        ServiceException ex = assertThrows(ServiceException.class,
                () -> partService.updateAutoCodePart(fixedReqVO(10L, 1, "ITEM_", 5).setId(100L)));
        assertEquals(1040110006, ex.getCode());
        verify(partMapper, never()).updateById(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

    @Test
    public void testUpdateAutoCodePart_fixedCharBecomesUnique() {
        // 准备：本规则已有固定字符 "ITEM_"（与其他规则重复），修改为 "SN_"
        MesMdAutoCodePartDO myPart = fixedPart(100L, 10L, 1, "ITEM_", 5);
        when(partMapper.selectById(100L)).thenReturn(myPart);
        when(partMapper.selectFixedCharPartList()).thenReturn(Arrays.asList(
                myPart, fixedPart(1L, 1L, 1, "WO_", 3)));
        when(ruleMapper.selectList()).thenReturn(Arrays.asList(
                new MesMdAutoCodeRuleDO().setId(1L), new MesMdAutoCodeRuleDO().setId(10L)));

        // 断言：修改后前缀不再重复，保存成功
        partService.updateAutoCodePart(fixedReqVO(10L, 1, "SN_", 3).setId(100L));
        verify(partMapper).updateById(org.mockito.ArgumentMatchers.<MesMdAutoCodePartDO>any());
    }

}
