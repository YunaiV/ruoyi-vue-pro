package cn.iocoder.yudao.module.oms.service.impl;


import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.convert.OmsShopConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.SHOP_NOT_EXISTS;

@Service
public class OmsShopServiceImpl implements OmsShopService {

    @Resource
    private OmsShopMapper shopMapper;

    @Resource
    private OmsShopConvert shopConvert;

    @Override
    public Long createShop(OmsShopSaveReqDTO saveReqDTO) {
        var shopDO = shopConvert.convert(saveReqDTO);
        ThrowUtil.ifSqlThrow(shopMapper.insert(shopDO), DB_INSERT_ERROR);
        return shopDO.getId();
    }

    @Override
    public void updateShop(OmsShopSaveReqDTO updateReqDTO) {
        Long shopId = updateReqDTO.getId();
        // 校验存在
        validateShopExists(shopId);
        var shopDO = shopConvert.convert(updateReqDTO);
        ThrowUtil.ifSqlThrow(shopMapper.insert(shopDO), DB_UPDATE_ERROR);
    }

    @Override
    public void deleteShop(Long id) {
        // 校验存在
        validateShopExists(id);
        //TODO 后续如果还存在其他关联，请做校验
        ThrowUtil.ifSqlThrow(shopMapper.deleteById(id), DB_DELETE_ERROR);
    }

    public void validateShopExists(Long id) {
        if (shopMapper.selectById(id) == null) {
            throw exception(SHOP_NOT_EXISTS);
        }
    }

    @Override
    public List<OmsShopDO> getByPlatformCode(String platformCode) {
        return shopMapper.getByPlatformCode(platformCode);
    }

    @Override
    public OmsShopDO getByPlatformShopCode(String platformShopCode) {
        return shopMapper.getByPlatformShopCode(platformShopCode);
    }
}
