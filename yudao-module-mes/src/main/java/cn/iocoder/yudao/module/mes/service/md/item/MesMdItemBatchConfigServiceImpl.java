package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.batchconfig.MesMdItemBatchConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdItemBatchConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.MD_ITEM_BATCH_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.MD_ITEM_NOT_EXISTS;

/**
 * MES 物料批次属性配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdItemBatchConfigServiceImpl implements MesMdItemBatchConfigService {

    @Resource
    private MesMdItemBatchConfigMapper itemBatchConfigMapper;

    @Resource
    @Lazy // 避免循环依赖（MesMdItemServiceImpl 也会注入本 Service）
    private MesMdItemService itemService;

    @Override
    public MesMdItemBatchConfigDO getItemBatchConfigByItemId(Long itemId) {
        return itemBatchConfigMapper.selectByItemId(itemId);
    }

    @Override
    public MesMdItemBatchConfigDO validateItemBatchConfigExists(Long itemId) {
        MesMdItemBatchConfigDO config = getItemBatchConfigByItemId(itemId);
        if (config == null) {
            throw exception(MD_ITEM_BATCH_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    @Override
    public Long saveItemBatchConfig(MesMdItemBatchConfigSaveReqVO saveReqVO) {
        // 1. 校验物料存在
        if (itemService.getItem(saveReqVO.getItemId()) == null) {
            throw exception(MD_ITEM_NOT_EXISTS);
        }

        // 2. 查询已有配置，决定新增还是更新
        MesMdItemBatchConfigDO existing = itemBatchConfigMapper.selectByItemId(saveReqVO.getItemId());
        if (existing != null) {
            // 更新
            MesMdItemBatchConfigDO updateObj = BeanUtils.toBean(saveReqVO, MesMdItemBatchConfigDO.class);
            updateObj.setId(existing.getId());
            itemBatchConfigMapper.updateById(updateObj);
            return existing.getId();
        }

        // 新增
        MesMdItemBatchConfigDO insertObj = BeanUtils.toBean(saveReqVO, MesMdItemBatchConfigDO.class);
        itemBatchConfigMapper.insert(insertObj);
        return insertObj.getId();
    }

    @Override
    public void deleteItemBatchConfigByItemId(Long itemId) {
        itemBatchConfigMapper.deleteByItemId(itemId);
    }

}
