package cn.iocoder.dashboard.modules.msg.dal.mysql.dao.sms;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.modules.msg.dal.mysql.daoobject.sms.SmsTemplateDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SmsTemplateMapper extends BaseMapper<SmsTemplateDO> {

    /**
     * 根据短信渠道id查询短信模板集合
     *
     * @param channelId 渠道id
     * @return 模板集合
     */
    default List<SmsTemplateDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapper<SmsTemplateDO>()
                .eq(SmsTemplateDO::getChannelId, channelId)
                .eq(SmsTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SmsTemplateDO::getId)
        );
    }

    /**
     * 查询有效短信模板集合
     *
     * @return 有效短信模板集合
     */
    default List<SmsTemplateDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapper<SmsTemplateDO>()
                .eq(SmsTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SmsTemplateDO::getId)
        );
    }
}
