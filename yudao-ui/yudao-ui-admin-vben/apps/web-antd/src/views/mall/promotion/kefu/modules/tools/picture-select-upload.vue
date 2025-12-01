<!-- 图片选择 -->
<script lang="ts" setup>
import { Image, message } from 'ant-design-vue';

import { useUpload } from '#/components/upload/use-upload';
import { $t } from '#/locales';
import Picture from '#/views/mall/promotion/kefu/asserts/picture.svg';

/** 选择并上传文件 */
const emits = defineEmits<{
  (e: 'sendPicture', v: string): void;
}>();

async function selectAndUpload() {
  const files: any = await getFiles();
  message.success($t('ui.upload.imgUploading'));
  const res = await useUpload().httpRequest(files[0].file);
  message.success($t('ui.upload.uploadSuccess'));
  emits('sendPicture', res);
}

/** 唤起文件选择窗口，并获取选择的文件 */
async function getFiles(options = {}) {
  const { multiple, accept, limit, fileSize } = {
    multiple: true,
    accept: 'image/jpeg, image/png, image/gif', // 默认选择图片
    limit: 1,
    fileSize: 500,
    ...options,
  };

  // 创建文件选择元素
  const input = document.createElement('input');
  input.type = 'file';
  input.style.display = 'none';
  if (multiple) input.multiple = true;
  if (accept) input.accept = accept;

  // 将文件选择元素添加到文档中
  document.body.append(input);

  // 触发文件选择元素的点击事件
  input.click();

  // 等待文件选择元素的 change 事件
  // 移除不必要的 try/catch 包装，直接返回 Promise
  return await new Promise((resolve, reject) => {
    input.addEventListener('change', (event: any) => {
      const filesArray = [...(event?.target?.files || [])];

      // 从文档中移除文件选择元素
      input.remove();

      // 判断是否超出上传数量限制
      if (filesArray.length > limit) {
        // 使用 Error 对象作为 reject 的原因
        reject(new Error(`超出上传数量限制，最多允许 ${limit} 个文件`));
        return;
      }
      // 判断是否超出上传文件大小限制
      const overSizedFiles = filesArray.filter(
        (file: File) => file.size / 1024 ** 2 > fileSize,
      );
      if (overSizedFiles.length > 0) {
        // 使用 Error 对象作为 reject 的原因
        reject(new Error(`文件大小超出限制，单个文件最大允许 ${fileSize}MB`));
        return;
      }

      // 生成文件列表，并添加 uid
      const fileList = filesArray.map((file, index) => ({
        file,
        uid: Date.now() + index,
      }));
      resolve(fileList);
    });
  });
}
</script>

<template>
  <div>
    <Image
      :preview="false"
      :src="Picture"
      width="35px"
      height="35px"
      @click="selectAndUpload"
    />
  </div>
</template>
