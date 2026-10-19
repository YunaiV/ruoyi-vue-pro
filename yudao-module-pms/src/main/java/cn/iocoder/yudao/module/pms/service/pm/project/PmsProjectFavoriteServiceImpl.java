package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectFavoriteDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectFavoriteMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * PMS 项目收藏 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsProjectFavoriteServiceImpl implements PmsProjectFavoriteService {

    @Resource
    private PmsProjectFavoriteMapper favoriteMapper;
    @Resource
    private PmsProjectMemberService projectMemberService;

    @Override
    public void createProjectFavorite(Long projectId, Long userId) {
        // 1. 校验用户是项目成员
        projectMemberService.validateProjectMember(projectId, userId);

        // 2. 创建收藏关系，重复请求保持幂等
        if (favoriteMapper.selectByProjectIdAndUserId(projectId, userId) != null) {
            return;
        }
        favoriteMapper.insert(new PmsProjectFavoriteDO().setProjectId(projectId).setUserId(userId));
    }

    @Override
    public void deleteProjectFavorite(Long projectId, Long userId) {
        // 1. 校验用户是项目成员
        projectMemberService.validateProjectMember(projectId, userId);

        // 2. 删除收藏关系，重复请求保持幂等
        PmsProjectFavoriteDO favorite = favoriteMapper.selectByProjectIdAndUserId(projectId, userId);
        if (favorite != null) {
            favoriteMapper.deleteById(favorite.getId());
        }
    }

    @Override
    public List<Long> getFavoriteProjectIdListByUserId(Long userId) {
        return favoriteMapper.selectProjectIdListByUserId(userId);
    }

    @Override
    public void deleteProjectFavoriteListByProjectId(Long projectId) {
        favoriteMapper.deleteByProjectIds(Collections.singleton(projectId));
    }

}
