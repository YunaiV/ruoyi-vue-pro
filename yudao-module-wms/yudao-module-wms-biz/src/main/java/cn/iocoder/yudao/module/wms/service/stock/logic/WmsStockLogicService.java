package cn.iocoder.yudao.module.wms.service.stock.logic;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 逻辑库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockLogicService {

    /**
     * 创建逻辑库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockLogicDO createStockLogic(@Valid WmsStockLogicSaveReqVO createReqVO);

    /**
     * 更新逻辑库存
     *
     * @param updateReqVO 更新信息
     */
    WmsStockLogicDO updateStockLogic(@Valid WmsStockLogicSaveReqVO updateReqVO);

    /**
     * 删除逻辑库存
     *
     * @param id 编号
     */
    void deleteStockLogic(Long id);

    /**
     * 获得逻辑库存
     *
     * @param id 编号
     * @return 逻辑库存
     */
    WmsStockLogicDO getStockLogic(Long id);

    /**
     * 获得逻辑库存分页
     *
     * @param pageReqVO 分页查询
     * @return 逻辑库存分页
     */
    PageResult<WmsStockLogicDO> getStockLogicPage(WmsStockLogicPageReqVO pageReqVO);

    /**
     * 获取库存归属列表
     *
     * @param warehouseId 仓库id，必填
     * @param productId 产品id，可选
     * @param companyId 公司id，可选
     * @param deptId 部门id，可选
     * @return 库存归属列表
     */
    List<WmsStockLogicDO> selectStockLogic(@NotNull Long warehouseId, Long productId, Long companyId, Long deptId);

    /**
     * 批量获取产品的库存归属列表
     *
     * @param warehouseId 仓库id
     * @param productId   产品id列表
     * @return 库存归属列表
     */
    List<WmsStockLogicDO> selectStockLogic(@NotNull Long warehouseId, List<Long> productId);

    /**
     * 根据唯一键获取库存归属，如果不存在则创建
     *
     * @param warehouseId 仓库id
     * @param companyId 公司id
     * @param deptId 部门id
     * @param productId 产品id
     * @param createNew 是否创建新记录
     * @return 库存归属
     */
    WmsStockLogicDO getByUkProductOwner(Long warehouseId, Long companyId, Long deptId, Long productId, boolean createNew);

    /**
     * 插入或更新库存归属记录
     *
     * @param stockLogicDO 库存归属信息
     */
    void insertOrUpdate(WmsStockLogicDO stockLogicDO);

    /**
     * 装配产品信息
     *
     * @param list 库存归属列表
     */
    void assembleProducts(List<WmsStockLogicRespVO> list);

    /**
     * 装配仓库信息
     *
     * @param list 库存归属列表
     */
    void assembleWarehouse(List<WmsStockLogicRespVO> list);

    /**
     * 装配部门信息
     *
     * @param list 库存归属列表
     */
    void assembleDept(List<WmsStockLogicRespVO> list);

    /**
     * 装配公司信息
     *
     * @param list 库存归属列表
     */
    void assembleCompany(List<WmsStockLogicRespVO> list);

    /**
     * 根据ID列表批量查询库存归属
     *
     * @param stockLogicIds 库存归属ID列表
     * @return 库存归属列表
     */
    List<WmsStockLogicDO> selectByIds(List<Long> stockLogicIds);

    /**
     * 获取某一部门在指定国别下的产品集合的库存归属
     * @param deptId    部门ID
     * @param productIds 产品IDs
     * @param country 国家字典值
     * @return 库存归属
     */
    List<WmsStockLogicDO> selectByDeptIdAndProductIdAndCountryId(Long deptId, @NotNull List<Long> productIds, @NotNull String country);


    /**
     * 获取某一部门在指定国别下的产品集合的库存归属
     *
     * @param deptId     部门ID
     * @param productIds 产品IDs
     * @param country    国家字典值
     * @return 库存归属
     */
    default Map<Long, WmsStockLogicDO> selectByDeptIdAndProductIdAndCountryIdMap(Long deptId, @NotNull List<Long> productIds, @NotNull String country) {
        List<WmsStockLogicDO> list = selectByDeptIdAndProductIdAndCountryId(deptId, productIds, country);
        if (list == null) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(WmsStockLogicDO::getProductId, Function.identity()));
    }

    /**
     * 根据部门ID和产品ID查询库存归属
     *
     * @param warehouseId 仓库ID
     * @param productId   产品ID
     * @return 库存归属列表
     */
    List<WmsStockLogicDO> selectByWarehouseIdAndProductId(Long warehouseId, Long productId);

}
