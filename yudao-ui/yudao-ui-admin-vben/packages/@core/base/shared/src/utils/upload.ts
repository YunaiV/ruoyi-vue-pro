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
