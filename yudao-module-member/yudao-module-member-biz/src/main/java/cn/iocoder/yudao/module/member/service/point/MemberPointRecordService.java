package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户积分记录 Service 接口
 *
 * @author QingX
 */
public interface MemberPointRecordService {


    /**
     * 更新用户积分记录
     *
     * @param updateReqVO 更新信息
     */
    void updateRecord(@Valid MemberPointRecordUpdateReqVO updateReqVO);



    /**
     * 获得用户积分记录
     *
     * @param id 编号
     * @return 用户积分记录
     */
    MemberPointRecordDO getRecord(Long id);

    /**
     * 获得用户积分记录列表
     *
     * @param ids 编号
     * @return 用户积分记录列表
     */
    List<MemberPointRecordDO> getRecordList(Collection<Long> ids);

    /**
     * 获得用户积分记录分页
     *
     * @param pageReqVO 分页查询
     * @return 用户积分记录分页
     */
    PageResult<MemberPointRecordDO> getRecordPage(MemberPointRecordPageReqVO pageReqVO);

    /**
     * 获得用户积分记录列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户积分记录列表
     */
    List<MemberPointRecordDO> getRecordList(MemberPointRecordExportReqVO exportReqVO);

}
