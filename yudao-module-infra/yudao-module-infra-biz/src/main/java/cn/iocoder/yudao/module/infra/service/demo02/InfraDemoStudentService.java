package cn.iocoder.yudao.module.infra.service.demo02;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

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


}