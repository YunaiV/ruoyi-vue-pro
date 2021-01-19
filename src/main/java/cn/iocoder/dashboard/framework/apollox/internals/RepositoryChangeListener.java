package cn.iocoder.dashboard.framework.apollox.internals;

import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RepositoryChangeListener {

    /**
     * Invoked when config repository changes.
     *
     * @param namespace     the namespace of this repository change
     * @param newProperties the properties after change
     */
    void onRepositoryChange(String namespace, Properties newProperties);

}
