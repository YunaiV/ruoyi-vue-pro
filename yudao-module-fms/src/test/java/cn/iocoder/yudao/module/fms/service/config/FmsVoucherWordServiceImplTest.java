package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword.FmsVoucherWordSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherWordMapper;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import(FmsVoucherWordServiceImpl.class)
public class FmsVoucherWordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsVoucherWordServiceImpl voucherWordService;
    @Resource
    private FmsVoucherWordMapper voucherWordMapper;
    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsVoucherService voucherService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;

    @Test
    public void testCreateVoucherWord_success() {
        // mock 数据
        voucherWordMapper.insert(buildVoucherWord(1L, "记", true, 1));
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(null, 1L, "转", "转账凭证", false);

        // 调用
        Long voucherWordId = voucherWordService.createVoucherWord(reqVO, 10L);

        // 断言
        FmsVoucherWordDO voucherWord = voucherWordMapper.selectById(voucherWordId);
        assertEquals("转", voucherWord.getName());
        assertEquals("转账凭证", voucherWord.getPrintTitle());
        assertFalse(voucherWord.getDefaultStatus());
        assertEquals(2, voucherWord.getSort());
    }

    @Test
    public void testCreateVoucherWord_firstWordDefault() {
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(null, 1L, "记", "记账凭证", false);

        // 调用
        Long voucherWordId = voucherWordService.createVoucherWord(reqVO, 10L);

        // 断言
        assertTrue(voucherWordMapper.selectById(voucherWordId).getDefaultStatus());
    }

    @Test
    public void testCreateVoucherWord_nameDuplicate() {
        // mock 数据
        voucherWordMapper.insert(buildVoucherWord(1L, "记", true, 1));
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(null, 1L, "记", "记账凭证", false);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.createVoucherWord(reqVO, 10L),
                VOUCHER_WORD_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateVoucherWord_switchDefault() {
        // mock 数据
        FmsVoucherWordDO first = buildVoucherWord(1L, "记", true, 1);
        voucherWordMapper.insert(first);
        FmsVoucherWordDO second = buildVoucherWord(1L, "收", false, 2);
        voucherWordMapper.insert(second);
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(
                second.getId(), 1L, "收", "收款凭证", true);

        // 调用
        voucherWordService.updateVoucherWord(reqVO, 10L);

        // 断言
        assertFalse(voucherWordMapper.selectById(first.getId()).getDefaultStatus());
        assertTrue(voucherWordMapper.selectById(second.getId()).getDefaultStatus());
    }

    @Test
    public void testUpdateVoucherWord_defaultRequired() {
        // mock 数据
        FmsVoucherWordDO voucherWord = buildVoucherWord(1L, "记", true, 1);
        voucherWordMapper.insert(voucherWord);
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(
                voucherWord.getId(), 1L, "记", "记账凭证", false);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.updateVoucherWord(reqVO, 10L),
                VOUCHER_WORD_DEFAULT_REQUIRED);
    }

    @Test
    public void testUpdateVoucherWord_inUse() {
        // mock 数据
        FmsVoucherWordDO voucherWord = buildVoucherWord(1L, "记", true, 1);
        voucherWordMapper.insert(voucherWord);
        when(voucherService.getVoucherCountByVoucherWordId(1L, voucherWord.getId())).thenReturn(1L);
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(
                voucherWord.getId(), 1L, "记", "通用记账凭证", true);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.updateVoucherWord(reqVO, 10L),
                VOUCHER_WORD_IN_USE_NOT_EDITABLE);
    }

    @Test
    public void testUpdateVoucherWord_inUseSwitchDefault() {
        // mock 数据
        FmsVoucherWordDO first = buildVoucherWord(1L, "记", false, 1);
        voucherWordMapper.insert(first);
        FmsVoucherWordDO second = buildVoucherWord(1L, "收", true, 2);
        voucherWordMapper.insert(second);
        when(voucherService.getVoucherCountByVoucherWordId(1L, first.getId())).thenReturn(1L);
        // 准备参数
        FmsVoucherWordSaveReqVO reqVO = buildSaveReqVO(
                first.getId(), 1L, first.getName(), first.getPrintTitle(), true);

        // 调用
        voucherWordService.updateVoucherWord(reqVO, 10L);

        // 断言
        assertTrue(voucherWordMapper.selectById(first.getId()).getDefaultStatus());
        assertFalse(voucherWordMapper.selectById(second.getId()).getDefaultStatus());
    }

    @Test
    public void testDeleteVoucherWord_defaultStatus() {
        // mock 数据
        FmsVoucherWordDO voucherWord = buildVoucherWord(1L, "记", true, 1);
        voucherWordMapper.insert(voucherWord);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.deleteVoucherWord(1L, voucherWord.getId(), 10L),
                VOUCHER_WORD_DEFAULT_NOT_DELETABLE);
    }

    @Test
    public void testDeleteVoucherWord_inUse() {
        // mock 数据
        FmsVoucherWordDO voucherWord = buildVoucherWord(1L, "转", false, 2);
        voucherWordMapper.insert(voucherWord);
        when(voucherService.getVoucherCountByVoucherWordId(1L, voucherWord.getId())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.deleteVoucherWord(1L, voucherWord.getId(), 10L),
                VOUCHER_WORD_IN_USE_NOT_DELETABLE, 1L);
    }

    @Test
    public void testDeleteVoucherWord_closingInUse() {
        // mock 数据
        FmsVoucherWordDO voucherWord = buildVoucherWord(1L, "转", false, 2);
        voucherWordMapper.insert(voucherWord);
        // mock 方法
        when(closingSchemeService.getClosingSchemeCountByVoucherWordId(1L, voucherWord.getId())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> voucherWordService.deleteVoucherWord(1L, voucherWord.getId(), 10L),
                VOUCHER_WORD_IN_USE_BY_CLOSING_SCHEME_NOT_DELETABLE, 1L);
    }

    @Test
    public void testInitializeDefaultVoucherWords_success() {
        // 准备参数
        Long accountSetId = randomLongId();

        // 调用
        voucherWordService.initializeDefaultVoucherWords(accountSetId);

        // 断言
        List<FmsVoucherWordDO> voucherWords = voucherWordMapper.selectListByAccountSetId(accountSetId);
        assertEquals(4, voucherWords.size());
        assertEquals("记", voucherWords.get(0).getName());
        assertEquals("记账凭证", voucherWords.get(0).getPrintTitle());
        assertTrue(voucherWords.get(0).getDefaultStatus());
        assertEquals("收", voucherWords.get(1).getName());
        assertEquals("收款凭证", voucherWords.get(1).getPrintTitle());
        assertFalse(voucherWords.get(1).getDefaultStatus());
        assertEquals("转", voucherWords.get(2).getName());
        assertEquals("转账凭证", voucherWords.get(2).getPrintTitle());
        assertFalse(voucherWords.get(2).getDefaultStatus());
        assertEquals("付", voucherWords.get(3).getName());
        assertEquals("付款凭证", voucherWords.get(3).getPrintTitle());
        assertFalse(voucherWords.get(3).getDefaultStatus());
    }

    // ========== 随机对象 ==========

    private FmsVoucherWordSaveReqVO buildSaveReqVO(Long id, Long accountSetId, String name,
            String printTitle, boolean defaultStatus) {
        return randomPojo(FmsVoucherWordSaveReqVO.class, reqVO -> reqVO.setId(id)
                .setAccountSetId(accountSetId).setName(name).setPrintTitle(printTitle)
                .setDefaultStatus(defaultStatus));
    }

    private FmsVoucherWordDO buildVoucherWord(
            Long accountSetId, String name, boolean defaultStatus, int sort) {
        return randomPojo(FmsVoucherWordDO.class, voucherWord -> voucherWord.setId(null)
                .setAccountSetId(accountSetId).setName(name).setPrintTitle(name + "账凭证")
                .setDefaultStatus(defaultStatus).setSort(sort));
    }

}
