import { baseUrl, apiPath, tenantId } from '@/sheep/config';
import request, { getAccessToken } from '@/sheep/request';

const FileApi = {
  // 上传文件
  uploadFile: (file, directory = '') => {
    uni.showLoading({
      title: '上传中',
    });
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: baseUrl + apiPath + '/infra/file/upload',
        filePath: file,
        name: 'file',
        header: {
          Accept: '*/*',
          'tenant-id': tenantId,
          Authorization: 'Bearer ' + getAccessToken(),
        },
        formData: {
          directory,
        },
        success: (uploadFileRes) => {
          let result = JSON.parse(uploadFileRes.data);
          if (result.error === 1) {
            uni.showToast({
              icon: 'none',
              title: result.msg,
            });
          } else {
            return resolve(result);
          }
        },
        fail: (error) => {
          console.log('上传失败：', error);
          return resolve(false);
        },
        complete: () => {
          uni.hideLoading();
        },
      });
    });
  },

  // 获取文件预签名地址
  getFilePresignedUrl: (name, directory) => {
    return request({
      url: '/infra/file/presigned-url',
      method: 'GET',
      params: {
        name,
        directory,
      },
    });
  },

  // 创建文件
  createFile: (data) => {
    return request({
      url: '/infra/file/create', // 请求的 URL
      method: 'POST', // 请求方法
      data: data, // 要发送的数据
    });
  },
};

export default FileApi;
