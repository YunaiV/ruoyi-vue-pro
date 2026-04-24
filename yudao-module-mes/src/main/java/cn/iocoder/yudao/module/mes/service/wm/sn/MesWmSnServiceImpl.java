package cn.iocoder.yudao.module.mes.service.wm.sn;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGroupRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.sn.MesWmSnDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.sn.MesWmSnMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeRuleCodeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * MES SN 码 Service 实现类
 *
 * @author deepay
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
        snMapper.deleteByUuid(uuid);
    }

}