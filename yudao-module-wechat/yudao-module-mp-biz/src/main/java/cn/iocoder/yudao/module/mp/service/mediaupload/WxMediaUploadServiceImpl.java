package cn.iocoder.yudao.module.mp.service.mediaupload;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload.WxMediaUploadDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.mediaupload.WxMediaUploadConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.mediaupload.WxMediaUploadMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 微信素材上传表  Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxMediaUploadServiceImpl implements WxMediaUploadService {

    @Resource
    private WxMediaUploadMapper wxMediaUploadMapper;

    @Override
    public Integer createWxMediaUpload(WxMediaUploadCreateReqVO createReqVO) {
        // 插入
        WxMediaUploadDO wxMediaUpload = WxMediaUploadConvert.INSTANCE.convert(createReqVO);
        wxMediaUploadMapper.insert(wxMediaUpload);
        // 返回
        return wxMediaUpload.getId();
    }

    @Override
    public void updateWxMediaUpload(WxMediaUploadUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxMediaUploadExists(updateReqVO.getId());
        // 更新
        WxMediaUploadDO updateObj = WxMediaUploadConvert.INSTANCE.convert(updateReqVO);
        wxMediaUploadMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxMediaUpload(Integer id) {
        // 校验存在
        this.validateWxMediaUploadExists(id);
        // 删除
        wxMediaUploadMapper.deleteById(id);
    }

    private void validateWxMediaUploadExists(Integer id) {
        if (wxMediaUploadMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxMediaUploadDO getWxMediaUpload(Integer id) {
        return wxMediaUploadMapper.selectById(id);
    }

    @Override
    public List<WxMediaUploadDO> getWxMediaUploadList(Collection<Integer> ids) {
        return wxMediaUploadMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxMediaUploadDO> getWxMediaUploadPage(WxMediaUploadPageReqVO pageReqVO) {
        return wxMediaUploadMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxMediaUploadDO> getWxMediaUploadList(WxMediaUploadExportReqVO exportReqVO) {
        return wxMediaUploadMapper.selectList(exportReqVO);
    }

}
