package cn.iocoder.dashboard.framework.apollox.internals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 配置 Repository 抽象类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigRepository implements ConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigRepository.class);

    /**
     * RepositoryChangeListener 数组
     */
    private List<RepositoryChangeListener> m_listeners = new CopyOnWriteArrayList<>();

    /**
     * 尝试同步
     *
     * @return 是否同步成功
     */
    protected boolean trySync() {
        try {
            // 同步
            sync();
            // 返回同步成功
            return true;
        } catch (Throwable ex) {
//            Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
            logger.warn("Sync config failed, will retry. Repository {}", getClass(), ex);
        }
        // 返回同步失败
        return false;
    }

    /**
     * 同步配置
     */
    protected abstract void sync();

    @Override
    public void addChangeListener(RepositoryChangeListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(RepositoryChangeListener listener) {
        m_listeners.remove(listener);
    }

    /**
     * 触发监听器们
     *
     * @param namespace     Namespace 名字
     * @param newProperties 配置
     */
    protected void fireRepositoryChange(String namespace, Properties newProperties) {
        // 循环 RepositoryChangeListener 数组
        for (RepositoryChangeListener listener : m_listeners) {
            try {
                // 触发监听器
                listener.onRepositoryChange(namespace, newProperties);
            } catch (Throwable ex) {
//                Tracer.logError(ex);
                logger.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
            }
        }
    }

}
