package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;

import java.util.Collection;
import java.util.List;

/**
 * 会员等级记录 Service 接口
 *
 * @author owen
 */
public interface MemberLevelLogService {

    /**
     * 删除会员等级记录
     *
     * @param id 编号
     */
    void deleteLevelLog(Long id);

    /**
     * 获得会员等级记录
     *
     * @param id 编号
     * @return 会员等级记录
     */
    MemberLevelLogDO getLevelLog(Long id);

    /**
     * 获得会员等级记录列表
     *
     * @param ids 编号
     * @return 会员等级记录列表
     */
    List<MemberLevelLogDO> getLevelLogList(Collection<Long> ids);

    /**
     * 获得会员等级记录分页
     *
     * @param pageReqVO 分页查询
     * @return 会员等级记录分页
     */
    PageResult<MemberLevelLogDO> getLevelLogPage(MemberLevelLogPageReqVO pageReqVO);

    /**
     * 获得会员等级记录列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 会员等级记录列表
     */
    List<MemberLevelLogDO> getLevelLogList(MemberLevelLogExportReqVO exportReqVO);

}
