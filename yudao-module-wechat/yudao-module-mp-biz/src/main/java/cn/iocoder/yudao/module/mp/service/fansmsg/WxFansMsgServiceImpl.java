package cn.iocoder.yudao.module.mp.service.fansmsg;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg.WxFansMsgDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.fansmsg.WxFansMsgConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.fansmsg.WxFansMsgMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 粉丝消息表  Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxFansMsgServiceImpl implements WxFansMsgService {

    @Resource
    private WxFansMsgMapper wxFansMsgMapper;

    @Override
    public Integer createWxFansMsg(WxFansMsgCreateReqVO createReqVO) {
        // 插入
        WxFansMsgDO wxFansMsg = WxFansMsgConvert.INSTANCE.convert(createReqVO);
        wxFansMsgMapper.insert(wxFansMsg);
        // 返回
        return wxFansMsg.getId();
    }

    @Override
    public void updateWxFansMsg(WxFansMsgUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxFansMsgExists(updateReqVO.getId());
        // 更新
        WxFansMsgDO updateObj = WxFansMsgConvert.INSTANCE.convert(updateReqVO);
        wxFansMsgMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxFansMsg(Integer id) {
        // 校验存在
        this.validateWxFansMsgExists(id);
        // 删除
        wxFansMsgMapper.deleteById(id);
    }

    private void validateWxFansMsgExists(Integer id) {
        if (wxFansMsgMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxFansMsgDO getWxFansMsg(Integer id) {
        return wxFansMsgMapper.selectById(id);
    }

    @Override
    public List<WxFansMsgDO> getWxFansMsgList(Collection<Integer> ids) {
        return wxFansMsgMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxFansMsgDO> getWxFansMsgPage(WxFansMsgPageReqVO pageReqVO) {
        return wxFansMsgMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxFansMsgDO> getWxFansMsgList(WxFansMsgExportReqVO exportReqVO) {
        return wxFansMsgMapper.selectList(exportReqVO);
    }

}
