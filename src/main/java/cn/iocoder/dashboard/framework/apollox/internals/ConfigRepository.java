package cn.iocoder.dashboard.framework.apollox.internals;

import java.util.Properties;

/**
 * 配置 Repository 接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRepository {

    /**
     * Get the config from this repository.
     * <p>
     * 获得配置，以 Properties 对象返回
     *
     * @return config
     */
    Properties getConfig();

    /**
     * Add change listener.
     *
     * @param listener the listener to observe the changes
     */
    void addChangeListener(RepositoryChangeListener listener);

    /**
     * Remove change listener.
     *
     * @param listener the listener to remove
     */
    void removeChangeListener(RepositoryChangeListener listener);

}
