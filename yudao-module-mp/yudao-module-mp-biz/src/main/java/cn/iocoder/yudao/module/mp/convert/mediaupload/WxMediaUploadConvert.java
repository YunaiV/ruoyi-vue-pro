package cn.iocoder.yudao.module.mp.convert.mediaupload;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload.WxMediaUploadDO;

/**
 * 微信素材上传表  Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxMediaUploadConvert {

    WxMediaUploadConvert INSTANCE = Mappers.getMapper(WxMediaUploadConvert.class);

    WxMediaUploadDO convert(WxMediaUploadCreateReqVO bean);

    WxMediaUploadDO convert(WxMediaUploadUpdateReqVO bean);

    WxMediaUploadRespVO convert(WxMediaUploadDO bean);

    List<WxMediaUploadRespVO> convertList(List<WxMediaUploadDO> list);

    PageResult<WxMediaUploadRespVO> convertPage(PageResult<WxMediaUploadDO> page);

    List<WxMediaUploadExcelVO> convertList02(List<WxMediaUploadDO> list);

}
