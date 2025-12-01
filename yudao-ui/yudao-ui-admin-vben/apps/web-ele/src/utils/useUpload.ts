import type { UploadRawFile } from 'element-plus';

import { ElMessage } from 'element-plus';

const message = ElMessage; // 消息

enum UploadType {
  Image = 'image',
  Video = 'video',
  Voice = 'voice',
}

const useBeforeUpload = (type: UploadType, maxSizeMB: number) => {
  const fn = (rawFile: UploadRawFile): boolean => {
    let allowTypes: string[] = [];
    let name = '';

    switch (type) {
      case UploadType.Image: {
        allowTypes = [
          'image/jpeg',
          'image/png',
          'image/gif',
          'image/bmp',
          'image/jpg',
        ];
        maxSizeMB = 2;
        name = '图片';
        break;
      }
      case UploadType.Video: {
        allowTypes = ['video/mp4'];
        maxSizeMB = 10;
        name = '视频';
        break;
      }
      case UploadType.Voice: {
        allowTypes = [
          'audio/mp3',
          'audio/mpeg',
          'audio/wma',
          'audio/wav',
          'audio/amr',
        ];
        maxSizeMB = 2;
        name = '语音';
        break;
      }
    }
    // 格式不正确
    if (!allowTypes.includes(rawFile.type)) {
      message.error(`上传${name}格式不对!`);
      return false;
    }
    // 大小不正确
    if (rawFile.size / 1024 / 1024 > maxSizeMB) {
      message.error(`上传${name}大小不能超过${maxSizeMB}M!`);
      return false;
    }

    return true;
  };

  return fn;
};

export { UploadType, useBeforeUpload };
