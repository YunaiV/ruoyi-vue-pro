import { describe, expect, it } from 'vitest';

import { diff } from '../diff';

describe('diff function', () => {
  it('should return an empty object when comparing identical objects', () => {
    const obj1 = { a: 1, b: { c: 2 } };
    const obj2 = { a: 1, b: { c: 2 } };
    expect(diff(obj1, obj2)).toEqual(undefined);
  });

  it('should detect simple changes in primitive values', () => {
    const obj1 = { a: 1, b: 2 };
    const obj2 = { a: 1, b: 3 };
    expect(diff(obj1, obj2)).toEqual({ b: 3 });
  });

  it('should detect nested object changes', () => {
    const obj1 = { a: 1, b: { c: 2, d: 4 } };
    const obj2 = { a: 1, b: { c: 3, d: 4 } };
    expect(diff(obj1, obj2)).toEqual({ b: { c: 3 } });
  });

  it('should handle array changes', () => {
    const obj1 = { a: [1, 2, 3], b: 2 };
    const obj2 = { a: [1, 2, 4], b: 2 };
    expect(diff(obj1, obj2)).toEqual({ a: [1, 2, 4] });
  });

  it('should handle added keys', () => {
    const obj1 = { a: 1 };
    const obj2 = { a: 1, b: 2 };
    expect(diff(obj1, obj2)).toEqual({ b: 2 });
  });

  it('should handle removed keys', () => {
    const obj1 = { a: 1, b: 2 };
    const obj2 = { a: 1 };
    expect(diff(obj1, obj2)).toEqual(undefined);
  });

  it('should handle boolean value changes', () => {
    const obj1 = { a: true, b: false };
    const obj2 = { a: true, b: true };
    expect(diff(obj1, obj2)).toEqual({ b: true });
  });

  it('should handle null and undefined values', () => {
    const obj1 = { a: null, b: undefined };
    const obj2: any = { a: 1, b: undefined };
    expect(diff(obj1, obj2)).toEqual({ a: 1 });
  });
});
