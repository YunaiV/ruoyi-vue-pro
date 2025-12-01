import { message } from 'ant-design-vue';
// TODO @xingyu：这种，要想办法全局共享起来么？

import { $t } from '#/locales';

export enum UploadType {
  Image = 'image',
  Video = 'video',
  Voice = 'voice',
}

interface UploadTypeConfig {
  allowTypes: string[];
  maxSizeMB: number;
  i18nKey: string;
}

export interface UploadRawFile {
  name: string;
  size: number;
  type: string;
}

const UPLOAD_CONFIGS: Record<UploadType, UploadTypeConfig> = {
  [UploadType.Image]: {
    allowTypes: [
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/bmp',
      'image/jpg',
    ],
    maxSizeMB: 2,
    i18nKey: 'mp.upload.image',
  },
  [UploadType.Video]: {
    allowTypes: ['video/mp4'],
    maxSizeMB: 10,
    i18nKey: 'mp.upload.video',
  },
  [UploadType.Voice]: {
    allowTypes: [
      'audio/mp3',
      'audio/mpeg',
      'audio/wma',
      'audio/wav',
      'audio/amr',
    ],
    maxSizeMB: 2,
    i18nKey: 'mp.upload.voice',
  },
};

export const useBeforeUpload = (type: UploadType, maxSizeMB?: number) => {
  const fn = (rawFile: UploadRawFile): boolean => {
    const config = UPLOAD_CONFIGS[type];
    const finalMaxSize = maxSizeMB ?? config.maxSizeMB;

    // 格式不正确
    if (!config.allowTypes.includes(rawFile.type)) {
      const typeName = $t(config.i18nKey);
      message.error($t('mp.upload.invalidFormat', [typeName]));
      return false;
    }

    // 大小不正确
    if (rawFile.size / 1024 / 1024 > finalMaxSize) {
      const typeName = $t(config.i18nKey);
      message.error($t('mp.upload.maxSize', [typeName, finalMaxSize]));
      return false;
    }

    return true;
  };

  return fn;
};
