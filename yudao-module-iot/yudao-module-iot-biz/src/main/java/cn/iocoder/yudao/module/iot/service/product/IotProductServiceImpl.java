package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptDebugReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptDebugRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductScriptUpdateReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.IotScriptException;
import cn.iocoder.yudao.module.iot.core.script.IotScriptExecutor;
import cn.iocoder.yudao.module.iot.core.script.IotScriptUtils;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.core.script.rhino.IotRhinoScriptExecutor;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 产品 Service 实现类
 *
 * @author ahh
 */
@Slf4j
@Service
@Validated
public class IotProductServiceImpl implements IotProductService {

    /**
     * 数据解析脚本的最大字节数（UTF-8）
     */
    private static final int SCRIPT_MAX_BYTES = 32 * 1024;
    /**
     * 调试时 ctx.deviceName 的固定值：ctx 中不带任何真实设备的信息或秘密
     */
    private static final String DEBUG_DEVICE_NAME = "debug-device";
    /**
     * 调试的截止时间：覆盖排队与执行，也是请求线程的最长等待时间
     */
    private static final long DEBUG_DEADLINE_MILLIS = 2000;
    /**
     * 保存时语法检查的最长等待时间
     */
    private static final long COMPILE_TIMEOUT_MILLIS = 2000;

    @Resource
    private IotProductMapper productMapper;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotDevicePropertyService devicePropertyDataService;

    /**
     * 调试池：2 个线程、队列 4；请求线程阻塞等待结果，最长到截止时间
     * <p>
     * Rhino 下死循环会被预算中断、线程可复用；不经过检查点的原生长操作仍可能卡住线程 → 熔断调试功能，需要重启 biz
     */
    private IotScriptExecutor debugScriptExecutor;

    @PostConstruct
    public void initDebugScriptExecutor() {
        debugScriptExecutor = new IotRhinoScriptExecutor(new IotRhinoScriptExecutor.Options().setName("iot-script-debug")
                .setPoolSize(2).setQueueCapacity(4).setMaxStuckThreads(2));
    }

    @PreDestroy
    public void destroyDebugScriptExecutor() {
        debugScriptExecutor.shutdown();
    }

    @Override
    public Long createProduct(IotProductSaveReqVO createReqVO) {
        // 1. 校验 ProductKey
        if (productMapper.selectByProductKey(createReqVO.getProductKey()) != null) {
            throw exception(PRODUCT_KEY_EXISTS);
        }

        // 2. 插入
        IotProductDO product = BeanUtils.toBean(createReqVO, IotProductDO.class)
                .setStatus(IotProductStatusEnum.UNPUBLISHED.getStatus())
                .setProductSecret(generateProductSecret());
        productMapper.insert(product);
        return product.getId();
    }

    private String generateProductSecret() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#updateReqVO.id")
    public void updateProduct(IotProductSaveReqVO updateReqVO) {
        updateReqVO.setProductKey(null); // 不更新产品标识
        // 1. 校验存在
        validateProductExists(updateReqVO.getId());

        // 2. 更新
        IotProductDO updateObj = BeanUtils.toBean(updateReqVO, IotProductDO.class);
        productMapper.updateById(updateObj);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void deleteProduct(Long id) {
        // 1.1 校验存在
        IotProductDO product = validateProductExists(id);
        // 1.2 发布状态不可删除
        validateProductStatus(product);
        // 1.3 校验是否有设备
        if (deviceService.getDeviceCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_HAS_DEVICE);
        }

        // 2.1 先删除产品属性表，见 https://github.com/YunaiV/ruoyi-vue-pro/issues/1324
        // 说明：时序库 DDL 与 MySQL 无法同一事务。属性表删除失败时直接抛出、保留产品以便重试；
        //      属性表删除幂等，若后续 MySQL 删除失败，再次删除产品仍可成功
        devicePropertyDataService.deleteDevicePropertyData(id);
        // 2.2 删除产品
        productMapper.deleteById(id);
    }

    @Override
    public IotProductDO validateProductExists(Long id) {
        IotProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    @Override
    public IotProductDO validateProductExists(String productKey) {
        IotProductDO product = productMapper.selectByProductKey(productKey);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    private void validateProductStatus(IotProductDO product) {
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
        }
    }

    @Override
    public IotProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.PRODUCT, key = "#id", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotProductDO getProductFromCache(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public IotProductDO getProductByProductKey(String productKey) {
        return productMapper.selectByProductKey(productKey);
    }

    @Override
    public PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void updateProductStatus(Long id, Integer status) {
        // 1. 校验存在
        validateProductExists(id);

        // 2. 更新为发布状态，需要创建产品超级表数据模型
        // TODO @芋艿：【待定 001】1）是否需要操作后，在 redis 进行缓存，实现一个“快照”的情况，类似 tl；
        if (Objects.equals(status, IotProductStatusEnum.PUBLISHED.getStatus())) {
            devicePropertyDataService.defineDevicePropertyData(id);
        }

        // 3. 更新
        IotProductDO updateObj = IotProductDO.builder().id(id).status(status).build();
        productMapper.updateById(updateObj);
    }

    @Override
    public List<IotProductDO> getProductList() {
        return productMapper.selectList();
    }

    @Override
    public List<IotProductDO> getProductList(Integer deviceType) {
        return productMapper.selectList(deviceType);
    }

    @Override
    public Long getProductCount(LocalDateTime createTime) {
        return productMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public List<IotProductDO> getProductList(Collection<Long> ids) {
        return productMapper.selectByIds(ids);
    }

    @Override
    public void syncProductPropertyTable(Long id) {
        // 1. 校验产品存在
        validateProductExists(id);

        // 2. 同步指定产品的 TDengine 表结构
        devicePropertyDataService.defineDevicePropertyData(id);
    }

    @Override
    public void validateProductsExist(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<IotProductDO> products = productMapper.selectByIds(ids);
        if (products.size() != ids.size()) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    // ========== 数据解析脚本 ==========

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#updateReqVO.id")
    public void updateProductScript(IotProductScriptUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        IotProductDO product = validateProductExists(updateReqVO.getId());
        // 1.2 校验脚本：UTF-8 不超过 32KB；语法只编译、不执行
        if (updateReqVO.getScriptContent() != null) {
            validateProductScript(updateReqVO.getScriptContent());
        }
        // 1.3 启用时内容不能为空：没提交的字段取库里的值
        String content = ObjUtil.defaultIfNull(updateReqVO.getScriptContent(), product.getScriptContent());
        Boolean enabled = ObjUtil.defaultIfNull(updateReqVO.getScriptEnabled(), product.getScriptEnabled());
        if (Boolean.TRUE.equals(enabled) && StrUtil.isBlank(content)) {
            throw exception(PRODUCT_SCRIPT_EMPTY);
        }

        // 2. 部分更新：只写本次提交的字段（null 跳过），保存内容不会顺带改启用状态
        IotProductDO updateObj = BeanUtils.toBean(updateReqVO, IotProductDO.class);
        productMapper.updateById(updateObj);
    }

    private void validateProductScriptLength(String content) {
        if (StrUtil.utf8Bytes(content).length > SCRIPT_MAX_BYTES) {
            throw exception(PRODUCT_SCRIPT_TOO_LONG, SCRIPT_MAX_BYTES);
        }
    }

    private void validateProductScript(String content) {
        validateProductScriptLength(content);
        // 空字符串表示清空
        if (StrUtil.isEmpty(content)) {
            return;
        }
        // 在调试池线程上编译：只做语法检查，不执行任何用户代码
        IotScriptResult result = debugScriptExecutor.compileOnly(content, COMPILE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if (result.getStatus() == IotScriptResult.Status.REJECTED || result.isTimeout()) {
            throw exception(PRODUCT_SCRIPT_DEBUG_BUSY);
        }
        if (!result.isSuccess()) {
            throw exception(PRODUCT_SCRIPT_COMPILE_FAILED, result.getLineNumber() > 0
                    ? StrUtil.format("第 {} 行：{}", result.getLineNumber(), result.getMessage()) : result.getMessage());
        }
    }

    @Override
    public IotProductScriptDebugRespVO debugProductScript(IotProductScriptDebugReqVO debugReqVO) {
        // 1.1 校验当前租户内的产品
        IotProductDO product = validateProductExists(debugReqVO.getId());
        // 1.2 校验脚本长度
        validateProductScriptLength(debugReqVO.getScriptContent());
        // 1.3 校验调试输入，构建 ctx
        String ctx = buildDebugContext(debugReqVO, product);

        // 2.1 执行：与网关相同的运行时和预算；请求线程阻塞等待，最长到截止时间
        IotScriptResult result = debugScriptExecutor.execute(IotScriptSource.ofDebug(debugReqVO.getScriptContent()),
                debugReqVO.getDirection(), ctx, System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEBUG_DEADLINE_MILLIS));
        // 2.2 构建结果：与网关相同的输出校验
        return buildDebugResult(debugReqVO, product, result);
    }

    /**
     * 构建调试的 ctx：与网关的结构一致，但设备名固定为 {@link #DEBUG_DEVICE_NAME}，不带任何真实设备的信息或秘密
     *
     * @param reqVO   调试请求
     * @param product 产品
     * @return ctx（JSON）
     */
    private static String buildDebugContext(IotProductScriptDebugReqVO reqVO, IotProductDO product) {
        // 1. decode：上行原始字节
        if (IotScriptUtils.FUNCTION_DECODE.equals(reqVO.getDirection())) {
            // 1.1 校验 HEX（允许空格）
            String hex = StrUtil.cleanBlank(reqVO.getInputHex());
            if (StrUtil.isEmpty(hex) || hex.length() % 2 != 0 || !ReUtil.isMatch("[0-9a-fA-F]+", hex)) {
                throw invalidParamException("上行数据必须是偶数位的 HEX");
            }
            // 1.2 校验长度：与透传上报的上限一致
            byte[] bytes = HexUtil.decodeHex(hex);
            if (bytes.length > IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES) {
                throw invalidParamException("上行数据不能超过 {} 字节", IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES);
            }
            // 1.3 构建 decode 的 ctx
            return IotScriptUtils.buildDecodeContext(product.getProtocolType(), product.getProductKey(),
                    DEBUG_DEVICE_NAME, System.currentTimeMillis(), bytes, null);
        }

        // 2.1 encode：与网关一致，只编码 property.set / service.invoke 请求
        Map<String, Object> message = reqVO.getMessage();
        if (message == null) {
            throw invalidParamException("下行指令不能为空");
        }
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                StrUtil.blankToDefault(MapUtil.getStr(message, "requestId"), "debug"),
                MapUtil.getStr(message, "method"), message.get("params"));
        if (!IotDeviceMessageUtils.isCommandRequest(request)) {
            throw invalidParamException("下行编码只支持 thing.property.set、thing.service.invoke");
        }
        // 2.2 构建 encode 的 ctx
        return IotScriptUtils.buildEncodeContext(product.getProtocolType(), product.getProductKey(),
                DEBUG_DEVICE_NAME, request);
    }

    /**
     * 构建调试结果：输出按与网关相同的规则校验
     *
     * @param reqVO   调试请求
     * @param product 产品：encode 输出的透传字节，按产品协议的规则校验
     * @param result  脚本的执行结果
     * @return 调试结果
     */
    private static IotProductScriptDebugRespVO buildDebugResult(IotProductScriptDebugReqVO reqVO, IotProductDO product,
                                                                IotScriptResult result) {
        // 1. 执行失败：异常、超时、拒绝
        IotProductScriptDebugRespVO respVO = new IotProductScriptDebugRespVO().setDurationMs(result.getDurationMillis());
        if (!result.isSuccess()) {
            return failDebugResult(respVO, result.getStatus().name(), result.getMessage(), result.getLineNumber());
        }

        // 2. 校验输出：不合法时返回 OUTPUT_INVALID 等错误
        try {
            if (IotScriptUtils.FUNCTION_DECODE.equals(reqVO.getDirection())) {
                // 2.1 decode
                // 与网关一致：decode 未定义或返回 null，表示没有业务消息（只记录原始消息），message 为空
                IotDeviceMessage message = IotScriptUtils.validateDecode(result.getJson(), null);
                respVO.setMessage(message != null ? buildDebugMessage(message) : null);
            } else {
                // 2.2 encode
                // 与网关一致：字节按产品协议的规则校验（Modbus 必须是合法的请求 PDU）
                IotScriptUtils.EncodeOutput output = IotScriptUtils.validateEncode(result.getJson(),
                        IotProtocolTypeEnum.of(product.getProtocolType()));
                if (output == null) { // 与网关一致：未生成下行数据
                    return failDebugResult(respVO, "ENCODE_EMPTY", "数据解析脚本未生成下行数据", -1);
                }
                respVO.setStandard(output.isStandard());
                respVO.setOutputHex(output.getBytes() != null ? HexUtil.encodeHexStr(output.getBytes(), false) : null);
            }
        } catch (IotScriptException e) {
            return failDebugResult(respVO, e.getStatus().name(), e.getMessage(), e.getLineNumber());
        }
        // 3. 成功
        return respVO.setSuccess(true);
    }

    private static IotProductScriptDebugRespVO failDebugResult(IotProductScriptDebugRespVO respVO, String type, String message, int lineNumber) {
        return respVO.setSuccess(false).setError(new IotProductScriptDebugRespVO.ErrorInfo()
                .setType(type).setMessage(message).setLine(lineNumber > 0 ? lineNumber : null));
    }

    private static Map<String, Object> buildDebugMessage(IotDeviceMessage message) {
        return BeanUtil.beanToMap(message, "method", "requestId", "params", "data", "code", "msg");
    }

}
