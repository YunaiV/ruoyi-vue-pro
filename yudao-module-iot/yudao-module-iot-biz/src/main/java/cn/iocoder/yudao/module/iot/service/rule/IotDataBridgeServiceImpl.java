package cn.iocoder.yudao.module.iot.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataBridgeMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DATA_BRIDGE_NOT_EXISTS;

/**
 * IoT 数据桥梁 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class IotDataBridgeServiceImpl implements IotDataBridgeService {

    @Resource
    private IotDataBridgeMapper dataBridgeMapper;

    @Override
    public Long createDataBridge(IotDataBridgeSaveReqVO createReqVO) {
        // 插入
        IotDataBridgeDO dataBridge = BeanUtils.toBean(createReqVO, IotDataBridgeDO.class);
        dataBridgeMapper.insert(dataBridge);
        // 返回
        return dataBridge.getId();
    }

    @Override
    public void updateDataBridge(IotDataBridgeSaveReqVO updateReqVO) {
        // 校验存在
        validateDataBridgeExists(updateReqVO.getId());
        // 更新
        IotDataBridgeDO updateObj = BeanUtils.toBean(updateReqVO, IotDataBridgeDO.class);
        dataBridgeMapper.updateById(updateObj);
    }

    @Override
    public void deleteDataBridge(Long id) {
        // 校验存在
        validateDataBridgeExists(id);
        // 删除
        dataBridgeMapper.deleteById(id);
    }

    private void validateDataBridgeExists(Long id) {
        if (dataBridgeMapper.selectById(id) == null) {
            throw exception(DATA_BRIDGE_NOT_EXISTS);
        }
    }

    @Override
    public IotDataBridgeDO getDataBridge(Long id) {
        return dataBridgeMapper.selectById(id);
    }

    @Override
    public PageResult<IotDataBridgeDO> getDataBridgePage(IotDataBridgePageReqVO pageReqVO) {
        return dataBridgeMapper.selectPage(pageReqVO);
    }

}