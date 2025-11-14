'use strict';
import FileApi from '@/sheep/api/infra/file';

const ERR_MSG_OK = 'chooseAndUploadFile:ok';
const ERR_MSG_FAIL = 'chooseAndUploadFile:fail';

function chooseImage(opts) {
  const {
    count,
    sizeType = ['original', 'compressed'],
    sourceType = ['album', 'camera'],
    extension,
  } = opts;
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType,
      sourceType,
      extension,
      success(res) {
        resolve(normalizeChooseAndUploadFileRes(res, 'image'));
      },
      fail(res) {
        reject({
          errMsg: res.errMsg.replace('chooseImage:fail', ERR_MSG_FAIL),
        });
      },
    });
  });
}

function chooseVideo(opts) {
  const { camera, compressed, maxDuration, sourceType = ['album', 'camera'], extension } = opts;
  return new Promise((resolve, reject) => {
    uni.chooseVideo({
      camera,
      compressed,
      maxDuration,
      sourceType,
      extension,
      success(res) {
        const { tempFilePath, duration, size, height, width } = res;
        resolve(
          normalizeChooseAndUploadFileRes(
            {
              errMsg: 'chooseVideo:ok',
              tempFilePaths: [tempFilePath],
              tempFiles: [
                {
                  name: (res.tempFile && res.tempFile.name) || '',
                  path: tempFilePath,
                  size,
                  type: (res.tempFile && res.tempFile.type) || '',
                  width,
                  height,
                  duration,
                  fileType: 'video',
                  cloudPath: '',
                },
              ],
            },
            'video',
          ),
        );
      },
      fail(res) {
        reject({
          errMsg: res.errMsg.replace('chooseVideo:fail', ERR_MSG_FAIL),
        });
      },
    });
  });
}

function chooseAll(opts) {
  const { count, extension } = opts;
  return new Promise((resolve, reject) => {
    let chooseFile = uni.chooseFile;
    if (typeof wx !== 'undefined' && typeof wx.chooseMessageFile === 'function') {
      chooseFile = wx.chooseMessageFile;
    }
    if (typeof chooseFile !== 'function') {
      return reject({
        errMsg: ERR_MSG_FAIL + ' 请指定 type 类型，该平台仅支持选择 image 或 video。',
      });
    }
    chooseFile({
      type: 'all',
      count,
      extension,
      success(res) {
        resolve(normalizeChooseAndUploadFileRes(res));
      },
      fail(res) {
        reject({
          errMsg: res.errMsg.replace('chooseFile:fail', ERR_MSG_FAIL),
        });
      },
    });
  });
}

function normalizeChooseAndUploadFileRes(res, fileType) {
  res.tempFiles.forEach((item, index) => {
    if (!item.name) {
      item.name = item.path.substring(item.path.lastIndexOf('/') + 1);
    }
    if (fileType) {
      item.fileType = fileType;
    }
    item.cloudPath = Date.now() + '_' + index + item.name.substring(item.name.lastIndexOf('.'));
  });
  if (!res.tempFilePaths) {
    res.tempFilePaths = res.tempFiles.map((file) => file.path);
  }
  return res;
}

async function readFile(uniFile) {
  // 微信小程序
  if (uni.getFileSystemManager) {
    const fs = uni.getFileSystemManager();
    return fs.readFileSync(uniFile.path);
  }
  // H5 等
  return uniFile.arrayBuffer();
}

function uploadCloudFiles(files, max = 5, onUploadProgress) {
  files = JSON.parse(JSON.stringify(files));
  const len = files.length;
  let count = 0;
  let self = this;
  return new Promise((resolve) => {
    while (count < max) {
      next();
    }

    function next() {
      let cur = count++;
      if (cur >= len) {
        !files.find((item) => !item.url && !item.errMsg) && resolve(files);
        return;
      }
      const fileItem = files[cur];
      const index = self.files.findIndex((v) => v.uuid === fileItem.uuid);
      fileItem.url = '';
      delete fileItem.errMsg;

      uniCloud
        .uploadFile({
          filePath: fileItem.path,
          cloudPath: fileItem.cloudPath,
          fileType: fileItem.fileType,
          onUploadProgress: (res) => {
            res.index = index;
            onUploadProgress && onUploadProgress(res);
          },
        })
        .then((res) => {
          fileItem.url = res.fileID;
          fileItem.index = index;
          if (cur < len) {
            next();
          }
        })
        .catch((res) => {
          fileItem.errMsg = res.errMsg || res.message;
          fileItem.index = index;
          if (cur < len) {
            next();
          }
        });
    }
  });
}

function uploadFilesFromPath(path, directory = '') {
  // 目的：用于微信小程序，选择图片时，只有 path
  return uploadFiles(
    Promise.resolve({
      tempFiles: [
        {
          path,
          type: 'image/jpeg',
          name: path.includes('/') ? path.substring(path.lastIndexOf('/') + 1) : path,
        },
      ],
    }),
    {
      directory,
    },
  );
}

async function uploadFiles(choosePromise, { onChooseFile, onUploadProgress, directory }) {
  // 获取选择的文件
  const res = await choosePromise;
  // 处理文件选择回调
  let files = res.tempFiles || [];
  if (onChooseFile) {
    const customChooseRes = onChooseFile(res);
    if (typeof customChooseRes !== 'undefined') {
      files = await Promise.resolve(customChooseRes);
      if (typeof files === 'undefined') {
        files = res.tempFiles || []; // Fallback
      }
    }
  }

  // 如果是前端直连上传
  if (UPLOAD_TYPE.CLIENT === import.meta.env.SHOPRO_UPLOAD_TYPE) {
    // 为上传创建一组 Promise
    const uploadPromises = files.map(async (file) => {
      try {
        // 1.1 获取文件预签名地址
        const { data: presignedInfo } = await FileApi.getFilePresignedUrl(file.name, directory);
        // 1.2 获取二进制文件对象
        const fileBuffer = await readFile(file);

        // 返回上传的 Promise
        return new Promise((resolve, reject) => {
          // 1.3. 上传文件到 S3
          uni.request({
            url: presignedInfo.uploadUrl,
            method: 'PUT',
            header: {
              'Content-Type': file.type,
            },
            data: fileBuffer,
            success: (res) => {
              // 1.4. 记录文件信息到后端（异步）
              createFile(presignedInfo, file);
              // 1.5. 重新赋值
              file.url = presignedInfo.url;
              resolve(file);
            },
            fail: (err) => {
              reject(err);
            },
          });
        });
      } catch (error) {
        console.error('上传失败：', error);
        throw error;
      }
    });

    // 等待所有上传完成
    return await Promise.all(uploadPromises); // 返回已上传的文件列表
  } else {
    // 后端上传
    for (let file of files) {
      const { data } = await FileApi.uploadFile(file.path, directory);
      file.url = data;
    }

    return files;
  }
}

function chooseAndUploadFile(
  opts = {
    type: 'all',
    directory: undefined,
  },
) {
  if (opts.type === 'image') {
    return uploadFiles(chooseImage(opts), opts);
  } else if (opts.type === 'video') {
    return uploadFiles(chooseVideo(opts), opts);
  }
  return uploadFiles(chooseAll(opts), opts);
}

/**
 * 创建文件信息
 * @param vo 文件预签名信息
 * @param file 文件
 */
function createFile(vo, file) {
  const fileVo = {
    configId: vo.configId,
    url: vo.url,
    path: vo.path,
    name: file.name,
    type: file.fileType,
    size: file.size,
  };
  FileApi.createFile(fileVo);
  return fileVo;
}

/**
 * 上传类型
 */
const UPLOAD_TYPE = {
  // 客户端直接上传（只支持S3服务）
  CLIENT: 'client',
  // 客户端发送到后端上传
  SERVER: 'server',
};

export { chooseAndUploadFile, uploadCloudFiles, uploadFilesFromPath };
