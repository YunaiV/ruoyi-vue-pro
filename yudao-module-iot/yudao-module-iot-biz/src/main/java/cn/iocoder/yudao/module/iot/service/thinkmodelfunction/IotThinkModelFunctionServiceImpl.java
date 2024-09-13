package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodelfunction.IotThinkModelFunctionConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotThinkModelFunctionMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_NOT_EXISTS;

@Slf4j
@Service
@Validated
public class IotThinkModelFunctionServiceImpl implements IotThinkModelFunctionService {

    @Resource
    private IotThinkModelFunctionMapper thinkModelFunctionMapper;

    @Override
    public Long createThinkModelFunction(IotThinkModelFunctionSaveReqVO createReqVO) {
        log.info("创建物模型，参数：{}", createReqVO);
        // 验证 ProductKey 对应的产品物模型是否已存在
        validateThinkModelFunctionNotExistsByProductKey(createReqVO.getProductKey());
        // 插入
        IotThinkModelFunctionDO thinkModelFunction = IotThinkModelFunctionConvert.INSTANCE.convert(createReqVO);
        thinkModelFunctionMapper.insert(thinkModelFunction);
        // 返回
        return thinkModelFunction.getId();
    }

    private void validateThinkModelFunctionNotExistsByProductKey(String productKey) {
        if (thinkModelFunctionMapper.selectByProductKey(productKey) != null) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY);
        }
    }

    @Override
    public void deleteThinkModelFunction(Long id) {
        log.info("删除物模型，id：{}", id);
        // 校验存在
        validateThinkModelFunctionExists(id);
        // 删除
        thinkModelFunctionMapper.deleteById(id);
    }

    private void validateThinkModelFunctionExists(Long id) {
        if (thinkModelFunctionMapper.selectById(id) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunctionByProductKey(String productKey) {
        return thinkModelFunctionMapper.selectByProductKey(productKey);
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunctionByProductId(Long productId) {
        return thinkModelFunctionMapper.selectByProductId(productId);
    }

    @Override
    public void updateThinkModelFunction(IotThinkModelFunctionSaveReqVO updateReqVO) {
        log.info("更新物模型，参数：{}", updateReqVO);
        // 校验存在
        validateThinkModelFunctionExists(updateReqVO.getId());
        // 校验 productKey 是否重复
        validateProductKeyUnique(updateReqVO.getId(), updateReqVO.getProductKey());
        // 更新
        IotThinkModelFunctionDO thinkModelFunction = IotThinkModelFunctionConvert.INSTANCE.convert(updateReqVO);
        thinkModelFunctionMapper.updateById(thinkModelFunction);
    }

    private void validateProductKeyUnique(Long id, String productKey) {
        IotThinkModelFunctionDO existingFunction = thinkModelFunctionMapper.selectByProductKey(productKey);
        if (existingFunction != null && !existingFunction.getId().equals(id)) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY);
        }
    }
}