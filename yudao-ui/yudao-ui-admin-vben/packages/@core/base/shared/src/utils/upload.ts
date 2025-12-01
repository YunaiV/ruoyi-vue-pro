/**
 * 根据支持的文件类型生成 accept 属性值
 *
 * @param supportedFileTypes 支持的文件类型数组，如 ['PDF', 'DOC', 'DOCX']
 * @returns 用于文件上传组件 accept 属性的字符串
 */
export function generateAcceptedFileTypes(
  supportedFileTypes: string[],
): string {
  const allowedExtensions = supportedFileTypes.map((ext) => ext.toLowerCase());
  const mimeTypes: string[] = [];

  // 添加常见的 MIME 类型映射
  if (allowedExtensions.includes('txt')) {
    mimeTypes.push('text/plain');
  }
  if (allowedExtensions.includes('pdf')) {
    mimeTypes.push('application/pdf');
  }
  if (allowedExtensions.includes('html') || allowedExtensions.includes('htm')) {
    mimeTypes.push('text/html');
  }
  if (allowedExtensions.includes('csv')) {
    mimeTypes.push('text/csv');
  }
  if (allowedExtensions.includes('xlsx') || allowedExtensions.includes('xls')) {
    mimeTypes.push(
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    );
  }
  if (allowedExtensions.includes('docx') || allowedExtensions.includes('doc')) {
    mimeTypes.push(
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    );
  }
  if (allowedExtensions.includes('pptx') || allowedExtensions.includes('ppt')) {
    mimeTypes.push(
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    );
  }
  if (allowedExtensions.includes('xml')) {
    mimeTypes.push('application/xml', 'text/xml');
  }
  if (
    allowedExtensions.includes('md') ||
    allowedExtensions.includes('markdown')
  ) {
    mimeTypes.push('text/markdown');
  }
  if (allowedExtensions.includes('epub')) {
    mimeTypes.push('application/epub+zip');
  }
  if (allowedExtensions.includes('eml')) {
    mimeTypes.push('message/rfc822');
  }
  if (allowedExtensions.includes('msg')) {
    mimeTypes.push('application/vnd.ms-outlook');
  }

  // 添加文件扩展名
  const extensions = allowedExtensions.map((ext) => `.${ext}`);

  return [...mimeTypes, ...extensions].join(',');
}

/**
 * 从 URL 中提取文件名
 *
 * @param url 文件 URL
 * @returns 文件名，如果无法提取则返回 'unknown'
 */
export function getFileNameFromUrl(url: null | string | undefined): string {
  // 处理空值
  if (!url) {
    return 'unknown';
  }

  try {
    const urlObj = new URL(url);
    const pathname = urlObj.pathname;
    const fileName = pathname.split('/').pop() || 'unknown';
    return decodeURIComponent(fileName);
  } catch {
    // 如果 URL 解析失败，尝试从字符串中提取
    const parts = url.split('/');
    return parts[parts.length - 1] || 'unknown';
  }
}

/**
 * 默认图片类型
 */
export const defaultImageAccepts = [
  'bmp',
  'gif',
  'jpeg',
  'jpg',
  'png',
  'svg',
  'webp',
];

/**
 * 判断文件是否为图片
 *
 * @param filename 文件名
 * @param accepts 支持的文件类型
 * @returns 是否为图片
 */
export function isImage(
  filename: null | string | undefined,
  accepts: string[] = defaultImageAccepts,
): boolean {
  if (!filename || accepts.length === 0) {
    return false;
  }
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  return accepts.includes(ext);
}

/**
 * 判断文件是否为指定类型
 *
 * @param file 文件
 * @param accepts 支持的文件类型
 * @returns 是否为指定类型
 */
export function checkFileType(file: File, accepts: string[]) {
  if (!accepts || accepts.length === 0) {
    return true;
  }
  const newTypes = accepts.join('|');
  const reg = new RegExp(`${String.raw`\.(` + newTypes})$`, 'i');
  return reg.test(file.name);
}

/**
 * 格式化文件大小
 *
 * @param bytes 文件大小（字节）
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(bytes: number, digits = 2): string {
  if (bytes === 0) {
    return '0 B';
  }
  const k = 1024;
  const unitArr = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const index = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / k ** index).toFixed(digits))} ${unitArr[index]}`;
}

/**
 * 获取文件图标（Lucide Icons）
 *
 * @param filename 文件名
 * @returns Lucide 图标名称
 */
export function getFileIcon(filename: null | string | undefined): string {
  if (!filename) {
    return 'lucide:file';
  }
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  if (isImage(ext)) {
    return 'lucide:image';
  }
  if (['pdf'].includes(ext)) {
    return 'lucide:file-text';
  }
  if (['doc', 'docx'].includes(ext)) {
    return 'lucide:file-text';
  }
  if (['xls', 'xlsx'].includes(ext)) {
    return 'lucide:file-spreadsheet';
  }
  if (['ppt', 'pptx'].includes(ext)) {
    return 'lucide:presentation';
  }
  if (['aac', 'm4a', 'mp3', 'wav'].includes(ext)) {
    return 'lucide:music';
  }
  if (['avi', 'mov', 'mp4', 'wmv'].includes(ext)) {
    return 'lucide:video';
  }
  return 'lucide:file';
}

/**
 * 获取文件类型样式类（Tailwind CSS 渐变色）
 *
 * @param filename 文件名
 * @returns Tailwind CSS 渐变类名
 */
export function getFileTypeClass(filename: null | string | undefined): string {
  if (!filename) {
    return 'from-gray-500 to-gray-700';
  }
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  if (isImage(ext)) {
    return 'from-yellow-400 to-orange-500';
  }
  if (['pdf'].includes(ext)) {
    return 'from-red-500 to-red-700';
  }
  if (['doc', 'docx'].includes(ext)) {
    return 'from-blue-600 to-blue-800';
  }
  if (['xls', 'xlsx'].includes(ext)) {
    return 'from-green-600 to-green-800';
  }
  if (['ppt', 'pptx'].includes(ext)) {
    return 'from-orange-600 to-orange-800';
  }
  if (['aac', 'm4a', 'mp3', 'wav'].includes(ext)) {
    return 'from-purple-500 to-purple-700';
  }
  if (['avi', 'mov', 'mp4', 'wmv'].includes(ext)) {
    return 'from-red-500 to-red-700';
  }
  return 'from-gray-500 to-gray-700';
}
