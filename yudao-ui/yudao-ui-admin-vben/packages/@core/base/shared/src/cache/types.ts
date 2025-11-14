type StorageType = 'localStorage' | 'sessionStorage';

interface StorageValue<T> {
  data: T;
  expiry: null | number;
}

interface IStorageCache {
  clear(): void;
  getItem<T>(key: string): null | T;
  key(index: number): null | string;
  length(): number;
  removeItem(key: string): void;
  setItem<T>(key: string, value: T, expiryInMinutes?: number): void;
}

export type { IStorageCache, StorageType, StorageValue };
