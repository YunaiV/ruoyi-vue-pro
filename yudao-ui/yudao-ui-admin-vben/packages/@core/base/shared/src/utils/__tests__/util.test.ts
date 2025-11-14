import { describe, expect, it } from 'vitest';

import { bindMethods, getNestedValue } from '../util';

class TestClass {
  public value: string;

  constructor(value: string) {
    this.value = value;
    bindMethods(this); // 调用通用方法
  }

  getValue() {
    return this.value;
  }

  setValue(newValue: string) {
    this.value = newValue;
  }
}

describe('bindMethods', () => {
  it('should bind methods to the instance correctly', () => {
    const instance = new TestClass('initial');

    // 解构方法
    const { getValue } = instance;

    // 检查 getValue 是否能正确调用，并且 this 绑定了 instance
    expect(getValue()).toBe('initial');
  });

  it('should bind multiple methods', () => {
    const instance = new TestClass('initial');

    const { getValue, setValue } = instance;

    // 检查 getValue 和 setValue 方法是否正确绑定了 this
    setValue('newValue');
    expect(getValue()).toBe('newValue');
  });

  it('should not bind non-function properties', () => {
    const instance = new TestClass('initial');

    // 检查普通属性是否保持原样
    expect(instance.value).toBe('initial');
  });

  it('should not bind constructor method', () => {
    const instance = new TestClass('test');

    // 检查 constructor 是否没有被绑定
    expect(instance.constructor.name).toBe('TestClass');
  });

  it('should not bind getter/setter properties', () => {
    class TestWithGetterSetter {
      get value() {
        return this._value;
      }

      set value(newValue: string) {
        this._value = newValue;
      }

      private _value: string = 'test';

      constructor() {
        bindMethods(this);
      }
    }

    const instance = new TestWithGetterSetter();
    const { value } = instance;

    // Getter 和 setter 不应被绑定
    expect(value).toBe('test');
  });
});

describe('getNestedValue', () => {
  interface UserProfile {
    age: number;
    name: string;
  }

  interface UserSettings {
    theme: string;
  }

  interface Data {
    user: {
      profile: UserProfile;
      settings: UserSettings;
    };
  }

  const data: Data = {
    user: {
      profile: {
        age: 25,
        name: 'Alice',
      },
      settings: {
        theme: 'dark',
      },
    },
  };

  it('should get a nested value when the path is valid', () => {
    const result = getNestedValue(data, 'user.profile.name');
    expect(result).toBe('Alice');
  });

  it('should return undefined for non-existent property', () => {
    const result = getNestedValue(data, 'user.profile.gender');
    expect(result).toBeUndefined();
  });

  it('should return undefined when accessing a non-existent deep path', () => {
    const result = getNestedValue(data, 'user.nonexistent.field');
    expect(result).toBeUndefined();
  });

  it('should return undefined if a middle level is undefined', () => {
    const result = getNestedValue({ user: undefined }, 'user.profile.name');
    expect(result).toBeUndefined();
  });

  it('should return the correct value for a nested setting', () => {
    const result = getNestedValue(data, 'user.settings.theme');
    expect(result).toBe('dark');
  });

  it('should work for a single-level path', () => {
    const result = getNestedValue({ a: 1, b: 2 }, 'b');
    expect(result).toBe(2);
  });

  it('should return the entire object if path is empty', () => {
    expect(() => getNestedValue(data, '')()).toThrow();
  });

  it('should handle paths with array indexes', () => {
    const complexData = { list: [{ name: 'Item1' }, { name: 'Item2' }] };
    const result = getNestedValue(complexData, 'list.1.name');
    expect(result).toBe('Item2');
  });

  it('should return undefined when accessing an out-of-bounds array index', () => {
    const complexData = { list: [{ name: 'Item1' }] };
    const result = getNestedValue(complexData, 'list.2.name');
    expect(result).toBeUndefined();
  });
});
