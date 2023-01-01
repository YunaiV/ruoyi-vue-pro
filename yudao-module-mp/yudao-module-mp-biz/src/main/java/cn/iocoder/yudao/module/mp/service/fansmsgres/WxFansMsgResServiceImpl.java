package cn.iocoder.yudao.module.mp.service.fansmsgres;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres.WxFansMsgResDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.fansmsgres.WxFansMsgResConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.fansmsgres.WxFansMsgResMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 回复粉丝消息历史表  Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxFansMsgResServiceImpl implements WxFansMsgResService {

    @Resource
    private WxFansMsgResMapper wxFansMsgResMapper;

    @Override
    public Integer createWxFansMsgRes(WxFansMsgResCreateReqVO createReqVO) {
        // 插入
        WxFansMsgResDO wxFansMsgRes = WxFansMsgResConvert.INSTANCE.convert(createReqVO);
        wxFansMsgResMapper.insert(wxFansMsgRes);
        // 返回
        return wxFansMsgRes.getId();
    }

    @Override
    public void updateWxFansMsgRes(WxFansMsgResUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxFansMsgResExists(updateReqVO.getId());
        // 更新
        WxFansMsgResDO updateObj = WxFansMsgResConvert.INSTANCE.convert(updateReqVO);
        wxFansMsgResMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxFansMsgRes(Integer id) {
        // 校验存在
        this.validateWxFansMsgResExists(id);
        // 删除
        wxFansMsgResMapper.deleteById(id);
    }

    private void validateWxFansMsgResExists(Integer id) {
        if (wxFansMsgResMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxFansMsgResDO getWxFansMsgRes(Integer id) {
        return wxFansMsgResMapper.selectById(id);
    }

    @Override
    public List<WxFansMsgResDO> getWxFansMsgResList(Collection<Integer> ids) {
        return wxFansMsgResMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxFansMsgResDO> getWxFansMsgResPage(WxFansMsgResPageReqVO pageReqVO) {
        return wxFansMsgResMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxFansMsgResDO> getWxFansMsgResList(WxFansMsgResExportReqVO exportReqVO) {
        return wxFansMsgResMapper.selectList(exportReqVO);
    }

}
