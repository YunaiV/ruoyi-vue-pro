package cn.iocoder.yudao.module.system.dal.redis.oauth2;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.OAUTH2_ACCESS_TOKEN;

/**
 * {@link OAuth2AccessTokenDO} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class OAuth2AccessTokenRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public OAuth2AccessTokenDO get(String accessToken) {
        String redisKey = formatKey(accessToken);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), OAuth2AccessTokenDO.class);
    }

    public void set(OAuth2AccessTokenDO accessTokenDO) {
        String redisKey = formatKey(accessTokenDO.getAccessToken());
        // 清理多余字段，避免缓存
        accessTokenDO.setUpdater(null).setUpdateTime(null).setCreateTime(null).setCreator(null).setDeleted(null);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(accessTokenDO),
                accessTokenDO.getExpiresTime().getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public void delete(String accessToken) {
        String redisKey = formatKey(accessToken);
        stringRedisTemplate.delete(redisKey);
    }

    public void deleteList(Collection<String> accessTokens) {
        List<String> redisKeys = CollectionUtils.convertList(accessTokens, OAuth2AccessTokenRedisDAO::formatKey);
        stringRedisTemplate.delete(redisKeys);
    }

    private static String formatKey(String accessToken) {
        return String.format(OAUTH2_ACCESS_TOKEN.getKeyTemplate(), accessToken);
    }

}
