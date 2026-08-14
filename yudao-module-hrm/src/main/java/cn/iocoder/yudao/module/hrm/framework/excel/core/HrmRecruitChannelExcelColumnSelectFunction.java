package cn.iocoder.yudao.module.hrm.framework.excel.core;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 招聘渠道下拉框数据源的 {@link ExcelColumnSelectFunction} 实现类
 *
 * @author 芋道源码
 */
@Service
public class HrmRecruitChannelExcelColumnSelectFunction implements ExcelColumnSelectFunction {

    public static final String NAME = "getHrmRecruitChannelNameList";

    @Resource
    private HrmRecruitChannelService recruitChannelService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getOptions() {
        return convertList(recruitChannelService.getRecruitChannelSimpleList(),
                HrmRecruitChannelExcelColumnSelectFunction::formatOption);
    }

    /**
     * 格式化招聘渠道 Excel 下拉选项
     *
     * @param channel 招聘渠道
     * @return Excel 下拉选项
     */
    public static String formatOption(HrmRecruitChannelDO channel) {
        return channel.getName() + "（" + channel.getId() + "）";
    }

    /**
     * 解析招聘渠道 Excel 下拉选项
     *
     * @param channels 可选招聘渠道
     * @param option Excel 选项
     * @return 招聘渠道；选项不存在时返回 {@code null}
     */
    public static HrmRecruitChannelDO parseOption(List<HrmRecruitChannelDO> channels, String option) {
        return CollUtil.findOne(channels, item -> formatOption(item).equals(option));
    }

}
