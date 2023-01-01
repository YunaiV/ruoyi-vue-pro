package cn.iocoder.yudao.module.mp.dal.mysql.mediaupload;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload.WxMediaUploadDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo.*;

/**
 * 微信素材上传表  Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxMediaUploadMapper extends BaseMapperX<WxMediaUploadDO> {

    default PageResult<WxMediaUploadDO> selectPage(WxMediaUploadPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxMediaUploadDO>()
                .eqIfPresent(WxMediaUploadDO::getType, reqVO.getType())
                .eqIfPresent(WxMediaUploadDO::getUrl, reqVO.getUrl())
                .eqIfPresent(WxMediaUploadDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxMediaUploadDO::getThumbMediaId, reqVO.getThumbMediaId())
                .eqIfPresent(WxMediaUploadDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxMediaUploadDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxMediaUploadDO::getId));
    }

    default List<WxMediaUploadDO> selectList(WxMediaUploadExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxMediaUploadDO>()
                .eqIfPresent(WxMediaUploadDO::getType, reqVO.getType())
                .eqIfPresent(WxMediaUploadDO::getUrl, reqVO.getUrl())
                .eqIfPresent(WxMediaUploadDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxMediaUploadDO::getThumbMediaId, reqVO.getThumbMediaId())
                .eqIfPresent(WxMediaUploadDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxMediaUploadDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxMediaUploadDO::getId));
    }

}
