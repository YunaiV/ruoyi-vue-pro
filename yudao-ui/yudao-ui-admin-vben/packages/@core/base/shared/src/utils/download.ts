import { openWindow } from './window';

interface DownloadOptions<T = string> {
  fileName?: string;
  source: T;
  target?: string;
}

const DEFAULT_FILENAME = 'downloaded_file';

/**
 * 通过 URL 下载文件，支持跨域
 * @throws {Error} - 当下载失败时抛出错误
 */
export async function downloadFileFromUrl({
  fileName,
  source,
  target = '_blank',
}: DownloadOptions): Promise<void> {
  if (!source || typeof source !== 'string') {
    throw new Error('Invalid URL.');
  }

  const isChrome = window.navigator.userAgent.toLowerCase().includes('chrome');
  const isSafari = window.navigator.userAgent.toLowerCase().includes('safari');

  if (/iP/.test(window.navigator.userAgent)) {
    console.error('Your browser does not support download!');
    return;
  }

  if (isChrome || isSafari) {
    triggerDownload(source, resolveFileName(source, fileName));
    return;
  }
  if (!source.includes('?')) {
    source += '?download';
  }

  openWindow(source, { target });
}

/**
 * 下载图片（允许跨域）
 * @param url - 图片 URL
 * @param canvasWidth - 画布宽度
 * @param canvasHeight - 画布高度
 * @param drawWithImageSize - 将图片绘制在画布上时带上图片的宽高值, 默认是要带上的
 * @returns
 */
export function downloadImageByCanvas({
  url,
  canvasWidth,
  canvasHeight,
  drawWithImageSize = true,
}: {
  canvasHeight?: number;
  canvasWidth?: number;
  drawWithImageSize?: boolean;
  url: string;
}) {
  const image = new Image();
  // image.setAttribute('crossOrigin', 'anonymous')
  image.src = url;
  image.addEventListener('load', () => {
    const canvas = document.createElement('canvas');
    canvas.width = canvasWidth || image.width;
    canvas.height = canvasHeight || image.height;
    const ctx = canvas.getContext('2d') as CanvasRenderingContext2D;
    ctx?.clearRect(0, 0, canvas.width, canvas.height);
    if (drawWithImageSize) {
      ctx.drawImage(image, 0, 0, image.width, image.height);
    } else {
      ctx.drawImage(image, 0, 0);
    }
    const url = canvas.toDataURL('image/png');
    downloadFileFromImageUrl({ source: url, fileName: 'image.png' });
  });
}

/**
 * 通过 Base64 下载文件
 */
export function downloadFileFromBase64({ fileName, source }: DownloadOptions) {
  if (!source || typeof source !== 'string') {
    throw new Error('Invalid Base64 data.');
  }

  const resolvedFileName = fileName || DEFAULT_FILENAME;
  triggerDownload(source, resolvedFileName);
}

/**
 * 通过图片 URL 下载图片文件
 */
export async function downloadFileFromImageUrl({
  fileName,
  source,
}: DownloadOptions) {
  const base64 = await urlToBase64(source);
  downloadFileFromBase64({ fileName, source: base64 });
}

/**
 * 通过 Blob 下载文件
 */
export function downloadFileFromBlob({
  fileName = DEFAULT_FILENAME,
  source,
}: DownloadOptions<Blob>): void {
  if (!(source instanceof Blob)) {
    throw new TypeError('Invalid Blob data.');
  }

  const url = URL.createObjectURL(source);
  triggerDownload(url, fileName);
}

/**
 * 下载文件，支持 Blob、字符串和其他 BlobPart 类型
 */
export function downloadFileFromBlobPart({
  fileName = DEFAULT_FILENAME,
  source,
}: DownloadOptions<BlobPart>): void {
  // 如果 data 不是 Blob，则转换为 Blob
  const blob =
    source instanceof Blob
      ? source
      : new Blob([source], { type: 'application/octet-stream' });

  // 创建对象 URL 并触发下载
  const url = URL.createObjectURL(blob);
  triggerDownload(url, fileName);
}

/**
 * @description: base64 to blob
 */
export function dataURLtoBlob(base64Buf: string): Blob {
  const arr = base64Buf.split(',');
  const typeItem = arr[0];
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  const mime = typeItem!.match(/:(.*?);/)![1];
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  const bstr = window.atob(arr[1]!);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    u8arr[n] = bstr.codePointAt(n)!;
  }
  return new Blob([u8arr], { type: mime });
}

/**
 * img url to base64
 * @param url
 */
export function urlToBase64(url: string, mineType?: string): Promise<string> {
  return new Promise((resolve, reject) => {
    let canvas = document.createElement('CANVAS') as HTMLCanvasElement | null;
    const ctx = canvas?.getContext('2d');
    const img = new Image();
    img.crossOrigin = '';
    img.addEventListener('load', () => {
      if (!canvas || !ctx) {
        return reject(new Error('Failed to create canvas.'));
      }
      canvas.height = img.height;
      canvas.width = img.width;
      ctx.drawImage(img, 0, 0);
      const dataURL = canvas.toDataURL(mineType || 'image/png');
      canvas = null;
      resolve(dataURL);
    });
    img.src = url;
  });
}

/**
 * 将 Base64 字符串转换为文件对象
 * @param base64 - Base64 字符串
 * @param fileName - 文件名
 * @returns File 对象
 */
export function base64ToFile(base64: string, fileName: string): File {
  // 输入验证
  if (!base64 || typeof base64 !== 'string') {
    throw new Error('base64 参数必须是非空字符串');
  }

  // 将 base64 按照逗号进行分割，将前缀与后续内容分隔开
  const data = base64.split(',');
  if (data.length !== 2 || !data[0] || !data[1]) {
    throw new Error('无效的 base64 格式');
  }

  // 利用正则表达式从前缀中获取类型信息（image/png、image/jpeg、image/webp等）
  const typeMatch = data[0].match(/:(.*?);/);
  if (!typeMatch || !typeMatch[1]) {
    throw new Error('无法解析 base64 类型信息');
  }
  const type = typeMatch[1];

  // 从类型信息中获取具体的文件格式后缀（png、jpeg、webp）
  const typeParts = type.split('/');
  if (typeParts.length !== 2 || !typeParts[1]) {
    throw new Error('无效的 MIME 类型格式');
  }
  const suffix = typeParts[1];

  try {
    // 使用 atob() 对 base64 数据进行解码，结果是一个文件数据流以字符串的格式输出
    const bstr = window.atob(data[1]);

    // 获取解码结果字符串的长度
    const n = bstr.length;
    // 根据解码结果字符串的长度创建一个等长的整型数字数组
    const u8arr = new Uint8Array(n);

    // 优化的 Uint8Array 填充逻辑
    for (let i = 0; i < n; i++) {
      // 使用 charCodeAt() 获取字符对应的字节值（Base64 解码后的字符串是字节级别的）
      // eslint-disable-next-line unicorn/prefer-code-point
      u8arr[i] = bstr.charCodeAt(i);
    }

    // 返回 File 文件对象
    return new File([u8arr], `${fileName}.${suffix}`, { type });
  } catch (error) {
    throw new Error(
      `Base64 解码失败: ${error instanceof Error ? error.message : '未知错误'}`,
    );
  }
}

/**
 * 通用下载触发函数
 * @param href - 文件下载的 URL
 * @param fileName - 下载文件的名称，如果未提供则自动识别
 * @param revokeDelay - 清理 URL 的延迟时间 (毫秒)
 */
export function triggerDownload(
  href: string,
  fileName: string | undefined,
  revokeDelay: number = 100,
): void {
  const defaultFileName = 'downloaded_file';
  const finalFileName = fileName || defaultFileName;

  const link = document.createElement('a');
  link.href = href;
  link.download = finalFileName;
  link.style.display = 'none';

  if (link.download === undefined) {
    link.setAttribute('target', '_blank');
  }

  document.body.append(link);
  link.click();
  link.remove();

  // 清理临时 URL 以释放内存
  setTimeout(() => URL.revokeObjectURL(href), revokeDelay);
}

function resolveFileName(url: string, fileName?: string): string {
  return fileName || url.slice(url.lastIndexOf('/') + 1) || DEFAULT_FILENAME;
}
