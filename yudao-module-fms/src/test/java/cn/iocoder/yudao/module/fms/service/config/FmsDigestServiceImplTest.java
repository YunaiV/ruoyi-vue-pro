package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest.FmsDigestSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsDigestDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsDigestMapper;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.DIGEST_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Import(FmsDigestServiceImpl.class)
public class FmsDigestServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 10L;

    @Resource
    private FmsDigestServiceImpl digestService;
    @Resource
    private FmsDigestMapper digestMapper;

    @MockitoBean
    private FmsAccountSetService accountSetService;

    @Test
    public void testCreateDigest_success() {
        // 准备参数
        FmsDigestSaveReqVO reqVO = buildSaveReqVO(null, 1L, "购买办公用品");

        // 调用
        Long digestId = digestService.createDigest(reqVO, USER_ID);

        // 断言
        FmsDigestDO digest = digestMapper.selectById(digestId);
        assertEquals("购买办公用品", digest.getContent());
        assertEquals(1L, digest.getAccountSetId());
        verify(accountSetService).validateAccountSetWritePermission(1L, USER_ID);
    }

    @Test
    public void testUpdateDigest_success() {
        // mock 数据
        FmsDigestDO digest = new FmsDigestDO().setAccountSetId(1L).setContent("购买办公用品");
        digestMapper.insert(digest);
        // 准备参数
        FmsDigestSaveReqVO reqVO = buildSaveReqVO(digest.getId(), 1L, "支付办公用品款");

        // 调用
        digestService.updateDigest(reqVO, USER_ID);

        // 断言
        assertEquals("支付办公用品款", digestMapper.selectById(digest.getId()).getContent());
        verify(accountSetService).validateAccountSetWritePermission(1L, USER_ID);
    }

    @Test
    public void testUpdateDigest_wrongAccountSet() {
        // mock 数据
        FmsDigestDO digest = new FmsDigestDO().setAccountSetId(1L).setContent("购买办公用品");
        digestMapper.insert(digest);
        // 准备参数
        FmsDigestSaveReqVO reqVO = buildSaveReqVO(digest.getId(), 2L, "支付办公用品款");

        // 调用，并断言
        assertServiceException(() -> digestService.updateDigest(reqVO, USER_ID), DIGEST_NOT_EXISTS);
        verify(accountSetService).validateAccountSetWritePermission(2L, USER_ID);
    }

    @Test
    public void testDeleteDigest_success() {
        // mock 数据
        FmsDigestDO digest = new FmsDigestDO().setAccountSetId(1L).setContent("购买办公用品");
        digestMapper.insert(digest);

        // 调用
        digestService.deleteDigest(1L, digest.getId(), USER_ID);

        // 断言
        assertNull(digestMapper.selectById(digest.getId()));
        verify(accountSetService).validateAccountSetWritePermission(1L, USER_ID);
    }

    @Test
    public void testGetDigestList_accountSetAndSort() {
        // mock 数据
        FmsDigestDO first = new FmsDigestDO().setAccountSetId(1L).setContent("购买办公用品");
        digestMapper.insert(first);
        FmsDigestDO second = new FmsDigestDO().setAccountSetId(1L).setContent("支付员工报销款");
        digestMapper.insert(second);
        digestMapper.insert(new FmsDigestDO().setAccountSetId(2L).setContent("其他账套摘要"));

        // 调用
        List<FmsDigestDO> list = digestService.getDigestList(1L, USER_ID);

        // 断言
        assertEquals(2, list.size());
        assertEquals(second.getId(), CollUtil.getFirst(list).getId());
        verify(accountSetService).validateAccountSetReadPermission(1L, USER_ID);
    }

    // ========== 随机对象 ==========

    private FmsDigestSaveReqVO buildSaveReqVO(Long id, Long accountSetId, String content) {
        FmsDigestSaveReqVO reqVO = new FmsDigestSaveReqVO();
        reqVO.setId(id);
        reqVO.setAccountSetId(accountSetId);
        reqVO.setContent(content);
        return reqVO;
    }

}
