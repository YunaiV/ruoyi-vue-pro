package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFacePackMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_HAS_ITEMS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IM 表情包 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImFacePackServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImFacePackServiceImpl service;

    @Mock
    private ImFacePackMapper facePackMapper;
    @Mock
    private ImFacePackItemService facePackItemService;

    // ========== validateFacePackExists ==========

    @Test
    public void testValidateFacePackExists_notFound() {
        when(facePackMapper.selectById(1L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.validateFacePackExists(1L));
        assertEquals(FACE_PACK_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testValidateFacePackExists_returnsDo() {
        ImFacePackDO pack = ImFacePackDO.builder().id(1L).name("猫主子").build();
        when(facePackMapper.selectById(1L)).thenReturn(pack);

        assertEquals(pack, service.validateFacePackExists(1L));
    }

    // ========== createFacePack ==========

    @Test
    public void testCreateFacePack_insert() {
        // 准备
        ImFacePackSaveReqVO reqVO = new ImFacePackSaveReqVO();
        reqVO.setName("猫主子").setIcon("icon.png").setSort(0).setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        service.createFacePack(reqVO);

        // 断言：mapper.insert 被调用一次
        ArgumentCaptor<ImFacePackDO> captor = ArgumentCaptor.forClass(ImFacePackDO.class);
        verify(facePackMapper).insert(captor.capture());
        assertEquals("猫主子", captor.getValue().getName());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
    }

    // ========== updateFacePack ==========

    @Test
    public void testUpdateFacePack_success() {
        // 准备：存在
        ImFacePackSaveReqVO reqVO = new ImFacePackSaveReqVO();
        reqVO.setId(1L).setName("新名").setSort(1).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackMapper.selectById(1L)).thenReturn(ImFacePackDO.builder().id(1L).build());

        // 调用
        service.updateFacePack(reqVO);

        // 断言
        ArgumentCaptor<ImFacePackDO> captor = ArgumentCaptor.forClass(ImFacePackDO.class);
        verify(facePackMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("新名", captor.getValue().getName());
    }

    @Test
    public void testUpdateFacePack_notExists() {
        // 准备
        ImFacePackSaveReqVO reqVO = new ImFacePackSaveReqVO();
        reqVO.setId(99L).setName("x").setSort(0).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(facePackMapper.selectById(99L)).thenReturn(null);

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateFacePack(reqVO));
        assertEquals(FACE_PACK_NOT_EXISTS.getCode(), exception.getCode());
        verify(facePackMapper, never()).updateById(any(ImFacePackDO.class));
    }

    // ========== deleteFacePack ==========

    @Test
    public void testDeleteFacePack_success() {
        when(facePackMapper.selectById(1L)).thenReturn(ImFacePackDO.builder().id(1L).build());
        when(facePackItemService.getFacePackItemCount(1L)).thenReturn(0L);

        service.deleteFacePack(1L);

        verify(facePackMapper).deleteById(1L);
    }

    @Test
    public void testDeleteFacePack_hasItems() {
        // 准备：包下仍有表情
        when(facePackMapper.selectById(1L)).thenReturn(ImFacePackDO.builder().id(1L).build());
        when(facePackItemService.getFacePackItemCount(1L)).thenReturn(3L);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteFacePack(1L));
        assertEquals(FACE_PACK_HAS_ITEMS.getCode(), exception.getCode());
        verify(facePackMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteFacePack_notExists() {
        when(facePackMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteFacePack(99L));
        assertEquals(FACE_PACK_NOT_EXISTS.getCode(), exception.getCode());
    }

    // ========== deleteFacePackList ==========

    @Test
    public void testDeleteFacePackList_emptySkip() {
        // 调用：空 ids 列表直接返回，不查询、不删除
        service.deleteFacePackList(Collections.emptyList());

        verify(facePackItemService, never()).getFacePackItemCount(any(java.util.Collection.class));
        verify(facePackMapper, never()).deleteByIds(any());
    }

    @Test
    public void testDeleteFacePackList_anyHasItemsRejectAll() {
        // 准备：批量中存在表情
        when(facePackItemService.getFacePackItemCount(Arrays.asList(1L, 2L))).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.deleteFacePackList(Arrays.asList(1L, 2L)));
        assertEquals(FACE_PACK_HAS_ITEMS.getCode(), exception.getCode());
        verify(facePackMapper, never()).deleteByIds(any());
    }

    @Test
    public void testDeleteFacePackList_success() {
        // 准备
        when(facePackItemService.getFacePackItemCount(Arrays.asList(1L, 2L))).thenReturn(0L);

        // 调用
        service.deleteFacePackList(Arrays.asList(1L, 2L));

        // 断言
        verify(facePackMapper).deleteByIds(Arrays.asList(1L, 2L));
    }

}
