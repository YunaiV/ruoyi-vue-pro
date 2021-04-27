package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysSmsChannelMapper extends BaseMapperX<SysSmsChannelDO> {

    default PageResult<SysSmsChannelDO> selectPage(SysSmsChannelPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysSmsChannelDO>()
                .likeIfPresent("signature", reqVO.getSignature())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    @Select("SELECT id FROM sys_sms_channel WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

}
