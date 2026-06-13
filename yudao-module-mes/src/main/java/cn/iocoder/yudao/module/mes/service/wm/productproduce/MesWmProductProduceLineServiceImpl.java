package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productproduce.vo.MesWmProductProduceLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceLineMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产入库单行 Service 实现类
 */
@Service
@Validated
public class MesWmProductProduceLineServiceImpl implements MesWmProductProduceLineService {

    @Resource
    private MesWmProductProduceLineMapper productProduceLineMapper;

    @Override
    public void createProductProduceLine(MesWmProductProduceLineDO line) {
        productProduceLineMapper.insert(line);
    }

    @Override
    public MesWmProductProduceLineDO validateProductProduceLineExists(Long id) {
        MesWmProductProduceLineDO line = productProduceLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_PRODUCT_PRODUCE_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public void updateProductProduceLine(MesWmProductProduceLineDO line) {
        productProduceLineMapper.updateById(line);
    }

    @Override
    public List<MesWmProductProduceLineDO> getProductProduceLineListByProduceId(Long produceId) {
        return productProduceLineMapper.selectListByProduceId(produceId);
    }

    @Override
    public List<MesWmProductProduceLineDO> getProductProduceLineListByFeedbackId(Long feedbackId) {
        return productProduceLineMapper.selectListByFeedbackId(feedbackId);
    }

    @Override
    public PageResult<MesWmProductProduceLineDO> getProductProduceLinePage(MesWmProductProduceLinePageReqVO pageReqVO) {
        return productProduceLineMapper.selectPage(pageReqVO);
    }

}
