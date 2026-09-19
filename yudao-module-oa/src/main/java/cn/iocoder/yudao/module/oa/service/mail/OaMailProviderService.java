package cn.iocoder.yudao.module.oa.service.mail;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider.OaMailProviderSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailProviderDO;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 企业邮箱服务配置 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMailProviderService {

    /**
     * 创建邮箱服务配置
     *
     * @param reqVO 邮箱服务配置信息
     * @return 邮箱服务配置编号
     */
    Long createMailProvider(@Valid OaMailProviderSaveReqVO reqVO);

    /**
     * 更新邮箱服务配置
     *
     * @param reqVO 邮箱服务配置信息
     */
    void updateMailProvider(@Valid OaMailProviderSaveReqVO reqVO);

    /**
     * 删除邮箱服务配置
     *
     * 仅允许删除未被邮箱账号引用的服务配置。
     *
     * @param id 邮箱服务配置编号
     */
    void deleteMailProvider(Long id);

    /**
     * 校验邮箱服务配置存在
     *
     * @param id 邮箱服务配置编号
     * @return 邮箱服务配置
     */
    OaMailProviderDO validateMailProviderExists(Long id);

    /**
     * 获得邮箱服务配置列表
     *
     * @param status 状态，允许为空，为空时查询全部状态
     * @return 邮箱服务配置列表
     */
    List<OaMailProviderDO> getMailProviderList(Integer status);

}
