import type { PluginOption } from 'vite';

import type { ArchiverPluginOptions } from '../typing';

import fs from 'node:fs';
import fsp from 'node:fs/promises';
import { join } from 'node:path';

import archiver from 'archiver';

export const viteArchiverPlugin = (
  options: ArchiverPluginOptions = {},
): PluginOption => {
  return {
    apply: 'build',
    closeBundle: {
      handler() {
        const { name = 'dist', outputDir = '.' } = options;

        setTimeout(async () => {
          const folderToZip = 'dist';

          const zipOutputDir = join(process.cwd(), outputDir);
          const zipOutputPath = join(zipOutputDir, `${name}.zip`);
          try {
            await fsp.mkdir(zipOutputDir, { recursive: true });
          } catch {
            // ignore
          }

          try {
            await zipFolder(folderToZip, zipOutputPath);
            console.log(`Folder has been zipped to: ${zipOutputPath}`);
          } catch (error) {
            console.error('Error zipping folder:', error);
          }
        }, 0);
      },
      order: 'post',
    },
    enforce: 'post',
    name: 'vite:archiver',
  };
};

async function zipFolder(
  folderPath: string,
  outputPath: string,
): Promise<void> {
  return new Promise((resolve, reject) => {
    const output = fs.createWriteStream(outputPath);
    const archive = archiver('zip', {
      zlib: { level: 9 }, // 设置压缩级别为 9 以实现最高压缩率
    });

    output.on('close', () => {
      console.log(
        `ZIP file created: ${outputPath} (${archive.pointer()} total bytes)`,
      );
      resolve();
    });

    archive.on('error', (err) => {
      reject(err);
    });

    archive.pipe(output);

    // 使用 directory 方法以流的方式压缩文件夹，减少内存消耗
    archive.directory(folderPath, false);

    // 流式处理完成
    archive.finalize();
  });
}
