package cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SysSmsChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsChannelMapper extends BaseMapper<SysSmsChannelDO> {

    default IPage<SysSmsChannelDO> selectChannelPage(SmsChannelPageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO), new LambdaQueryWrapper<SysSmsChannelDO>()
                .like(StrUtil.isNotBlank(reqVO.getName()), SysSmsChannelDO::getName, reqVO.getName())
                .like(StrUtil.isNotBlank(reqVO.getSignature()), SysSmsChannelDO::getName, reqVO.getSignature())
        );
    }

    default List<SysSmsChannelDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapper<SysSmsChannelDO>()
                .eq(SysSmsChannelDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SysSmsChannelDO::getId)
        );
    }
}
