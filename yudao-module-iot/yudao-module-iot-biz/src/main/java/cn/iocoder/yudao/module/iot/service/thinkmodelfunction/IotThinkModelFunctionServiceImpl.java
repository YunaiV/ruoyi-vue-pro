package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotThinkModelFunctionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_NOT_EXISTS;

/**
 * IoT 产品物模型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotThinkModelFunctionServiceImpl implements IotThinkModelFunctionService {

    @Resource
    private IotThinkModelFunctionMapper thinkModelFunctionMapper;

    @Override
    public Long createThinkModelFunction(IotThinkModelFunctionSaveReqVO createReqVO) {
        // 插入
        IotThinkModelFunctionDO thinkModelFunction = BeanUtils.toBean(createReqVO, IotThinkModelFunctionDO.class);
        // properties 字段，需要转换成 JSON
        thinkModelFunction.setProperties(JSONUtil.toJsonStr(createReqVO.getProperties()));
        thinkModelFunctionMapper.insert(thinkModelFunction);
        // 返回
        return thinkModelFunction.getId();
    }

    @Override
    public void deleteThinkModelFunction(Long id) {
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

    private void validateThinkModelFunctionExistsByProductKey(String productKey) {
        if (thinkModelFunctionMapper.selectByProductKey(productKey) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunctionByProductKey(String productKey) {
        return thinkModelFunctionMapper.selectByProductKey(productKey);
    }

    @Override
    public void updateThinkModelFunctionByProductKey(IotThinkModelFunctionSaveReqVO updateReqVO) {
        // 校验存在
        validateThinkModelFunctionExistsByProductKey(updateReqVO.getProductKey());
        // 更新
        IotThinkModelFunctionDO thinkModelFunction = BeanUtils.toBean(updateReqVO, IotThinkModelFunctionDO.class);
        // properties 字段，需要转换成 JSON
        thinkModelFunction.setProperties(JSONUtil.toJsonStr(updateReqVO.getProperties()));
        thinkModelFunctionMapper.updateByProductKey(thinkModelFunction);
    }

}