package cn.iocoder.yudao.module.mp.dal.mysql.receivetext;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.receivetext.WxReceiveTextDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo.*;

/**
 * 回复关键字 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxReceiveTextMapper extends BaseMapperX<WxReceiveTextDO> {

    default PageResult<WxReceiveTextDO> selectPage(WxReceiveTextPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxReceiveTextDO>()
                .eqIfPresent(WxReceiveTextDO::getReceiveText, reqVO.getReceiveText())
                .eqIfPresent(WxReceiveTextDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxReceiveTextDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxReceiveTextDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxReceiveTextDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxReceiveTextDO::getId));
    }

    default List<WxReceiveTextDO> selectList(WxReceiveTextExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxReceiveTextDO>()
                .eqIfPresent(WxReceiveTextDO::getReceiveText, reqVO.getReceiveText())
                .eqIfPresent(WxReceiveTextDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxReceiveTextDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxReceiveTextDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxReceiveTextDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxReceiveTextDO::getId));
    }

}
