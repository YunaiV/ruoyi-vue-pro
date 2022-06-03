package cn.iocoder.yudao.module.mp.dal.mysql.fansmsg;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg.WxFansMsgDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo.*;

/**
 * 粉丝消息表  Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansMsgMapper extends BaseMapperX<WxFansMsgDO> {

    default PageResult<WxFansMsgDO> selectPage(WxFansMsgPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxFansMsgDO>()
                .eqIfPresent(WxFansMsgDO::getOpenid, reqVO.getOpenid())
                .likeIfPresent(WxFansMsgDO::getNickname, reqVO.getNickname())
                .eqIfPresent(WxFansMsgDO::getHeadimgUrl, reqVO.getHeadimgUrl())
                .eqIfPresent(WxFansMsgDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxFansMsgDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxFansMsgDO::getContent, reqVO.getContent())
                .eqIfPresent(WxFansMsgDO::getResContent, reqVO.getResContent())
                .eqIfPresent(WxFansMsgDO::getIsRes, reqVO.getIsRes())
                .eqIfPresent(WxFansMsgDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxFansMsgDO::getPicUrl, reqVO.getPicUrl())
                .eqIfPresent(WxFansMsgDO::getPicPath, reqVO.getPicPath())
                .betweenIfPresent(WxFansMsgDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansMsgDO::getId));
    }

    default List<WxFansMsgDO> selectList(WxFansMsgExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxFansMsgDO>()
                .eqIfPresent(WxFansMsgDO::getOpenid, reqVO.getOpenid())
                .likeIfPresent(WxFansMsgDO::getNickname, reqVO.getNickname())
                .eqIfPresent(WxFansMsgDO::getHeadimgUrl, reqVO.getHeadimgUrl())
                .eqIfPresent(WxFansMsgDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxFansMsgDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxFansMsgDO::getContent, reqVO.getContent())
                .eqIfPresent(WxFansMsgDO::getResContent, reqVO.getResContent())
                .eqIfPresent(WxFansMsgDO::getIsRes, reqVO.getIsRes())
                .eqIfPresent(WxFansMsgDO::getMediaId, reqVO.getMediaId())
                .eqIfPresent(WxFansMsgDO::getPicUrl, reqVO.getPicUrl())
                .eqIfPresent(WxFansMsgDO::getPicPath, reqVO.getPicPath())
                .betweenIfPresent(WxFansMsgDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansMsgDO::getId));
    }

}
