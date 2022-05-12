package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.OAuth2ClientMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.OAUTH2_CLIENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link OAuth2ClientServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(OAuth2ClientServiceImpl.class)
public class OAuth2ClientServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OAuth2ClientServiceImpl oAuth2ClientService;

    @Resource
    private OAuth2ClientMapper oAuth2ClientMapper;

    @Test
    public void testCreateOAuth2Client_success() {
        // 准备参数
        OAuth2ClientCreateReqVO reqVO = randomPojo(OAuth2ClientCreateReqVO.class);

        // 调用
        Long oauth2ClientId = oAuth2ClientService.createOAuth2Client(reqVO);
        // 断言
        assertNotNull(oauth2ClientId);
        // 校验记录的属性是否正确
        OAuth2ClientDO oAuth2Client = oAuth2ClientMapper.selectById(oauth2ClientId);
        assertPojoEquals(reqVO, oAuth2Client);
    }

    @Test
    public void testUpdateOAuth2Client_success() {
        // mock 数据
        OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class);
        oAuth2ClientMapper.insert(dbOAuth2Client);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OAuth2ClientUpdateReqVO reqVO = randomPojo(OAuth2ClientUpdateReqVO.class, o -> {
            o.setId(dbOAuth2Client.getId()); // 设置更新的 ID
        });

        // 调用
        oAuth2ClientService.updateOAuth2Client(reqVO);
        // 校验是否更新正确
        OAuth2ClientDO oAuth2Client = oAuth2ClientMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, oAuth2Client);
    }

    @Test
    public void testUpdateOAuth2Client_notExists() {
        // 准备参数
        OAuth2ClientUpdateReqVO reqVO = randomPojo(OAuth2ClientUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> oAuth2ClientService.updateOAuth2Client(reqVO), OAUTH2_CLIENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteOAuth2Client_success() {
        // mock 数据
        OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class);
        oAuth2ClientMapper.insert(dbOAuth2Client);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOAuth2Client.getId();

        // 调用
        oAuth2ClientService.deleteOAuth2Client(id);
       // 校验数据不存在了
       assertNull(oAuth2ClientMapper.selectById(id));
    }

    @Test
    public void testDeleteOAuth2Client_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> oAuth2ClientService.deleteOAuth2Client(id), OAUTH2_CLIENT_NOT_EXISTS);
    }

    @Test
    @Disabled
    public void testGetOAuth2ClientPage() {
       // mock 数据
       OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class, o -> { // 等会查询到
           o.setName("潜龙");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
       });
       oAuth2ClientMapper.insert(dbOAuth2Client);
       // 测试 name 不匹配
       oAuth2ClientMapper.insert(cloneIgnoreId(dbOAuth2Client, o -> o.setName("凤凰")));
       // 测试 status 不匹配
       oAuth2ClientMapper.insert(cloneIgnoreId(dbOAuth2Client, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())));
       // 准备参数
       OAuth2ClientPageReqVO reqVO = new OAuth2ClientPageReqVO();
       reqVO.setName("long");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

       // 调用
       PageResult<OAuth2ClientDO> pageResult = oAuth2ClientService.getOAuth2ClientPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOAuth2Client, pageResult.getList().get(0));
    }

}
