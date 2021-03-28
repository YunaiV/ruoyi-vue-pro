package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsChannelMapper extends BaseMapperX<SysSmsChannelDO> {

    default PageResult<SysSmsChannelDO> selectChannelPage(SmsChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapper<SysSmsChannelDO>()
                .like(StrUtil.isNotBlank(reqVO.getSignature()), SysSmsChannelDO::getSignature, reqVO.getSignature()));
    }

    default List<SysSmsChannelDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapper<SysSmsChannelDO>()
                .eq(SysSmsChannelDO::getStatus, status)
                .orderByAsc(SysSmsChannelDO::getId));
    }

}
