package cn.iocoder.yudao.module.mes.service.pro.workorder;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.bom.MesProWorkOrderBomPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.bom.MesProWorkOrderBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductBomDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderBomDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.workorder.MesProWorkOrderBomMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdProductBomService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.MD_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_WORK_ORDER_BOM_NOT_EXISTS;

/**
 * MES 生产工单 BOM Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProWorkOrderBomServiceImpl implements MesProWorkOrderBomService {

    @Resource
    private MesProWorkOrderBomMapper workOrderBomMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdProductBomService productBomService;

    @Override
    public Long createWorkOrderBom(MesProWorkOrderBomSaveReqVO createReqVO) {
        // 校验工单存在
        workOrderService.validateWorkOrderExists(createReqVO.getWorkOrderId());

        // 插入数据
        MesProWorkOrderBomDO workOrderBom = BeanUtils.toBean(createReqVO, MesProWorkOrderBomDO.class);
        workOrderBomMapper.insert(workOrderBom);
        return workOrderBom.getId();
    }

    @Override
    public void updateWorkOrderBom(MesProWorkOrderBomSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkOrderBomExists(updateReqVO.getId());

        // 更新数据
        MesProWorkOrderBomDO updateObj = BeanUtils.toBean(updateReqVO, MesProWorkOrderBomDO.class);
        workOrderBomMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkOrderBom(Long id) {
        // 校验存在
        validateWorkOrderBomExists(id);

        // 删除
        workOrderBomMapper.deleteById(id);
    }

    private void validateWorkOrderBomExists(Long id) {
        if (workOrderBomMapper.selectById(id) == null) {
            throw exception(PRO_WORK_ORDER_BOM_NOT_EXISTS);
        }
    }

    @Override
    public MesProWorkOrderBomDO getWorkOrderBom(Long id) {
        return workOrderBomMapper.selectById(id);
    }

    @Override
    public PageResult<MesProWorkOrderBomDO> getWorkOrderBomPage(MesProWorkOrderBomPageReqVO pageReqVO) {
        return workOrderBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesProWorkOrderBomDO> getWorkOrderBomListByWorkOrderId(Long workOrderId) {
        return workOrderBomMapper.selectListByWorkOrderId(workOrderId);
    }

    @Override
    public void deleteWorkOrderBomByWorkOrderId(Long workOrderId) {
        workOrderBomMapper.deleteByWorkOrderId(workOrderId);
    }

    @Override
    public void generateWorkOrderBom(Long workOrderId, MesProWorkOrderSaveReqVO reqVO, boolean updated) {
        // 1. 如果是更新场景，先清理旧的 BOM 数据
        if (updated) {
            workOrderBomMapper.deleteByWorkOrderId(workOrderId);
        }

        // 2.1 查询产品 BOM
        List<MesMdProductBomDO> productBomList = productBomService.getProductBomListByItemId(reqVO.getProductId());
        if (CollUtil.isEmpty(productBomList)) {
            return;
        }
        // 2.2 批量获取并校验 BOM 物料
        Set<Long> bomItemIds = convertSet(productBomList, MesMdProductBomDO::getBomItemId);
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(bomItemIds);
        bomItemIds.forEach(itemId -> {
            if (itemMap.get(itemId) == null) {
                throw exception(MD_ITEM_NOT_EXISTS);
            }
        });

        // 3. 构建工单 BOM 列表并批量插入
        List<MesProWorkOrderBomDO> bomList = new ArrayList<>(productBomList.size());
        for (MesMdProductBomDO productBom : productBomList) {
            bomList.add(new MesProWorkOrderBomDO().setWorkOrderId(workOrderId).setItemId(productBom.getBomItemId())
                    .setQuantity(reqVO.getQuantity().multiply(productBom.getQuantity())));
        }
        workOrderBomMapper.insertBatch(bomList);
    }

}