package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.config.HrmRecruitEliminateReasonSaveReqVO;

import javax.validation.Valid;
import java.util.List;

/**
 * HRM 招聘设置 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitConfigService {

    /**
     * 保存招聘淘汰原因
     *
     * @param saveReqVO 淘汰原因列表
     */
    void saveRecruitEliminateReason(@Valid HrmRecruitEliminateReasonSaveReqVO saveReqVO);

    /**
     * 获得招聘淘汰原因列表
     *
     * @return 淘汰原因列表
     */
    List<String> getRecruitEliminateReasonList();

}
