package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysMenuMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单 Service 实现
 */
@Service
@Slf4j
public class SysMenuServiceImpl implements SysMenuService {

    /**
     * 菜单缓存
     * key：菜单编号
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, SysMenuDO> menuCache;
    /**
     * 权限与菜单缓存
     * key：权限 {@link SysMenuDO#getPerms()}
     * value：SysMenuDO 数组，因为一个权限可能对应多个 SysMenuDO 对象
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<String, SysMenuDO> permMenuCache;

    @Resource
    private SysMenuMapper menuMapper;

    /**
     * 初始化 {@link #menuCache} 和 {@link #permMenuCache} 缓存
     */
    @Override
    @PostConstruct
    public void init() {
        List<SysMenuDO> menuList = menuMapper.selectList(null);
        ImmutableMap.Builder<Long, SysMenuDO> menuCacheBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<String, SysMenuDO> permMenuCacheBuilder = ImmutableMultimap.builder();
        menuList.forEach(menuDO -> {
            menuCacheBuilder.put(menuDO.getMenuId(), menuDO);
            permMenuCacheBuilder.put(menuDO.getPerms(), menuDO);
        });
        menuCache = menuCacheBuilder.build();
        permMenuCache = permMenuCacheBuilder.build();
        log.info("[init][初始化菜单数量为 {}]", menuList.size());
    }

    @Override
    public List<SysMenuDO> listMenusFromCache() {
        // Guava ImmutableMap 对应的 value 类型为 ImmutableCollection
        // 而 ImmutableList 在 copyof 时，如果入参类型为 ImmutableCollection 时，会进行包装，而不会进行复制。
        return ImmutableList.copyOf(menuCache.values());
    }

    @Override
    public List<SysMenuDO> listMenusFromCache(Collection<Long> menuIds) {
        if (CollectionUtil.isEmpty(menuIds)) {
            return Collections.emptyList();
        }
        return menuCache.values().stream().filter(menuDO -> menuIds.contains(menuDO.getMenuId()))
                .collect(Collectors.toList());
    }
}
