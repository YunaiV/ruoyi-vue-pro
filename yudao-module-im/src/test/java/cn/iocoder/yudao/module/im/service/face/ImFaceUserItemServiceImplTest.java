package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFaceUserItemMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_NOT_OWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IM 用户私有表情 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImFaceUserItemServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImFaceUserItemServiceImpl service;

    @Mock
    private ImFaceUserItemMapper faceUserItemMapper;

    // ========== createFaceUserItem ==========

    @Test
    public void testCreateFaceUserItem_success() {
        // 准备
        ImFaceUserItemSaveReqVO reqVO = new ImFaceUserItemSaveReqVO();
        reqVO.setUrl("a.png").setName("doge").setWidth(100).setHeight(100);
        when(faceUserItemMapper.selectByUserIdAndUrl(1L, "a.png")).thenReturn(null);

        // 调用
        service.createFaceUserItem(1L, reqVO);

        // 断言：写入用户编号
        ArgumentCaptor<ImFaceUserItemDO> captor = ArgumentCaptor.forClass(ImFaceUserItemDO.class);
        verify(faceUserItemMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals("a.png", captor.getValue().getUrl());
    }

    @Test
    public void testCreateFaceUserItem_duplicateUrl() {
        // 准备：相同 (userId, url) 已存在
        ImFaceUserItemSaveReqVO reqVO = new ImFaceUserItemSaveReqVO();
        reqVO.setUrl("a.png").setWidth(100).setHeight(100);
        when(faceUserItemMapper.selectByUserIdAndUrl(1L, "a.png"))
                .thenReturn(new ImFaceUserItemDO().setUserId(1L));

        // 调用 + 断言：抛重复异常，不落库
        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.createFaceUserItem(1L, reqVO));
        assertEquals(FACE_USER_ITEM_DUPLICATED.getCode(), exception.getCode());
        verify(faceUserItemMapper, never()).insert(any(ImFaceUserItemDO.class));
    }

    // ========== deleteFaceUserItem（用户端） ==========

    @Test
    public void testDeleteFaceUserItem_userOwn_success() {
        // 准备：归属当前用户
        when(faceUserItemMapper.selectById(10L)).thenReturn(new ImFaceUserItemDO().setUserId(1L));

        // 调用
        service.deleteFaceUserItem(1L, 10L);

        // 断言
        verify(faceUserItemMapper).deleteById(10L);
    }

    @Test
    public void testDeleteFaceUserItem_notExists() {
        when(faceUserItemMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.deleteFaceUserItem(1L, 99L));
        assertEquals(FACE_USER_ITEM_NOT_EXISTS.getCode(), exception.getCode());
        verify(faceUserItemMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteFaceUserItem_notOwn() {
        // 准备：表情归属 user 2，但 user 1 来删
        when(faceUserItemMapper.selectById(10L)).thenReturn(new ImFaceUserItemDO().setUserId(2L));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.deleteFaceUserItem(1L, 10L));
        assertEquals(FACE_USER_ITEM_NOT_OWN.getCode(), exception.getCode());
        verify(faceUserItemMapper, never()).deleteById(anyLong());
    }

    // ========== deleteFaceUserItem（管理后台 by id） ==========

    @Test
    public void testDeleteFaceUserItemAdmin_success() {
        when(faceUserItemMapper.selectById(10L)).thenReturn(new ImFaceUserItemDO().setUserId(2L));

        service.deleteFaceUserItem(10L);

        verify(faceUserItemMapper).deleteById(10L);
    }

    @Test
    public void testDeleteFaceUserItemAdmin_notExists() {
        when(faceUserItemMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.deleteFaceUserItem(99L));
        assertEquals(FACE_USER_ITEM_NOT_EXISTS.getCode(), exception.getCode());
    }

}
