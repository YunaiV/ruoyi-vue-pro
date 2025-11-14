import { createHash } from 'node:crypto';

import { describe, expect, it } from 'vitest';

import { generatorContentHash } from '../hash';

describe('generatorContentHash', () => {
  it('should generate an MD5 hash for the content', () => {
    const content = 'example content';
    const expectedHash = createHash('md5')
      .update(content, 'utf8')
      .digest('hex');
    const actualHash = generatorContentHash(content);
    expect(actualHash).toBe(expectedHash);
  });

  it('should generate an MD5 hash with specified length', () => {
    const content = 'example content';
    const hashLength = 10;
    const generatedHash = generatorContentHash(content, hashLength);
    expect(generatedHash).toHaveLength(hashLength);
  });

  it('should correctly generate the hash with specified length', () => {
    const content = 'example content';
    const hashLength = 8;
    const expectedHash = createHash('md5')
      .update(content, 'utf8')
      .digest('hex')
      .slice(0, hashLength);
    const generatedHash = generatorContentHash(content, hashLength);
    expect(generatedHash).toBe(expectedHash);
  });

  it('should return full hash if hash length parameter is not provided', () => {
    const content = 'example content';
    const expectedHash = createHash('md5')
      .update(content, 'utf8')
      .digest('hex');
    const actualHash = generatorContentHash(content);
    expect(actualHash).toBe(expectedHash);
  });

  it('should handle empty content', () => {
    const content = '';
    const expectedHash = createHash('md5')
      .update(content, 'utf8')
      .digest('hex');
    const actualHash = generatorContentHash(content);
    expect(actualHash).toBe(expectedHash);
  });
});
