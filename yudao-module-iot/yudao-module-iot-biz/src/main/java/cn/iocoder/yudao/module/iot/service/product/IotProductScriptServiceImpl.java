package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptTestReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptTestRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductScriptDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductScriptMapper;
import cn.iocoder.yudao.module.iot.plugin.script.context.PluginScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_SCRIPT_NOT_EXISTS;

/**
 * IoT 产品脚本信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotProductScriptServiceImpl implements IotProductScriptService {

    @Resource
    private IotProductScriptMapper productScriptMapper;

    @Resource
    private IotProductService productService;

    @Resource
    private ScriptService scriptService;

    @Override
    public Long createProductScript(IotProductScriptSaveReqVO createReqVO) {
        // 验证产品是否存在
        validateProductExists(createReqVO.getProductId());

        // 插入
        IotProductScriptDO productScript = BeanUtils.toBean(createReqVO, IotProductScriptDO.class);
        // 初始化版本为1
        productScript.setVersion(1);
        // 初始化测试相关字段
        productScript.setLastTestResult(null);
        productScript.setLastTestTime(null);
        productScriptMapper.insert(productScript);
        // 返回
        return productScript.getId();
    }

    @Override
    public void updateProductScript(IotProductScriptSaveReqVO updateReqVO) {
        // 校验存在
        validateProductScriptExists(updateReqVO.getId());

        // 获取旧的记录，保留版本号和测试信息
        IotProductScriptDO oldScript = getProductScript(updateReqVO.getId());

        // 更新
        IotProductScriptDO updateObj = BeanUtils.toBean(updateReqVO, IotProductScriptDO.class);
        // 更新版本号
        updateObj.setVersion(oldScript.getVersion() + 1);
        // 保留测试相关信息
        updateObj.setLastTestTime(oldScript.getLastTestTime());
        updateObj.setLastTestResult(oldScript.getLastTestResult());
        productScriptMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductScript(Long id) {
        // 校验存在
        validateProductScriptExists(id);
        // 删除
        productScriptMapper.deleteById(id);
    }

    private void validateProductScriptExists(Long id) {
        if (productScriptMapper.selectById(id) == null) {
            throw exception(PRODUCT_SCRIPT_NOT_EXISTS);
        }
    }

    private void validateProductExists(Long productId) {
        IotProductDO product = productService.getProduct(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public IotProductScriptDO getProductScript(Long id) {
        return productScriptMapper.selectById(id);
    }

    @Override
    public PageResult<IotProductScriptDO> getProductScriptPage(IotProductScriptPageReqVO pageReqVO) {
        return productScriptMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotProductScriptDO> getProductScriptListByProductId(Long productId) {
        return productScriptMapper.selectList(new LambdaQueryWrapper<IotProductScriptDO>()
                .eq(IotProductScriptDO::getProductId, productId)
                .orderByDesc(IotProductScriptDO::getId));
    }

    @Override
    public IotProductScriptTestRespVO testProductScript(IotProductScriptTestReqVO testReqVO) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证产品是否存在
            validateProductExists(testReqVO.getProductId());

            // 根据ID获取已保存的脚本（如果有）
            IotProductScriptDO existingScript = null;
            if (testReqVO.getId() != null) {
                existingScript = getProductScript(testReqVO.getId());
            }

            // 创建测试上下文
            PluginScriptContext context = new PluginScriptContext();
            IotProductDO product = productService.getProduct(testReqVO.getProductId());

            // 设置设备上下文（使用产品信息，没有具体设备）
            context.withDeviceContext(product.getProductKey(), null);

            // 设置输入参数
            Map<String, Object> params = new HashMap<>();
            params.put("input", testReqVO.getTestInput());
            params.put("productKey", product.getProductKey());
            params.put("scriptType", testReqVO.getScriptType());

            // 根据脚本类型设置特定参数
            switch (testReqVO.getScriptType()) {
                case 1: // PROPERTY_PARSER
                    params.put("method", "property");
                    break;
                case 2: // EVENT_PARSER
                    params.put("method", "event");
                    params.put("identifier", "default");
                    break;
                case 3: // COMMAND_ENCODER
                    params.put("method", "command");
                    break;
                default:
                    // 默认不添加额外参数
            }

            // 添加所有参数到上下文
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.setParameter(entry.getKey(), entry.getValue());
            }

            // 执行脚本
            Object result = scriptService.executeScript(
                    testReqVO.getScriptLanguage(),
                    testReqVO.getScriptContent(),
                    context);

            // 更新测试结果（如果是已保存的脚本）
            if (existingScript != null) {
                IotProductScriptDO updateObj = new IotProductScriptDO();
                updateObj.setId(existingScript.getId());
                updateObj.setLastTestTime(LocalDateTime.now());
                updateObj.setLastTestResult(1); // 1表示成功
                productScriptMapper.updateById(updateObj);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return IotProductScriptTestRespVO.success(result, executionTime);

        } catch (Exception e) {
            log.error("[testProductScript][测试脚本异常]", e);

            // 如果是已保存的脚本，更新测试失败状态
            if (testReqVO.getId() != null) {
                try {
                    IotProductScriptDO updateObj = new IotProductScriptDO();
                    updateObj.setId(testReqVO.getId());
                    updateObj.setLastTestTime(LocalDateTime.now());
                    updateObj.setLastTestResult(0); // 0表示失败
                    productScriptMapper.updateById(updateObj);
                } catch (Exception ex) {
                    log.error("[testProductScript][更新脚本测试结果异常]", ex);
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return IotProductScriptTestRespVO.error(e.getMessage(), executionTime);
        }
    }

    @Override
    public void updateProductScriptStatus(Long id, Integer status) {
        // 校验存在
        validateProductScriptExists(id);

        // 更新状态
        IotProductScriptDO updateObj = new IotProductScriptDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        productScriptMapper.updateById(updateObj);
    }
}