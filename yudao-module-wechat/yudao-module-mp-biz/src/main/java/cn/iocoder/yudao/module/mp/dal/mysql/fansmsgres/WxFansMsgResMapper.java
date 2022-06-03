package cn.iocoder.yudao.module.mp.dal.mysql.fansmsgres;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres.WxFansMsgResDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo.*;

/**
 * 回复粉丝消息历史表  Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansMsgResMapper extends BaseMapperX<WxFansMsgResDO> {

    default PageResult<WxFansMsgResDO> selectPage(WxFansMsgResPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxFansMsgResDO>()
                .eqIfPresent(WxFansMsgResDO::getFansMsgId, reqVO.getFansMsgId())
                .eqIfPresent(WxFansMsgResDO::getResContent, reqVO.getResContent())
                .betweenIfPresent(WxFansMsgResDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansMsgResDO::getId));
    }

    default List<WxFansMsgResDO> selectList(WxFansMsgResExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxFansMsgResDO>()
                .eqIfPresent(WxFansMsgResDO::getFansMsgId, reqVO.getFansMsgId())
                .eqIfPresent(WxFansMsgResDO::getResContent, reqVO.getResContent())
                .betweenIfPresent(WxFansMsgResDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansMsgResDO::getId));
    }

}
