package cn.iocoder.yudao.module.wms.api.inbound;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.api.inbound.dto.*;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemListForTmsReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/28 17:03
 * @description: 入库单 API 实现
 */
@Service
@Slf4j
@Validated
public class WmsInboundApiImpl implements WmsInboundApi {

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;

    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(WmsInboundSaveReqDTO createReqDTO) {
        WmsInboundSaveReqVO createReqVO = BeanUtils.toBean(createReqDTO, WmsInboundSaveReqVO.class);
        //预填实际数量
        List<WmsInboundItemSaveReqDTO> itemList = createReqDTO.getItemList();
        itemList.forEach(item -> {
            item.setActualQty(item.getPlanQty());
        });
        createReqVO.setItemList(BeanUtils.toBean(itemList, WmsInboundItemSaveReqVO.class));
        //调拨单触发时，填写公司缺省值
        if (createReqVO.getUpstreamType().equals(BillType.TMS_TRANSFER.getValue())) {
            createReqVO.setCompanyId(999L);
        }
        WmsInboundDO inbound = inboundService.createInbound(createReqVO);
        log.info("wms入库单已创建: {}", inbound.getCode());
        //外部模块生成的入库单，直接发起审批
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillType(BillType.WMS_INBOUND.getValue());
        approvalReqVO.setBillId(inbound.getId());
        inboundService.approve(WmsInboundAuditStatus.Event.SUBMIT, approvalReqVO);
        return inbound.getId();
    }

    @Override
    public WmsInboundDTO getInbound(Long id) {
        WmsInboundDO inbound = inboundService.getInbound(id);
        return BeanUtils.toBean(inbound, WmsInboundDTO.class);
    }

    @Override
    public List<WmsInboundDTO> getInboundList(Integer upstreamType, Long upstreamId) {
        List<WmsInboundDO> inboundList = inboundService.getInboundList(upstreamType, upstreamId);
        return BeanUtils.toBean(inboundList, WmsInboundDTO.class);
    }

    /**
     * 作废入库单
     * @param id 入库单ID
     * @param comment 作废原因
     * @param billType 单据类型
     **/
    @Override
    public void abandonInbound(Long id, String comment, Integer billType) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(id);
        approvalReqVO.setComment(comment);
        inboundService.forceAbandon(approvalReqVO);
    }

    @Override
    public List<WmsInboundItemRespDTO> getInboundItemList(WmsInboundItemListForTmsReqDTO reqDTO) {
        // 查询数据
        List<WmsInboundItemQueryDO> doListResult = inboundItemService.getInboundItemListForTms(BeanUtils.toBean(reqDTO, WmsInboundItemListForTmsReqVO.class));
        // 转换
        List<WmsInboundItemRespVO> voListResult = BeanUtils.toBean(doListResult, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voListResult)
            .mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
            .mapping(WmsInboundItemRespVO::getUpdater, WmsInboundItemRespVO::setUpdaterName)
            .fill();
        // 装配
//        inboundItemService.assembleDept(voListResult);
//        inboundItemService.assembleInbound(voListResult);
//        inboundItemService.assembleProducts(voListResult);
//        inboundItemService.assembleWarehouse(voListResult);
//        inboundItemService.assembleStockType(voListResult);
        inboundItemService.assembleCompany(voListResult);
        inboundItemService.assembleStockWarehouse(voListResult);
//        InboundExecutor.setShelveAvailableQty(voListResult);
        // 返回
        return BeanUtils.toBean(voListResult, WmsInboundItemRespDTO.class);
    }

}
