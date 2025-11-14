type StorageType = 'localStorage' | 'sessionStorage';

interface StorageManagerOptions {
  prefix?: string;
  storageType?: StorageType;
}

interface StorageItem<T> {
  expiry?: number;
  value: T;
}

class StorageManager {
  private prefix: string;
  private storage: Storage;

  constructor({
    prefix = '',
    storageType = 'localStorage',
  }: StorageManagerOptions = {}) {
    this.prefix = prefix;
    this.storage =
      storageType === 'localStorage'
        ? window.localStorage
        : window.sessionStorage;
  }

  /**
   * 清除所有带前缀的存储项
   */
  clear(): void {
    const keysToRemove: string[] = [];
    for (let i = 0; i < this.storage.length; i++) {
      const key = this.storage.key(i);
      if (key && key.startsWith(this.prefix)) {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach((key) => this.storage.removeItem(key));
  }

  /**
   * 清除所有过期的存储项
   */
  clearExpiredItems(): void {
    for (let i = 0; i < this.storage.length; i++) {
      const key = this.storage.key(i);
      if (key && key.startsWith(this.prefix)) {
        const shortKey = key.replace(this.prefix, '');
        this.getItem(shortKey); // 调用 getItem 方法检查并移除过期项
      }
    }
  }

  /**
   * 获取存储项
   * @param key 键
   * @param defaultValue 当项不存在或已过期时返回的默认值
   * @returns 值，如果项已过期或解析错误则返回默认值
   */
  getItem<T>(key: string, defaultValue: null | T = null): null | T {
    const fullKey = this.getFullKey(key);
    const itemStr = this.storage.getItem(fullKey);
    if (!itemStr) {
      return defaultValue;
    }

    try {
      const item: StorageItem<T> = JSON.parse(itemStr);
      if (item.expiry && Date.now() > item.expiry) {
        this.storage.removeItem(fullKey);
        return defaultValue;
      }
      return item.value;
    } catch (error) {
      console.error(`Error parsing item with key "${fullKey}":`, error);
      this.storage.removeItem(fullKey); // 如果解析失败，删除该项
      return defaultValue;
    }
  }

  /**
   * 移除存储项
   * @param key 键
   */
  removeItem(key: string): void {
    const fullKey = this.getFullKey(key);
    this.storage.removeItem(fullKey);
  }

  /**
   * 设置存储项
   * @param key 键
   * @param value 值
   * @param ttl 存活时间（毫秒）
   */
  setItem<T>(key: string, value: T, ttl?: number): void {
    const fullKey = this.getFullKey(key);
    const expiry = ttl ? Date.now() + ttl : undefined;
    const item: StorageItem<T> = { expiry, value };
    try {
      this.storage.setItem(fullKey, JSON.stringify(item));
    } catch (error) {
      console.error(`Error setting item with key "${fullKey}":`, error);
    }
  }

  /**
   * 获取完整的存储键
   * @param key 原始键
   * @returns 带前缀的完整键
   */
  private getFullKey(key: string): string {
    return `${this.prefix}-${key}`;
  }
}

export { StorageManager };
