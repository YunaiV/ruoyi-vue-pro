package cn.iocoder.yudao.module.wms.service.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.WmsStockBinMoveMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_NO_DUPLICATE;

/**
 * 库位移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinMoveServiceImpl implements WmsStockBinMoveService {

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockBinMoveMapper stockBinMoveMapper;

    /**
     * @sign : 4F450E910B7AC4E6
     */
    @Override
    public WmsStockBinMoveDO createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.STOCK_BIN_MOVE_NO_PREFIX, 3);
        createReqVO.setNo(no);
        if (stockBinMoveMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(STOCK_BIN_MOVE_NO_DUPLICATE);
        }
        // 插入
        WmsStockBinMoveDO stockBinMove = BeanUtils.toBean(createReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.insert(stockBinMove);
        // 返回
        return stockBinMove;
    }

    /**
     * @sign : B906C915ADF7E281
     */
    @Override
    public WmsStockBinMoveDO updateStockBinMove(WmsStockBinMoveSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockBinMoveDO exists = validateStockBinMoveExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 更新
        WmsStockBinMoveDO stockBinMove = BeanUtils.toBean(updateReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.updateById(stockBinMove);
        // 返回
        return stockBinMove;
    }

    /**
     * @sign : D02CAA96D65F5B67
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockBinMove(Long id) {
        // 校验存在
        WmsStockBinMoveDO stockBinMove = validateStockBinMoveExists(id);
        // 唯一索引去重
        stockBinMove.setNo(stockBinMoveMapper.flagUKeyAsLogicDelete(stockBinMove.getNo()));
        stockBinMoveMapper.updateById(stockBinMove);
        // 删除
        stockBinMoveMapper.deleteById(id);
    }

    /**
     * @sign : BE64F6298385B449
     */
    private WmsStockBinMoveDO validateStockBinMoveExists(Long id) {
        WmsStockBinMoveDO stockBinMove = stockBinMoveMapper.selectById(id);
        if (stockBinMove == null) {
            throw exception(STOCK_BIN_MOVE_NOT_EXISTS);
        }
        return stockBinMove;
    }

    @Override
    public WmsStockBinMoveDO getStockBinMove(Long id) {
        return stockBinMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveDO> getStockBinMovePage(WmsStockBinMovePageReqVO pageReqVO) {
        return stockBinMoveMapper.selectPage(pageReqVO);
    }
}
