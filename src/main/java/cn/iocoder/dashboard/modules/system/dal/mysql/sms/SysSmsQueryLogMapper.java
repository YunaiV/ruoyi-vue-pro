package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.iocoder.dashboard.common.enums.DefaultBitFieldEnum;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsQueryLogDO;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsQueryLogMapper extends BaseMapper<SysSmsQueryLogDO> {

    /**
     * 查询还没有获取发送结果的短信请求信息
     */
    default List<SysSmsQueryLogDO> selectNoResultQueryLogList() {
        return this.selectList(new LambdaQueryWrapper<SysSmsQueryLogDO>()
                .eq(SysSmsQueryLogDO::getSendStatus, SmsSendStatusEnum.QUERY_SUCCESS)
                .eq(SysSmsQueryLogDO::getGotResult, DefaultBitFieldEnum.NO)
        );
    }


    /**
     * 根据APIId修改对象
     */
    default boolean updateByApiId(SysSmsQueryLogDO queryLogDO, String apiId) {
        return update(queryLogDO, new LambdaQueryWrapper<SysSmsQueryLogDO>()
                .eq(SysSmsQueryLogDO::getApiId, apiId)
        ) > 0;
    }
}
