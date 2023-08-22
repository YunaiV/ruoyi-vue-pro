package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;

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

    // TODO @疯狂：把 createCancelLog、createAdjustLog、createAutoUpgradeLog 几个日志合并成一个通用的日志方法；整体的内容，交给 MemberLevelService 去做；以及对应的 level 变化的通知；

    /**
     * 创建记录： 取消等级
     *
     * @param userId 会员编号
     * @param reason 调整原因
     */
    void createCancelLog(Long userId, String reason);

    /**
     * 创建记录： 手动调整
     *
     * @param user       会员
     * @param level      等级
     * @param experience 变动经验值
     * @param reason     调整原因
     */
    void createAdjustLog(MemberUserDO user, MemberLevelDO level, int experience, String reason);

    /**
     * 创建记录： 自动升级
     *
     * @param user  会员
     * @param level 等级
     */
    void createAutoUpgradeLog(MemberUserDO user, MemberLevelDO level);
}
