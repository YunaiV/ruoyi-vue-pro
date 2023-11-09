package cn.iocoder.yudao.module.infra.service.demo02;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentAddressDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 学生 Service 接口
 *
 * @author 芋道源码
 */
public interface InfraDemoStudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemoStudent(@Valid InfraDemoStudentCreateReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateDemoStudent(@Valid InfraDemoStudentUpdateReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteDemoStudent(Long id);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    InfraDemoStudentDO getDemoStudent(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<InfraDemoStudentDO> getDemoStudentPage(InfraDemoStudentPageReqVO pageReqVO);

    /**
     * 获得学生列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 学生列表
     */
    List<InfraDemoStudentDO> getDemoStudentList(InfraDemoStudentExportReqVO exportReqVO);


    // ==================== 子表（学生联系人） ====================

    /**
     * 获得学生联系人列表
     *
     * @param studentId 学生编号
     * @return 学生联系人列表
     */
    List<InfraDemoStudentContactDO> getDemoStudentContactListByStudentId(Long studentId);


    // ==================== 子表（学生地址） ====================

    /**
     * 获得学生地址
     *
     * @param studentId 学生编号
     * @return 学生地址
     */
    InfraDemoStudentAddressDO getDemoStudentAddressByStudentId(Long studentId);

}