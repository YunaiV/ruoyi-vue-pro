// pathUtils.test.ts

import { describe, expect, it } from 'vitest';

import { toPosixPath } from '../path';

describe('toPosixPath', () => {
  // 测试 Windows 风格路径到 POSIX 风格路径的转换
  it('converts Windows-style paths to POSIX paths', () => {
    const windowsPath = String.raw`C:\Users\Example\file.txt`;
    const expectedPosixPath = 'C:/Users/Example/file.txt';
    expect(toPosixPath(windowsPath)).toBe(expectedPosixPath);
  });

  // 确认 POSIX 风格路径不会被改变
  it('leaves POSIX-style paths unchanged', () => {
    const posixPath = '/home/user/file.txt';
    expect(toPosixPath(posixPath)).toBe(posixPath);
  });

  // 测试带有多个分隔符的路径
  it('converts paths with mixed separators', () => {
    const mixedPath = String.raw`C:/Users\Example\file.txt`;
    const expectedPosixPath = 'C:/Users/Example/file.txt';
    expect(toPosixPath(mixedPath)).toBe(expectedPosixPath);
  });

  // 测试空字符串
  it('handles empty strings', () => {
    const emptyPath = '';
    expect(toPosixPath(emptyPath)).toBe('');
  });

  // 测试仅包含分隔符的路径
  it('handles path with only separators', () => {
    const separatorsPath = '\\\\\\';
    const expectedPosixPath = '///';
    expect(toPosixPath(separatorsPath)).toBe(expectedPosixPath);
  });

  // 测试不包含任何分隔符的路径
  it('handles path without separators', () => {
    const noSeparatorPath = 'file.txt';
    expect(toPosixPath(noSeparatorPath)).toBe('file.txt');
  });

  // 测试以分隔符结尾的路径
  it('handles path ending with a separator', () => {
    const endingSeparatorPath = 'C:\\Users\\Example\\';
    const expectedPosixPath = 'C:/Users/Example/';
    expect(toPosixPath(endingSeparatorPath)).toBe(expectedPosixPath);
  });

  // 测试以分隔符开头的路径
  it('handles path starting with a separator', () => {
    const startingSeparatorPath = String.raw`\Users\Example`;
    const expectedPosixPath = '/Users/Example';
    expect(toPosixPath(startingSeparatorPath)).toBe(expectedPosixPath);
  });

  // 测试包含非法字符的路径
  it('handles path with invalid characters', () => {
    const invalidCharsPath = String.raw`C:\Us*?ers\Ex<ample>|file.txt`;
    const expectedPosixPath = 'C:/Us*?ers/Ex<ample>|file.txt';
    expect(toPosixPath(invalidCharsPath)).toBe(expectedPosixPath);
  });
});
