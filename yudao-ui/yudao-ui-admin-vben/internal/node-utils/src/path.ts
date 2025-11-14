import { posix } from 'node:path';

/**
 * 将给定的文件路径转换为 POSIX 风格。
 * @param {string} pathname - 原始文件路径。
 */
function toPosixPath(pathname: string) {
  return pathname.split(`\\`).join(posix.sep);
}

export { toPosixPath };
