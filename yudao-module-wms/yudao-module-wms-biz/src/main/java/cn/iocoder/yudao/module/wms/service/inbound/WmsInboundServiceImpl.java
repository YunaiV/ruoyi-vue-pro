package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.WmsInboundMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 入库单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInboundServiceImpl implements WmsInboundService {

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    @Lazy()
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsInboundMapper inboundMapper;

    /**
     * @sign : 7A9DE76D2B7A3B30
     */
    @Override
    public WmsInboundDO createInbound(WmsInboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.INBOUND_NO_PREFIX, INBOUND_NOT_EXISTS);
        createReqVO.setNo(no);
        if (inboundMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(INBOUND_NO_DUPLICATE);
        }
        // 按 wms_inbound.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (createReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 插入
        WmsInboundDO inbound = BeanUtils.toBean(createReqVO, WmsInboundDO.class);
        inboundMapper.insert(inbound);
        // 返回
        return inbound;
    }

    /**
     * @sign : 3B2DE699CC208A81
     */
    @Override
    public WmsInboundDO updateInbound(WmsInboundSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundDO exists = validateInboundExists(updateReqVO.getId());
        updateReqVO.setNo(exists.getNo());
        // 按 wms_inbound.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (updateReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(updateReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 更新
        WmsInboundDO inbound = BeanUtils.toBean(updateReqVO, WmsInboundDO.class);
        inboundMapper.updateById(inbound);
        // 返回
        return inbound;
    }

    /**
     * @sign : FFFDDAD5269478BB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInbound(Long id) {
        // 校验存在
        WmsInboundDO inbound = validateInboundExists(id);
        // 唯一索引去重
        inbound.setNo(inboundMapper.flagUKeyAsLogicDelete(inbound.getNo()));
        inboundMapper.updateById(inbound);
        // 删除
        inboundMapper.deleteById(id);
    }

    /**
     * @sign : 6549448A5F16EE5E
     */
    private WmsInboundDO validateInboundExists(Long id) {
        WmsInboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw exception(INBOUND_NOT_EXISTS);
        }
        return inbound;
    }

    @Override
    public WmsInboundDO getInbound(Long id) {
        return inboundMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundDO> getInboundPage(WmsInboundPageReqVO pageReqVO) {
        return inboundMapper.selectPage(pageReqVO);
    }

    /**
     * 按 warehouseId 查询 WmsInboundDO
     */
    public List<WmsInboundDO> selectByWarehouseId(Long warehouseId, int limit) {
        return inboundMapper.selectByWarehouseId(warehouseId, limit);
    }
}