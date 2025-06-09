package cn.iocoder.yudao.module.wms.api.stock.logic;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.api.stock.logic.dto.WmsStockLogicDTO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.service.stock.logic.WmsStockLogicService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 库存所有权 API 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class WmsStockLogicApiImpl implements WmsStockLogicApi {

    @Resource
    private WmsStockLogicService stockLogicService;

    @Override
    public Map<Long, WmsStockLogicDTO> selectByDeptIdAndProductIdAndCountryIdMap(Long deptId, List<Long> productIds, String country) {
        Map<Long, WmsStockLogicDO> map = stockLogicService.selectByDeptIdAndProductIdAndCountryIdMap(deptId, productIds, country);
        return BeanUtils.toBean(map, WmsStockLogicDTO.class);
    }

} 