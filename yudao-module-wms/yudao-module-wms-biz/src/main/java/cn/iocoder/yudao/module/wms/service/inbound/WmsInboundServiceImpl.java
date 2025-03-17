package cn.iocoder.yudao.module.wms.service.inbound;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.wms.dal.mysql.inbound.WmsInboundMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 入库单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInboundServiceImpl implements WmsInboundService {

    @Resource
    private WmsInboundMapper inboundMapper;

    @Override
    public Long createInbound(WmsInboundSaveReqVO createReqVO) {
        // 插入
        WmsInboundDO inbound = BeanUtils.toBean(createReqVO, WmsInboundDO.class);
        inboundMapper.insert(inbound);
        // 返回
        return inbound.getId();
    }

    @Override
    public void updateInbound(WmsInboundSaveReqVO updateReqVO) {
        // 校验存在
        validateInboundExists(updateReqVO.getId());
        // 更新
        WmsInboundDO updateObj = BeanUtils.toBean(updateReqVO, WmsInboundDO.class);
        inboundMapper.updateById(updateObj);
    }

    @Override
    public void deleteInbound(Long id) {
        // 校验存在
        validateInboundExists(id);
        // 删除
        inboundMapper.deleteById(id);
    }

    private void validateInboundExists(Long id) {
        if (inboundMapper.selectById(id) == null) {
            //throw exception(INBOUND_NOT_EXISTS);
        }
    }

    @Override
    public WmsInboundDO getInbound(Long id) {
        return inboundMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundDO> getInboundPage(WmsInboundPageReqVO pageReqVO) {
        return inboundMapper.selectPage(pageReqVO);
    }

}