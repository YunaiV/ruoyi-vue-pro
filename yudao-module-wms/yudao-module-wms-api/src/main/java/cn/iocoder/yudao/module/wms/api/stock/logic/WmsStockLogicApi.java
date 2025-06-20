package cn.iocoder.yudao.module.wms.api.stock.logic;

import cn.iocoder.yudao.module.wms.api.stock.logic.dto.WmsStockLogicDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 库存所有权 API 接口
 *
 * @author wdy
 */
@Validated
public interface WmsStockLogicApi {

    /**
     * 获取某一部门在指定国别下的产品集合的库存归属
     *
     * @param deptId     部门ID
     * @param productIds 产品IDs
     * @param country    国家字典值
     * @return 库存归属 Map，key 为产品ID
     */
    Map<Long, WmsStockLogicDTO> selectByDeptIdAndProductIdAndCountryIdMap(Long deptId, @NotNull List<Long> productIds, @NotNull String country);

} 