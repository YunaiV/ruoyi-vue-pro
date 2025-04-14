package cn.iocoder.yudao.module.oms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopSaveReqVO;
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
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.*;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateBatchById;

@Service
@Slf4j
public class OmsShopServiceImpl implements OmsShopService {

    private final String CREATOR = "Admin";

    @Resource
    private OmsShopMapper shopMapper;

    @Override
    public Long createShop(OmsShopSaveReqVO saveReqDTO) {
        OmsShopDO shopDO = OmsShopConvert.INSTANCE.convert(saveReqDTO);
        OmsShopDO omsShopDO = shopMapper.selectOne(OmsShopDO::getName, saveReqDTO.getName());
        if (omsShopDO != null) {
            throw exception(OMS_SHOP_NAME_EXISTS);
        }
        ThrowUtil.ifSqlThrow(shopMapper.insert(shopDO), DB_INSERT_ERROR);
        return shopDO.getId();
    }

    @Override
    public void updateShop(OmsShopSaveReqVO updateReqVO) {
        Long shopId = updateReqVO.getId();
        // 校验存在
        validateShopExists(shopId);
        var shopDO = OmsShopConvert.INSTANCE.convert(updateReqVO);
        ThrowUtil.ifSqlThrow(shopMapper.insert(shopDO), DB_UPDATE_ERROR);
    }

    @Override
    public void deleteShop(Long id) {
        // 校验存在
        validateShopExists(id);
        ThrowUtil.ifSqlThrow(shopMapper.deleteById(id), DB_DELETE_ERROR);
    }

    public void validateShopExists(Long id) {
        if (shopMapper.selectById(id) == null) {
            throw exception(OMS_SHOP_NOT_EXISTS);
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
            //用创建者区分是否是同步过来的数据还是运营新增的数据
            shop.setCreator(CREATOR);
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

    @Override
    public List<OmsShopRespVO> getShopList(OmsShopPageReqVO reqVO) {
        return BeanUtils.toBean(shopMapper.getShopList(reqVO), OmsShopRespVO.class);
    }

    @Override
    public PageResult<OmsShopRespVO> getShopPage(OmsShopPageReqVO pageReqVO) {
        PageResult<OmsShopDO> omsShopDOPageResult = shopMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(omsShopDOPageResult, OmsShopRespVO.class);
    }

    @Override
    public OmsShopRespVO getShopById(Long id) {
        OmsShopDO omsShopDO = shopMapper.selectById(id);
        return BeanUtils.toBean(omsShopDO, OmsShopRespVO.class);
    }

    @Override
    public Boolean updateShopNameAndCode(Long id, String name, String code) {
        OmsShopDO omsShopDO = shopMapper.selectById(id);
        if (omsShopDO == null) {
            throw exception(OMS_SHOP_NOT_EXISTS);
        }
        OmsShopDO shopDO = shopMapper.selectOne(OmsShopDO::getName, name);
        if (shopDO != null && !shopDO.getId().equals(id)) {
            throw exception(OMS_SHOP_NAME_EXISTS);
        }
        omsShopDO.setName(name);
        omsShopDO.setCode(code);
        return shopMapper.updateById(omsShopDO) > 0;
    }

    @Override
    public Map<Long, OmsShopRespVO> getShopMapByIds(Set<Long> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return new HashMap<>();
        }
        LambdaQueryWrapperX<OmsShopDO> wrapperX = new LambdaQueryWrapperX<>();
        wrapperX.in(OmsShopDO::getId, shopIds);
        List<OmsShopDO> shopDOList = shopMapper.selectList(wrapperX);
        return StreamX.from(shopDOList).toMap(OmsShopDO::getId, t -> BeanUtils.toBean(t, OmsShopRespVO.class));

    }
}
