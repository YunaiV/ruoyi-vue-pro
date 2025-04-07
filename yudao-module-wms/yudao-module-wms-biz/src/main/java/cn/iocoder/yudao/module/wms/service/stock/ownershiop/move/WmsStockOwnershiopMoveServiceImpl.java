package cn.iocoder.yudao.module.wms.service.stock.ownershiop.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.vo.WmsStockOwnershiopMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.vo.WmsStockOwnershiopMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.WmsStockOwnershiopMoveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownershiop.move.WmsStockOwnershiopMoveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 所有者库存移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockOwnershiopMoveServiceImpl implements WmsStockOwnershiopMoveService {

    @Resource
    private WmsStockOwnershiopMoveMapper stockOwnershiopMoveMapper;

    @Override
    public Long createStockOwnershiopMove(WmsStockOwnershiopMoveSaveReqVO createReqVO) {
        // 插入
        WmsStockOwnershiopMoveDO stockOwnershiopMove = BeanUtils.toBean(createReqVO, WmsStockOwnershiopMoveDO.class);
        stockOwnershiopMoveMapper.insert(stockOwnershiopMove);
        // 返回
        return stockOwnershiopMove.getId();
    }

    @Override
    public void updateStockOwnershiopMove(WmsStockOwnershiopMoveSaveReqVO updateReqVO) {
        // 校验存在
        validateStockOwnershiopMoveExists(updateReqVO.getId());
        // 更新
        WmsStockOwnershiopMoveDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockOwnershiopMoveDO.class);
        stockOwnershiopMoveMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockOwnershiopMove(Long id) {
        // 校验存在
        validateStockOwnershiopMoveExists(id);
        // 删除
        stockOwnershiopMoveMapper.deleteById(id);
    }

    private void validateStockOwnershiopMoveExists(Long id) {
        if (stockOwnershiopMoveMapper.selectById(id) == null) {
            // throw exception(STOCK_OWNERSHIOP_MOVE_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockOwnershiopMoveDO getStockOwnershiopMove(Long id) {
        return stockOwnershiopMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockOwnershiopMoveDO> getStockOwnershiopMovePage(WmsStockOwnershiopMovePageReqVO pageReqVO) {
        return stockOwnershiopMoveMapper.selectPage(pageReqVO);
    }

}