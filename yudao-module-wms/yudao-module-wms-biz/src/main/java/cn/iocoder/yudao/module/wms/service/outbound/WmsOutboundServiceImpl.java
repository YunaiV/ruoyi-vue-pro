package cn.iocoder.yudao.module.wms.service.outbound;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.wms.dal.mysql.outbound.WmsOutboundMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出库单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsOutboundServiceImpl implements WmsOutboundService {

    @Resource
    private WmsOutboundMapper outboundMapper;

    @Override
    public Long createOutbound(WmsOutboundSaveReqVO createReqVO) {
        // 插入
        WmsOutboundDO outbound = BeanUtils.toBean(createReqVO, WmsOutboundDO.class);
        outboundMapper.insert(outbound);
        // 返回
        return outbound.getId();
    }

    @Override
    public void updateOutbound(WmsOutboundSaveReqVO updateReqVO) {
        // 校验存在
        validateOutboundExists(updateReqVO.getId());
        // 更新
        WmsOutboundDO updateObj = BeanUtils.toBean(updateReqVO, WmsOutboundDO.class);
        outboundMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutbound(Long id) {
        // 校验存在
        validateOutboundExists(id);
        // 删除
        outboundMapper.deleteById(id);
    }

    private void validateOutboundExists(Long id) {
        if (outboundMapper.selectById(id) == null) {
            //throw exception(OUTBOUND_NOT_EXISTS);
        }
    }

    @Override
    public WmsOutboundDO getOutbound(Long id) {
        return outboundMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOutboundDO> getOutboundPage(WmsOutboundPageReqVO pageReqVO) {
        return outboundMapper.selectPage(pageReqVO);
    }

}