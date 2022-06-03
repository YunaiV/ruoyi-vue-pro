package cn.iocoder.yudao.module.mp.service.receivetext;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.receivetext.WxReceiveTextDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.receivetext.WxReceiveTextConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.receivetext.WxReceiveTextMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 回复关键字 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxReceiveTextServiceImpl implements WxReceiveTextService {

    @Resource
    private WxReceiveTextMapper wxReceiveTextMapper;

    @Override
    public Integer createWxReceiveText(WxReceiveTextCreateReqVO createReqVO) {
        // 插入
        WxReceiveTextDO wxReceiveText = WxReceiveTextConvert.INSTANCE.convert(createReqVO);
        wxReceiveTextMapper.insert(wxReceiveText);
        // 返回
        return wxReceiveText.getId();
    }

    @Override
    public void updateWxReceiveText(WxReceiveTextUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxReceiveTextExists(updateReqVO.getId());
        // 更新
        WxReceiveTextDO updateObj = WxReceiveTextConvert.INSTANCE.convert(updateReqVO);
        wxReceiveTextMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxReceiveText(Integer id) {
        // 校验存在
        this.validateWxReceiveTextExists(id);
        // 删除
        wxReceiveTextMapper.deleteById(id);
    }

    private void validateWxReceiveTextExists(Integer id) {
        if (wxReceiveTextMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxReceiveTextDO getWxReceiveText(Integer id) {
        return wxReceiveTextMapper.selectById(id);
    }

    @Override
    public List<WxReceiveTextDO> getWxReceiveTextList(Collection<Integer> ids) {
        return wxReceiveTextMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxReceiveTextDO> getWxReceiveTextPage(WxReceiveTextPageReqVO pageReqVO) {
        return wxReceiveTextMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxReceiveTextDO> getWxReceiveTextList(WxReceiveTextExportReqVO exportReqVO) {
        return wxReceiveTextMapper.selectList(exportReqVO);
    }

}
