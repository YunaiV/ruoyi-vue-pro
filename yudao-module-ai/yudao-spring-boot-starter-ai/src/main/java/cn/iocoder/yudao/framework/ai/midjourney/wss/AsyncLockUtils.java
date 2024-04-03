package cn.iocoder.yudao.framework.ai.midjourney.wss;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.framework.ai.midjourney.jad.DomainObject;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@UtilityClass
public class AsyncLockUtils {
	private static final TimedCache<String, LockObject> LOCK_MAP = CacheUtil.newTimedCache(Duration.ofDays(1).toMillis());

	public static synchronized LockObject getLock(String key) {
		return LOCK_MAP.get(key);
	}

	public static LockObject waitForLock(String key, Duration duration) throws TimeoutException {
		LockObject lockObject;
		synchronized (LOCK_MAP) {
			if (LOCK_MAP.containsKey(key)) {
				lockObject = LOCK_MAP.get(key);
			} else {
				lockObject = new LockObject(key);
				LOCK_MAP.put(key, lockObject);
			}
		}
		Future<?> future = ThreadUtil.execAsync(() -> {
			try {
				lockObject.sleep();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		try {
			future.get(duration.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			// do nothing
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new TimeoutException("Wait Timeout");
		} finally {
			LOCK_MAP.remove(lockObject.getId());
		}
		return lockObject;
	}

	public static class LockObject extends DomainObject {

		public LockObject(String id) {
			this.id = id;
		}
	}
}
