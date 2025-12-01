import type { Ref } from 'vue';

import type { AxiosProgressEvent, InfraFileApi } from '#/api/infra/file';

import { computed, unref } from 'vue';

import { useAppConfig } from '@vben/hooks';
import { $t } from '@vben/locales';

import { createFile, getFilePresignedUrl, uploadFile } from '#/api/infra/file';
import { baseRequestClient } from '#/api/request';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

/**
 * 上传类型
 */
enum UPLOAD_TYPE {
  // 客户端直接上传（只支持S3服务）
  CLIENT = 'client',
  // 客户端发送到后端上传
  SERVER = 'server',
}

export function useUploadType({
  acceptRef,
  helpTextRef,
  maxNumberRef,
  maxSizeRef,
}: {
  acceptRef: Ref<string[]>;
  helpTextRef: Ref<string>;
  maxNumberRef: Ref<number>;
  maxSizeRef: Ref<number>;
}) {
  // 文件类型限制
  const getAccept = computed(() => {
    const accept = unref(acceptRef);
    if (accept && accept.length > 0) {
      return accept;
    }
    return [];
  });
  const getStringAccept = computed(() => {
    return unref(getAccept)
      .map((item) => {
        return item.indexOf('/') > 0 || item.startsWith('.')
          ? item
          : `.${item}`;
      })
      .join(',');
  });

  // 支持jpg、jpeg、png格式，不超过2M，最多可选择10张图片，。
  const getHelpText = computed(() => {
    const helpText = unref(helpTextRef);
    if (helpText) {
      return helpText;
    }
    const helpTexts: string[] = [];

    const accept = unref(acceptRef);
    if (accept.length > 0) {
      helpTexts.push($t('ui.upload.accept', [accept.join(',')]));
    }

    const maxSize = unref(maxSizeRef);
    if (maxSize) {
      helpTexts.push($t('ui.upload.maxSize', [maxSize]));
    }

    const maxNumber = unref(maxNumberRef);
    if (maxNumber && maxNumber !== Infinity) {
      helpTexts.push($t('ui.upload.maxNumber', [maxNumber]));
    }
    return helpTexts.join('，');
  });
  return { getAccept, getStringAccept, getHelpText };
}

// TODO @芋艿：目前保持和 admin-vue3 一致，后续可能重构
export function useUpload(directory?: string) {
  // 后端上传地址
  const uploadUrl = getUploadUrl();
  // 是否使用前端直连上传
  const isClientUpload =
    UPLOAD_TYPE.CLIENT === import.meta.env.VITE_UPLOAD_TYPE;
  // 重写ElUpload上传方法
  async function httpRequest(
    file: File,
    onUploadProgress?: AxiosProgressEvent,
  ) {
    // 模式一：前端上传
    if (isClientUpload) {
      // 1.1 生成文件名称
      const fileName = await generateFileName(file);
      // 1.2 获取文件预签名地址
      const presignedInfo = await getFilePresignedUrl(fileName, directory);
      // 1.3 上传文件
      return baseRequestClient
        .put(presignedInfo.uploadUrl, file, {
          headers: {
            'Content-Type': file.type,
          },
        })
        .then(() => {
          // 1.4. 记录文件信息到后端（异步）
          createFile0(presignedInfo, file);
          // 通知成功，数据格式保持与后端上传的返回结果一致
          return { url: presignedInfo.url };
        });
    } else {
      // 模式二：后端上传
      return uploadFile({ file, directory }, onUploadProgress);
    }
  }

  return {
    uploadUrl,
    httpRequest,
  };
}

/**
 * 获得上传 URL
 */
export function getUploadUrl(): string {
  return `${apiURL}/infra/file/upload`;
}

/**
 * 创建文件信息
 *
 * @param vo 文件预签名信息
 * @param file 文件
 */
function createFile0(
  vo: InfraFileApi.FilePresignedUrlRespVO,
  file: File,
): InfraFileApi.File {
  const fileVO = {
    configId: vo.configId,
    url: vo.url,
    path: vo.path,
    name: file.name,
    type: file.type,
    size: file.size,
  };
  createFile(fileVO);
  return fileVO;
}

/**
 * 生成文件名称（使用算法SHA256）
 *
 * @param file 要上传的文件
 */
async function generateFileName(file: File) {
  // // 读取文件内容
  // const data = await file.arrayBuffer();
  // const wordArray = CryptoJS.lib.WordArray.create(data);
  // // 计算SHA256
  // const sha256 = CryptoJS.SHA256(wordArray).toString();
  // // 拼接后缀
  // const ext = file.name.slice(Math.max(0, file.name.lastIndexOf('.')));
  // return `${sha256}${ext}`;
  return file.name;
}
