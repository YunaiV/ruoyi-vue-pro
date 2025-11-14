import { promises as fs } from 'node:fs';
import { join, normalize } from 'node:path';

const rootDir = process.cwd();

// 控制并发数量，避免创建过多的并发任务
const CONCURRENCY_LIMIT = 10;

// 需要跳过的目录，避免进入这些目录进行清理
const SKIP_DIRS = new Set(['.DS_Store', '.git', '.idea', '.vscode']);

/**
 * 处理单个文件/目录项
 * @param {string} currentDir - 当前目录路径
 * @param {string} item - 文件/目录名
 * @param {string[]} targets - 要删除的目标列表
 * @param {number} _depth - 当前递归深度
 * @returns {Promise<boolean>} - 是否需要进一步递归处理
 */
async function processItem(currentDir, item, targets, _depth) {
  // 跳过特殊目录
  if (SKIP_DIRS.has(item)) {
    return false;
  }

  try {
    const itemPath = normalize(join(currentDir, item));

    if (targets.includes(item)) {
      // 匹配到目标目录或文件时直接删除
      await fs.rm(itemPath, { force: true, recursive: true });
      console.log(`✅ Deleted: ${itemPath}`);
      return false; // 已删除，无需递归
    }

    // 使用 readdir 的 withFileTypes 选项，避免额外的 lstat 调用
    return true; // 可能需要递归，由调用方决定
  } catch (error) {
    // 更详细的错误信息
    if (error.code === 'ENOENT') {
      // 文件不存在，可能已被删除，这是正常情况
      return false;
    } else if (error.code === 'EPERM' || error.code === 'EACCES') {
      console.error(`❌ Permission denied: ${item} in ${currentDir}`);
    } else {
      console.error(
        `❌ Error handling item ${item} in ${currentDir}: ${error.message}`,
      );
    }
    return false;
  }
}

/**
 * 递归查找并删除目标目录（并发优化版本）
 * @param {string} currentDir - 当前遍历的目录路径
 * @param {string[]} targets - 要删除的目标列表
 * @param {number} depth - 当前递归深度，避免过深递归
 */
async function cleanTargetsRecursively(currentDir, targets, depth = 0) {
  // 限制递归深度，避免无限递归
  if (depth > 10) {
    console.warn(`Max recursion depth reached at: ${currentDir}`);
    return;
  }

  let dirents;
  try {
    // 使用 withFileTypes 选项，一次性获取文件类型信息，避免后续 lstat 调用
    dirents = await fs.readdir(currentDir, { withFileTypes: true });
  } catch (error) {
    // 如果无法读取目录，可能已被删除或权限不足
    console.warn(`Cannot read directory ${currentDir}: ${error.message}`);
    return;
  }

  // 分批处理，控制并发数量
  for (let i = 0; i < dirents.length; i += CONCURRENCY_LIMIT) {
    const batch = dirents.slice(i, i + CONCURRENCY_LIMIT);

    const tasks = batch.map(async (dirent) => {
      const item = dirent.name;
      const shouldRecurse = await processItem(currentDir, item, targets, depth);

      // 如果是目录且没有被删除，则递归处理
      if (shouldRecurse && dirent.isDirectory()) {
        const itemPath = normalize(join(currentDir, item));
        return cleanTargetsRecursively(itemPath, targets, depth + 1);
      }

      return null;
    });

    // 并发执行当前批次的任务
    const results = await Promise.allSettled(tasks);

    // 检查是否有失败的任务（可选：用于调试）
    const failedTasks = results.filter(
      (result) => result.status === 'rejected',
    );
    if (failedTasks.length > 0) {
      console.warn(
        `${failedTasks.length} tasks failed in batch starting at index ${i} in directory: ${currentDir}`,
      );
    }
  }
}

(async function startCleanup() {
  // 要删除的目录及文件名称
  const targets = ['node_modules', 'dist', '.turbo', 'dist.zip'];
  const deleteLockFile = process.argv.includes('--del-lock');
  const cleanupTargets = [...targets];

  if (deleteLockFile) {
    cleanupTargets.push('pnpm-lock.yaml');
  }

  console.log(
    `🚀 Starting cleanup of targets: ${cleanupTargets.join(', ')} from root: ${rootDir}`,
  );

  const startTime = Date.now();

  try {
    // 先统计要删除的目标数量
    console.log('📊 Scanning for cleanup targets...');

    await cleanTargetsRecursively(rootDir, cleanupTargets);

    const endTime = Date.now();
    const duration = (endTime - startTime) / 1000;

    console.log(
      `✨ Cleanup process completed successfully in ${duration.toFixed(2)}s`,
    );
  } catch (error) {
    console.error(`💥 Unexpected error during cleanup: ${error.message}`);
    process.exit(1);
  }
})();
