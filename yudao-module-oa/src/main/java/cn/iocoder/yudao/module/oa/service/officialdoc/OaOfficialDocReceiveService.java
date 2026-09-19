package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceivePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceiveSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocReceiveDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO;

/**
 * 公文收文 Service 接口
 *
 * @author 芋道源码
 */
public interface OaOfficialDocReceiveService {

    /**
     * 创建公文收文
     *
     * @param reqVO 公文收文信息
     * @return 公文收文编号
     */
    Long createOfficialDocReceive(OaOfficialDocReceiveSaveReqVO reqVO);

    /**
     * 根据已通过的发文创建部门收文
     *
     * @param send 发文
     */
    void createOfficialDocReceiveListByOfficialDocSend(OaOfficialDocSendDO send);

    /**
     * 更新公文收文
     *
     * @param reqVO 公文收文信息
     * @param userId 操作用户编号
     */
    void updateOfficialDocReceive(OaOfficialDocReceiveSaveReqVO reqVO, Long userId);

    /**
     * 签收公文
     *
     * @param id 收文编号
     * @param userId 签收人编号
     */
    void claimOfficialDocReceive(Long id, Long userId);

    /**
     * 提交公文收文审批
     *
     * @param id 单据编号
     * @param userId 操作用户编号
     * @return 流程实例编号
     */
    String submitOfficialDocReceive(Long id, Long userId);

    /**
     * 撤销公文收文审批
     *
     * @param id 单据编号
     * @param userId 操作用户编号
     */
    void cancelOfficialDocReceive(Long id, Long userId);

    /**
     * 更新公文收文审批结果
     *
     * @param id 单据编号
     * @param status 审批状态
     */
    void updateOfficialDocReceiveStatus(Long id, Integer status);

    /**
     * 删除公文收文
     *
     * @param id 公文收文编号
     * @param userId 操作用户编号
     */
    void deleteOfficialDocReceive(Long id, Long userId);

    /**
     * 获得公文收文分页
     *
     * @param reqVO 分页条件
     * @param userId 操作用户编号
     * @return 公文收文分页
     */
    PageResult<OaOfficialDocReceiveDO> getOfficialDocReceivePage(OaOfficialDocReceivePageReqVO reqVO, Long userId);

    /**
     * 获得公文收文
     *
     * @param id 公文收文编号
     * @return 公文收文
     */
    OaOfficialDocReceiveDO getOfficialDocReceive(Long id);

}
