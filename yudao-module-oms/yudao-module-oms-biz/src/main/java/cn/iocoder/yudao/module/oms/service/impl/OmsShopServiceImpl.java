package cn.iocoder.yudao.module.oms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.convert.OmsShopConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.SHOP_NOT_EXISTS;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateBatchById;

@Service
@Slf4j
public class OmsShopServiceImpl implements OmsShopService {

    @Resource
    private OmsShopMapper shopMapper;

    @Override
    public Long createShop(OmsShopSaveReqDTO saveReqDTO) {
        var shopDO = OmsShopConvert.INSTANCE.convert(saveReqDTO);
        ThrowUtil.ifSqlThrow(shopMapper.insert(shopDO), DB_INSERT_ERROR);
        return shopDO.getId();
    }

    @Override
    public void updateShop(OmsShopSaveReqDTO updateReqDTO) {
        Long shopId = updateReqDTO.getId();
        // 校验存在
        validateShopExists(shopId);
        var shopDO = OmsShopConvert.INSTANCE.convert(updateReqDTO);
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
    public OmsShopDTO getShopByPlatformShopCode(String platformShopCode) {
        OmsShopDO omsShopDO = shopMapper.getByPlatformShopCode(platformShopCode);
        return OmsShopConvert.INSTANCE.toOmsShopDTO(omsShopDO);
    }

    @Override
    public void createOrUpdateShopByPlatform(List<OmsShopSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        List<OmsShopDO> shops = OmsShopConvert.INSTANCE.toOmsShopDOs(saveReqDTOs);

        List<OmsShopDO> createShops = new ArrayList<>();
        List<OmsShopDO> updateShops = new ArrayList<>();

        List<OmsShopDO> existShops = getByPlatformCode(shops.get(0).getPlatformCode());

        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getPlatformShopCode(), omsShopDO -> omsShopDO));

        shops.forEach(shop -> {
            OmsShopDO existShop = existShopMap.get(shop.getPlatformShopCode());
            if (existShop != null) {
                shop.setId(existShop.getId());
                updateShops.add(shop);
            } else {
                // 新增
                createShops.add(shop);
            }
        });

        if (CollectionUtil.isNotEmpty(createShops)) {
            saveBatch(createShops);
        }

        if (CollectionUtil.isNotEmpty(updateShops)) {
            updateBatchById(updateShops);
        }
        log.info("sync shop success,salesPlatformCode:{},shopCount:{}", saveReqDTOs.get(0).getPlatformCode(), saveReqDTOs.size());
    }
}
