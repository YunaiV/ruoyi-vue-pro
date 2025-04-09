package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin.WmsInventoryBinMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.product.WmsInventoryProductMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_BIN_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NO_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_PRODUCT_EXISTS;

/**
 * 盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Resource
    @Lazy
    private WmsInventoryBinMapper inventoryBinMapper;

    @Resource
    @Lazy
    private WmsInventoryProductMapper inventoryProductMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsInventoryMapper inventoryMapper;

    /**
     * @sign : A9D51C9E0E654C80
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInventoryDO createInventory(WmsInventorySaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.INVENTORY_NO_PREFIX, 3);
        createReqVO.setNo(no);
        if (inventoryMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(INVENTORY_NO_DUPLICATE);
        }
        // 插入
        WmsInventoryDO inventory = BeanUtils.toBean(createReqVO, WmsInventoryDO.class);
        inventoryMapper.insert(inventory);
        // 保存库存盘点产品详情
        if (createReqVO.getProductItemList() != null) {
            List<WmsInventoryProductDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getProductItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setInventoryId(inventory.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsInventoryProductDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryProductDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            inventoryProductMapper.insertBatch(toInsetList);
        }
        // 保存库位盘点详情
        if (createReqVO.getBinItemList() != null) {
            List<WmsInventoryBinDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getBinItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setInventoryId(inventory.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsInventoryBinDO.class));
            });
            // 校验 toInsetList 中是否有重复的 binId
            boolean isBinIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryBinDO::getBinId);
            if (isBinIdRepeated) {
                throw exception(INVENTORY_BIN_EXISTS);
            }
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryBinDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_BIN_EXISTS);
            }
            inventoryBinMapper.insertBatch(toInsetList);
        }
        // 返回
        return inventory;
    }

    /**
     * @sign : 2710B20EC7D9E031
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInventoryDO updateInventory(WmsInventorySaveReqVO updateReqVO) {
        // 校验存在
        WmsInventoryDO exists = validateInventoryExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存库存盘点产品详情
        if (updateReqVO.getProductItemList() != null) {
            List<WmsInventoryProductDO> existsInDB = inventoryProductMapper.selectByInventoryId(updateReqVO.getId());
            StreamX.CompareResult<WmsInventoryProductDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getProductItemList(), WmsInventoryProductDO.class), WmsInventoryProductDO::getId);
            List<WmsInventoryProductDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsInventoryProductDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsInventoryProductDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsInventoryProductDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryProductDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setInventoryId(updateReqVO.getId());
            });
            // 保存详情
            inventoryProductMapper.insertBatch(toInsetList);
            inventoryProductMapper.updateBatch(toUpdateList);
            inventoryProductMapper.deleteBatchIds(toDeleteList);
        }
        // 保存库位盘点详情
        if (updateReqVO.getBinItemList() != null) {
            List<WmsInventoryBinDO> existsInDB = inventoryBinMapper.selectByInventoryId(updateReqVO.getId());
            StreamX.CompareResult<WmsInventoryBinDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getBinItemList(), WmsInventoryBinDO.class), WmsInventoryBinDO::getId);
            List<WmsInventoryBinDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsInventoryBinDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsInventoryBinDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsInventoryBinDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 binId
            boolean isBinIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryBinDO::getBinId);
            if (isBinIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryBinDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setInventoryId(updateReqVO.getId());
            });
            // 保存详情
            inventoryBinMapper.insertBatch(toInsetList);
            inventoryBinMapper.updateBatch(toUpdateList);
            inventoryBinMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsInventoryDO inventory = BeanUtils.toBean(updateReqVO, WmsInventoryDO.class);
        inventoryMapper.updateById(inventory);
        // 返回
        return inventory;
    }

    /**
     * @sign : 159065E285D50040
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventory(Long id) {
        // 校验存在
        WmsInventoryDO inventory = validateInventoryExists(id);
        // 唯一索引去重
        inventory.setNo(inventoryMapper.flagUKeyAsLogicDelete(inventory.getNo()));
        inventoryMapper.updateById(inventory);
        // 删除
        inventoryMapper.deleteById(id);
    }

    /**
     * @sign : CCF673C00F6357F0
     */
    private WmsInventoryDO validateInventoryExists(Long id) {
        WmsInventoryDO inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw exception(INVENTORY_NOT_EXISTS);
        }
        return inventory;
    }

    @Override
    public WmsInventoryDO getInventory(Long id) {
        return inventoryMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO) {
        return inventoryMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsInventoryDO
     */
    public List<WmsInventoryDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inventoryMapper.selectByIds(idList);
    }
}
