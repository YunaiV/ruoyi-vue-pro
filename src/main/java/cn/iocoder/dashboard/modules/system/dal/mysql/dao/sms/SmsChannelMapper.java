package cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SmsChannelMapper extends BaseMapper<SmsChannelDO> {

    default IPage<SmsChannelDO> selectChannelPage(SmsChannelPageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO), new LambdaQueryWrapper<SmsChannelDO>()
                .like(StrUtil.isNotBlank(reqVO.getName()), SmsChannelDO::getName, reqVO.getName())
                .like(StrUtil.isNotBlank(reqVO.getSignature()), SmsChannelDO::getName, reqVO.getSignature())
        );
    }

    default List<SmsChannelDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapper<SmsChannelDO>()
                .eq(SmsChannelDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SmsChannelDO::getId)
        );
    }
}
