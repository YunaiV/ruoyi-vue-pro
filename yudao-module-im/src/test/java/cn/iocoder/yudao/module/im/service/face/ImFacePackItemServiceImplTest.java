package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFacePackItemMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ImFacePackItemServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class ImFacePackItemServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImFacePackItemServiceImpl service;

    @Mock
    private ImFacePackItemMapper facePackItemMapper;
    @Mock
    private ImFacePackService facePackService;

    // ========== getEnabledItemListByPackIds ==========

    @Test
    public void testGetEnabledItemListByPackIds_emptyShortReturn() {
        // 调用 + 断言：空入参直接返回空列表，不查 DB
        assertTrue(service.getEnabledItemListByPackIds(Collections.emptyList()).isEmpty());
        verify(facePackItemMapper, never()).selectListByPackIdsAndStatus(any(), any());
    }

    @Test
    public void testGetEnabledItemListByPackIds_passesEnableStatus() {
        // 准备
        List<Long> packIds = Arrays.asList(1L, 2L);
        ImFacePackItemDO item = new ImFacePackItemDO();
        when(facePackItemMapper.selectListByPackIdsAndStatus(packIds, CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(item));

        // 调用 + 断言
        assertEquals(1, service.getEnabledItemListByPackIds(packIds).size());
    }

    // ========== createFacePackItem ==========

    @Test
    public void testCreateFacePackItem_success() {
        // 准备
        ImFacePackItemSaveReqVO reqVO = new ImFacePackItemSaveReqVO();
        reqVO.setPackId(10L).setUrl("a.png").setName("dog").setWidth(100).setHeight(100)
                .setSort(0).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackService.validateFacePackExists(10L)).thenReturn(new ImFacePackDO());

        // 调用
        service.createFacePackItem(reqVO);

        // 断言
        ArgumentCaptor<ImFacePackItemDO> captor = ArgumentCaptor.forClass(ImFacePackItemDO.class);
        verify(facePackItemMapper).insert(captor.capture());
        assertEquals(10L, captor.getValue().getPackId());
        assertEquals("a.png", captor.getValue().getUrl());
    }

    @Test
    public void testCreateFacePackItem_packNotExists() {
        // 准备：所属表情包不存在
        ImFacePackItemSaveReqVO reqVO = new ImFacePackItemSaveReqVO();
        reqVO.setPackId(99L).setUrl("a.png").setWidth(100).setHeight(100).setSort(0)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackService.validateFacePackExists(99L))
                .thenThrow(new ServiceException(FACE_PACK_NOT_EXISTS.getCode(), FACE_PACK_NOT_EXISTS.getMsg()));

        // 调用 + 断言：不落库
        assertThrows(ServiceException.class, () -> service.createFacePackItem(reqVO));
        verify(facePackItemMapper, never()).insert(any(ImFacePackItemDO.class));
    }

    // ========== updateFacePackItem ==========

    @Test
    public void testUpdateFacePackItem_success() {
        // 准备
        ImFacePackItemSaveReqVO reqVO = new ImFacePackItemSaveReqVO();
        reqVO.setId(1L).setPackId(10L).setUrl("a.png").setWidth(100).setHeight(100)
                .setSort(0).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackItemMapper.selectById(1L)).thenReturn(new ImFacePackItemDO());
        when(facePackService.validateFacePackExists(10L)).thenReturn(new ImFacePackDO());

        // 调用
        service.updateFacePackItem(reqVO);

        // 断言
        verify(facePackItemMapper).updateById(any(ImFacePackItemDO.class));
    }

    @Test
    public void testUpdateFacePackItem_itemNotExists() {
        ImFacePackItemSaveReqVO reqVO = new ImFacePackItemSaveReqVO();
        reqVO.setId(99L).setPackId(10L).setUrl("a.png").setWidth(100).setHeight(100).setSort(0)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackItemMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateFacePackItem(reqVO));
        assertEquals(FACE_PACK_ITEM_NOT_EXISTS.getCode(), exception.getCode());
        verify(facePackItemMapper, never()).updateById(any(ImFacePackItemDO.class));
    }

    // ========== deleteFacePackItem ==========

    @Test
    public void testDeleteFacePackItem_success() {
        when(facePackItemMapper.selectById(1L)).thenReturn(new ImFacePackItemDO());

        service.deleteFacePackItem(1L);

        verify(facePackItemMapper).deleteById(1L);
    }

    @Test
    public void testDeleteFacePackItem_notExists() {
        when(facePackItemMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.deleteFacePackItem(99L));
        assertEquals(FACE_PACK_ITEM_NOT_EXISTS.getCode(), exception.getCode());
        verify(facePackItemMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteFacePackItemList_emptySkip() {
        service.deleteFacePackItemList(Collections.emptyList());

        verify(facePackItemMapper, never()).deleteByIds(any());
    }

    @Test
    public void testDeleteFacePackItemList_success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(facePackItemMapper.deleteByIds(ids)).thenReturn(2);

        service.deleteFacePackItemList(ids);

        verify(facePackItemMapper).deleteByIds(ids);
    }

    @Test
    public void testDeleteFacePackItemList_ignoreMissingIds() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(facePackItemMapper.deleteByIds(ids)).thenReturn(1);

        service.deleteFacePackItemList(ids);

        verify(facePackItemMapper).deleteByIds(ids);
    }

}
