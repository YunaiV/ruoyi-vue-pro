package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptDebugReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptDebugRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotProductServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class IotProductServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotProductServiceImpl productService;

    @Mock
    private IotProductMapper productMapper;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyDataService;

    @Test
    public void testDeleteProduct_success() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);

        // 调用
        productService.deleteProduct(id);
        // 断言：先删除属性表，再删除 MySQL 产品
        InOrder inOrder = inOrder(devicePropertyDataService, productMapper);
        inOrder.verify(devicePropertyDataService).deleteDevicePropertyData(id);
        inOrder.verify(productMapper).deleteById(id);
    }

    @Test
    public void testDeleteProduct_notExists() {
        // 准备参数
        Long id = 1L;

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_NOT_EXISTS);
        // 断言：不删除属性表、产品
        assertNoDelete();
    }

    @Test
    public void testDeleteProduct_published() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.PUBLISHED.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_STATUS_NOT_DELETE);
        // 断言：不删除属性表、产品
        assertNoDelete();
        verifyNoInteractions(deviceService);
    }

    @Test
    public void testDeleteProduct_hasDevice() {
        // 准备参数
        Long id = 1L;
        // mock 方法
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> productService.deleteProduct(id), PRODUCT_DELETE_FAIL_HAS_DEVICE);
        // 断言：不删除属性表、产品
        assertNoDelete();
    }

    @Test
    public void testDeleteProduct_deletePropertyTableFailed() {
        // 准备参数
        Long id = 1L;
        // mock 方法：属性表删除失败
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);
        RuntimeException failure = new RuntimeException("drop property table failed");
        doThrow(failure).when(devicePropertyDataService).deleteDevicePropertyData(id);

        // 调用，并断言异常
        assertSame(failure, assertThrows(RuntimeException.class, () -> productService.deleteProduct(id)));
        // 断言：保留 MySQL 产品，便于重试
        verify(productMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    public void testDeleteProduct_deleteProductFailedThenRetry() {
        // 准备参数
        Long id = 1L;
        // mock 方法：第一次 MySQL 删除失败，第二次成功
        when(productMapper.selectById(id)).thenReturn(buildProduct(id, IotProductStatusEnum.UNPUBLISHED.getStatus()));
        when(deviceService.getDeviceCountByProductId(id)).thenReturn(0L);
        RuntimeException failure = new RuntimeException("delete product failed");
        when(productMapper.deleteById(id)).thenThrow(failure).thenReturn(1);

        // 调用：第一次失败
        assertSame(failure, assertThrows(RuntimeException.class, () -> productService.deleteProduct(id)));
        // 调用：第二次重试成功
        productService.deleteProduct(id);
        // 断言：属性表删除是幂等的，重试时再次调用
        verify(devicePropertyDataService, times(2)).deleteDevicePropertyData(id);
        verify(productMapper, times(2)).deleteById(id);
    }

    @Test
    public void testUpdateProduct_notOverwriteScript() {
        // 准备参数：普通产品表单不含脚本字段
        IotProductSaveReqVO reqVO = new IotProductSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("温湿度");
        // mock 方法
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, IotProductStatusEnum.UNPUBLISHED.getStatus()));

        // 调用
        productService.updateProduct(reqVO);
        // 断言：脚本两列为 null，按默认 NOT_NULL 策略不会写回
        IotProductDO updateObj = captureUpdate();
        assertEquals("温湿度", updateObj.getName());
        assertNull(updateObj.getScriptContent());
        assertNull(updateObj.getScriptEnabled());
    }

    @Test
    public void testUpdateProductScript_contentOnly() {
        // 准备参数
        productService.initDebugScriptExecutor();
        try {
            IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
            reqVO.setId(1L);
            reqVO.setScriptContent("function decode(ctx) { return {method: 'thing.property.post', params: {}}; }");
            // mock 方法：已启用旧脚本
            when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null).setScriptEnabled(true).setScriptContent("old"));

            // 调用
            productService.updateProductScript(reqVO);
            // 断言：只写内容，不顺带改启用状态
            IotProductDO updateObj = captureUpdate();
            assertEquals(reqVO.getScriptContent(), updateObj.getScriptContent());
            assertNull(updateObj.getScriptEnabled());
            assertNull(updateObj.getName());
        } finally {
            productService.destroyDebugScriptExecutor();
        }
    }

    @Test
    public void testUpdateProductScript_enableOnly() {
        // 准备参数
        IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setScriptEnabled(true);
        // mock 方法：库里已有内容
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null).setScriptContent("function decode(ctx) {}"));

        // 调用
        productService.updateProductScript(reqVO);
        // 断言：只写启用状态
        IotProductDO updateObj = captureUpdate();
        assertEquals(true, updateObj.getScriptEnabled());
        assertNull(updateObj.getScriptContent());
    }

    @Test
    public void testUpdateProductScript_enableWithEmptyContent() {
        // 准备参数
        IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setScriptEnabled(true);
        // mock 方法：库里没有内容
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null));

        // 调用，并断言异常
        assertServiceException(() -> productService.updateProductScript(reqVO), PRODUCT_SCRIPT_EMPTY);
        verify(productMapper, never()).updateById(any(IotProductDO.class));
    }

    @Test
    public void testUpdateProductScript_clearWhileEnabled() {
        // 准备参数：启用状态下清空内容
        IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setScriptContent("");
        // mock 方法
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null).setScriptEnabled(true).setScriptContent("x"));

        // 调用，并断言异常
        assertServiceException(() -> productService.updateProductScript(reqVO), PRODUCT_SCRIPT_EMPTY);
        verify(productMapper, never()).updateById(any(IotProductDO.class));
    }

    @Test
    public void testUpdateProductScript_tooLong() {
        // 准备参数：超过 32KB（UTF-8）
        IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setScriptContent(StrUtil.repeat('a', 32 * 1024 + 1));
        // mock 方法
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null));

        // 调用，并断言异常
        assertServiceException(() -> productService.updateProductScript(reqVO), PRODUCT_SCRIPT_TOO_LONG, 32 * 1024);
        verify(productMapper, never()).updateById(any(IotProductDO.class));
    }

    @Test
    public void testUpdateProductScript_compileFailed() {
        // 准备参数
        productService.initDebugScriptExecutor();
        try {
            IotProductScriptUpdateReqVO reqVO = new IotProductScriptUpdateReqVO();
            reqVO.setId(1L);
            reqVO.setScriptContent("function decode(ctx) {\n  return {;\n}");
            // mock 方法
            when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null));

            // 调用，并断言异常：只编译、不执行，语法错误带行号
            ServiceException ex = assertThrows(ServiceException.class, () -> productService.updateProductScript(reqVO));
            assertEquals(PRODUCT_SCRIPT_COMPILE_FAILED.getCode(), ex.getCode());
            verify(productMapper, never()).updateById(any(IotProductDO.class));
        } finally {
            productService.destroyDebugScriptExecutor();
        }
    }

    @Test
    public void testDebugProductScript_decodeMessage() {
        // 准备参数：decode 返回一个标准消息对象
        IotProductScriptDebugReqVO reqVO = buildDecodeDebugReqVO(
                "function decode(ctx) { return {method: 'thing.property.post', params: {temperature: ctx.bytes[0]}}; }");

        // 调用
        IotProductScriptDebugRespVO respVO = debugProductScript(reqVO);
        // 断言：单条消息
        assertTrue(respVO.getSuccess());
        assertNull(respVO.getError());
        assertNotNull(respVO.getMessage());
        assertEquals("thing.property.post", respVO.getMessage().get("method"));
        assertEquals(25, ((Map<?, ?>) respVO.getMessage().get("params")).get("temperature"));
    }

    @Test
    public void testDebugProductScript_decodeNull() {
        // 准备参数：decode 返回 null，表示没有业务消息
        IotProductScriptDebugReqVO reqVO = buildDecodeDebugReqVO("function decode(ctx) { return null; }");

        // 调用
        IotProductScriptDebugRespVO respVO = debugProductScript(reqVO);
        // 断言：成功，没有消息
        assertTrue(respVO.getSuccess());
        assertNull(respVO.getError());
        assertNull(respVO.getMessage());
    }

    @Test
    public void testDebugProductScript_decodeOutputInvalid() {
        // 准备参数：旧的 messages 包装，不再支持
        IotProductScriptDebugReqVO reqVO = buildDecodeDebugReqVO(
                "function decode(ctx) { return {messages: [{method: 'thing.property.post', params: {}}]}; }");

        // 调用
        IotProductScriptDebugRespVO respVO = debugProductScript(reqVO);
        // 断言：输出不合法
        assertFalse(respVO.getSuccess());
        assertNull(respVO.getMessage());
        assertEquals("OUTPUT_INVALID", respVO.getError().getType());
    }

    @Test
    public void testDebugProductScript_encodeModbusPduInvalid() {
        // 准备参数：Modbus 产品，encode 输出的字节不是合法的请求 PDU（缺数量）；合法的 06 写单个寄存器
        IotProductScriptDebugReqVO invalidReqVO = buildEncodeDebugReqVO("function encode(ctx) { return {bytes: [6, 0, 16]}; }");
        IotProductScriptDebugReqVO validReqVO = buildEncodeDebugReqVO(
                "function encode(ctx) { return {bytes: [6, 0, 16, 0, ctx.message.params.power]}; }");

        // 调用
        IotProductScriptDebugRespVO invalidRespVO = debugProductScript(invalidReqVO, "modbus_tcp_client");
        IotProductScriptDebugRespVO validRespVO = debugProductScript(validReqVO, "modbus_tcp_client");
        // 断言：与网关一致，按产品协议的字节规则判定输出不合法（带原规则的错误信息）；合法的 PDU 照常输出
        assertFalse(invalidRespVO.getSuccess());
        assertEquals("OUTPUT_INVALID", invalidRespVO.getError().getType());
        assertEquals("PDU 长度必须在 5~253 字节之间", invalidRespVO.getError().getMessage());
        assertTrue(validRespVO.getSuccess());
        assertEquals("0600100001", validRespVO.getOutputHex());
    }

    private IotProductScriptDebugRespVO debugProductScript(IotProductScriptDebugReqVO reqVO) {
        return debugProductScript(reqVO, "tcp");
    }

    /**
     * 执行调试：真实的调试池；mock 产品
     */
    private IotProductScriptDebugRespVO debugProductScript(IotProductScriptDebugReqVO reqVO, String protocolType) {
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, null).setProtocolType(protocolType).setProductKey("pk"));
        productService.initDebugScriptExecutor();
        try {
            return productService.debugProductScript(reqVO);
        } finally {
            productService.destroyDebugScriptExecutor();
        }
    }

    private static IotProductScriptDebugReqVO buildDecodeDebugReqVO(String scriptContent) {
        IotProductScriptDebugReqVO reqVO = new IotProductScriptDebugReqVO();
        reqVO.setId(1L);
        reqVO.setScriptContent(scriptContent);
        reqVO.setDirection("decode");
        reqVO.setInputHex("19 01");
        return reqVO;
    }

    private static IotProductScriptDebugReqVO buildEncodeDebugReqVO(String scriptContent) {
        IotProductScriptDebugReqVO reqVO = new IotProductScriptDebugReqVO();
        reqVO.setId(1L);
        reqVO.setScriptContent(scriptContent);
        reqVO.setDirection("encode");
        Map<String, Object> message = new HashMap<>();
        message.put("method", "thing.property.set");
        message.put("params", Collections.singletonMap("power", 1));
        reqVO.setMessage(message);
        return reqVO;
    }

    private IotProductDO captureUpdate() {
        ArgumentCaptor<IotProductDO> captor = ArgumentCaptor.forClass(IotProductDO.class);
        verify(productMapper).updateById(captor.capture());
        return captor.getValue();
    }

    private void assertNoDelete() {
        verifyNoInteractions(devicePropertyDataService);
        verify(productMapper, never()).deleteById(any(Serializable.class));
    }

    private static IotProductDO buildProduct(Long id, Integer status) {
        return IotProductDO.builder().id(id).status(status).build();
    }

}
