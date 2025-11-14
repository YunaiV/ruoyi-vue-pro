import { describe, expect, it } from 'vitest';

import {
  capitalizeFirstLetter,
  kebabToCamelCase,
  toCamelCase,
  toLowerCaseFirstLetter,
} from '../letter';

describe('capitalizeFirstLetter', () => {
  it('should capitalize the first letter of a string', () => {
    expect(capitalizeFirstLetter('hello')).toBe('Hello');
    expect(capitalizeFirstLetter('world')).toBe('World');
  });

  it('should handle empty strings', () => {
    expect(capitalizeFirstLetter('')).toBe('');
  });

  it('should handle single character strings', () => {
    expect(capitalizeFirstLetter('a')).toBe('A');
    expect(capitalizeFirstLetter('b')).toBe('B');
  });

  it('should not change the case of other characters', () => {
    expect(capitalizeFirstLetter('hElLo')).toBe('HElLo');
  });
});

describe('toLowerCaseFirstLetter', () => {
  it('should convert the first letter to lowercase', () => {
    expect(toLowerCaseFirstLetter('CommonAppName')).toBe('commonAppName');
    expect(toLowerCaseFirstLetter('AnotherKeyExample')).toBe(
      'anotherKeyExample',
    );
  });

  it('should return the same string if the first letter is already lowercase', () => {
    expect(toLowerCaseFirstLetter('alreadyLowerCase')).toBe('alreadyLowerCase');
  });

  it('should handle empty strings', () => {
    expect(toLowerCaseFirstLetter('')).toBe('');
  });

  it('should handle single character strings', () => {
    expect(toLowerCaseFirstLetter('A')).toBe('a');
    expect(toLowerCaseFirstLetter('a')).toBe('a');
  });

  it('should handle strings with only one uppercase letter', () => {
    expect(toLowerCaseFirstLetter('A')).toBe('a');
  });

  it('should handle strings with special characters', () => {
    expect(toLowerCaseFirstLetter('!Special')).toBe('!Special');
    expect(toLowerCaseFirstLetter('123Number')).toBe('123Number');
  });
});

describe('toCamelCase', () => {
  it('should return the key if parentKey is empty', () => {
    expect(toCamelCase('child', '')).toBe('child');
  });

  it('should combine parentKey and key in camel case', () => {
    expect(toCamelCase('child', 'parent')).toBe('parentChild');
  });

  it('should handle empty key and parentKey', () => {
    expect(toCamelCase('', '')).toBe('');
  });

  it('should handle key with capital letters', () => {
    expect(toCamelCase('Child', 'parent')).toBe('parentChild');
    expect(toCamelCase('Child', 'Parent')).toBe('ParentChild');
  });
});

describe('kebabToCamelCase', () => {
  it('should convert kebab-case to camelCase correctly', () => {
    expect(kebabToCamelCase('my-component-name')).toBe('myComponentName');
  });

  it('should handle multiple consecutive hyphens', () => {
    expect(kebabToCamelCase('my--component--name')).toBe('myComponentName');
  });

  it('should trim leading and trailing hyphens', () => {
    expect(kebabToCamelCase('-my-component-name-')).toBe('myComponentName');
  });

  it('should preserve the case of the first word', () => {
    expect(kebabToCamelCase('My-component-name')).toBe('MyComponentName');
  });

  it('should convert a single word correctly', () => {
    expect(kebabToCamelCase('component')).toBe('component');
  });

  it('should return an empty string if input is empty', () => {
    expect(kebabToCamelCase('')).toBe('');
  });

  it('should handle strings with no hyphens', () => {
    expect(kebabToCamelCase('mycomponentname')).toBe('mycomponentname');
  });

  it('should handle strings with only hyphens', () => {
    expect(kebabToCamelCase('---')).toBe('');
  });

  it('should handle mixed case inputs', () => {
    expect(kebabToCamelCase('my-Component-Name')).toBe('myComponentName');
  });
});
