package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * MES 生产入库明细 Service 实现类
 */
@Service
@Validated
public class MesWmProductProduceDetailServiceImpl implements MesWmProductProduceDetailService {

    @Resource
    private MesWmProductProduceDetailMapper productProduceDetailMapper;

    @Override
    public void createProductProduceDetail(MesWmProductProduceDetailDO detail) {
        productProduceDetailMapper.insert(detail);
    }

    @Override
    public List<MesWmProductProduceDetailDO> getProductProduceDetailListByLineId(Long lineId) {
        return productProduceDetailMapper.selectListByLineId(lineId);
    }

}
