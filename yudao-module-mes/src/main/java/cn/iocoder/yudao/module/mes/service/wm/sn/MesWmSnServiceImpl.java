package cn.iocoder.yudao.module.mes.service.wm.sn;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGroupRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.sn.MesWmSnDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.sn.MesWmSnMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeRuleCodeEnum;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * MES SN 码 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class MesWmSnServiceImpl implements MesWmSnService {

    @Resource
    private MesWmSnMapper snMapper;

    @Resource
    private MesMdAutoCodeRecordService autoCodeRecordService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSnCodes(MesWmSnGenerateReqVO reqVO) {
        // 校验物料是否存在
        itemService.validateItemExists(reqVO.getItemId());
        // 校验工单是否存在
        if (reqVO.getWorkOrderId() != null) {
            workOrderService.validateWorkOrderExists(reqVO.getWorkOrderId());
        }

        List<MesWmSnDO> sns = new ArrayList<>(reqVO.getCount());
        // 生成批次 UUID
        String uuid = IdUtil.fastSimpleUUID();
        // 批量生成 SN 码
        for (int i = 0; i < reqVO.getCount(); i++) {
            String snCode = autoCodeRecordService.generateAutoCode(MesMdAutoCodeRuleCodeEnum.WM_SN_CODE.getCode());
            sns.add(new MesWmSnDO().setUuid(uuid).setCode(snCode)
                    .setItemId(reqVO.getItemId()).setBatchCode(reqVO.getBatchCode())
                    .setWorkOrderId(reqVO.getWorkOrderId()));
        }
        // 批量插入
        snMapper.insertBatch(sns);
        // 自动生成条码
        sns.forEach(sn -> barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.SN.getValue(),
                sn.getId(), sn.getCode(), sn.getCode()));
    }

    @Override
    public PageResult<MesWmSnGroupRespVO> getSnGroupPage(MesWmSnPageReqVO reqVO) {
        return snMapper.selectPageGroupByUuid(reqVO);
    }

    @Override
    public List<MesWmSnDO> getSnListByUuid(String uuid) {
        return snMapper.selectListByUuid(uuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSnByUuid(String uuid) {
        List<MesWmSnDO> sns = snMapper.selectListByUuid(uuid);
        if (CollUtil.isEmpty(sns)) {
            return;
        }

        // 删除 SN 码关联的条码记录
        barcodeService.deleteBarcodeByBizTypeAndBizIds(BarcodeBizTypeEnum.SN.getValue(),
                convertList(sns, MesWmSnDO::getId));
        // 删除 SN 码
        snMapper.deleteByUuid(uuid);
    }

}
