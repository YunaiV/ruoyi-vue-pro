package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.iocoder.dashboard.common.enums.DefaultBitFieldEnum;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsSendLogDO;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsQueryLogMapper extends BaseMapper<SysSmsSendLogDO> {

    /**
     * 查询还没有获取发送结果的短信请求信息
     */
    default List<SysSmsSendLogDO> selectNoResultQueryLogList() {
        return this.selectList(new LambdaQueryWrapper<SysSmsSendLogDO>()
                .eq(SysSmsSendLogDO::getSendStatus, SysSmsSendStatusEnum.QUERY_SUCCESS)
                .eq(SysSmsSendLogDO::getGotResult, DefaultBitFieldEnum.NO)
        );
    }


    /**
     * 根据APIId修改对象
     */
    default boolean updateByApiId(SysSmsSendLogDO queryLogDO, String apiId) {
        return update(queryLogDO, new LambdaQueryWrapper<SysSmsSendLogDO>()
                .eq(SysSmsSendLogDO::getApiId, apiId)
        ) > 0;
    }
}
