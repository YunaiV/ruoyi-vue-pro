package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.stockrecord.ErpStockRecordPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * ERP 产品库存明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpStockRecordServiceImpl implements ErpStockRecordService {

    @Resource
    private ErpStockRecordMapper stockRecordMapper;

    @Override
    public ErpStockRecordDO getStockRecord(Long id) {
        return stockRecordMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockRecordDO> getStockRecordPage(ErpStockRecordPageReqVO pageReqVO) {
        return stockRecordMapper.selectPage(pageReqVO);
    }

}