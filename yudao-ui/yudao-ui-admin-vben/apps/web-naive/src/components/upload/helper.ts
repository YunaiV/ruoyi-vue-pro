/**
 * 默认图片类型
 */
export const defaultImageAccepts = ['jpg', 'jpeg', 'png', 'gif', 'webp'];

export function checkFileType(file: File, accepts: string[]) {
  if (!accepts || accepts.length === 0) {
    return true;
  }
  const newTypes = accepts.join('|');
  const reg = new RegExp(`${String.raw`\.(` + newTypes})$`, 'i');
  return reg.test(file.name);
}

export function checkImgType(
  file: File,
  accepts: string[] = defaultImageAccepts,
) {
  return checkFileType(file, accepts);
}
