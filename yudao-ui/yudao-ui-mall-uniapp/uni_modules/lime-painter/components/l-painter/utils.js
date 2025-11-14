export const networkReg = /^(http|\/\/)/;
export const isBase64 = (path) => /^data:image\/(\w+);base64/.test(path);
export function sleep(delay) {
	return new Promise(resolve => setTimeout(resolve, delay))
}
let {platform, SDKVersion} = uni.getSystemInfoSync() 
export const isPC = /windows|mac/.test(platform)
// 缓存图片
let cache = {}
export function isNumber(value) {
	return /^-?\d+(\.\d+)?$/.test(value);
}
export function toPx(value, baseSize, isDecimal = false) {
	// 如果是数字
	if (typeof value === 'number') {
		return value
	}
	// 如果是字符串数字
	if (isNumber(value)) {
		return value * 1
	}
	// 如果有单位
	if (typeof value === 'string') {
		const reg = /^-?([0-9]+)?([.]{1}[0-9]+){0,1}(em|rpx|px|%)$/g
		const results = reg.exec(value);
		if (!value || !results) {
			return 0;
		}
		const unit = results[3];
		value = parseFloat(value);
		let res = 0;
		if (unit === 'rpx') {
			res = uni.upx2px(value);
		} else if (unit === 'px') {
			res = value * 1;
		} else if (unit === '%') {
			res = value * toPx(baseSize) / 100;
		} else if (unit === 'em') {
			res = value * toPx(baseSize || 14);
		}
		return isDecimal ? res.toFixed(2) * 1 : Math.round(res);
	}
	return 0
}

// 计算版本
export function compareVersion(v1, v2) {
	v1 = v1.split('.')
	v2 = v2.split('.')
	const len = Math.max(v1.length, v2.length)
	while (v1.length < len) {
		v1.push('0')
	}
	while (v2.length < len) {
		v2.push('0')
	}
	for (let i = 0; i < len; i++) {
		const num1 = parseInt(v1[i], 10)
		const num2 = parseInt(v2[i], 10)

		if (num1 > num2) {
			return 1
		} else if (num1 < num2) {
			return -1
		}
	}
	return 0
}

function gte(version) {
  // #ifdef MP-ALIPAY
  SDKVersion = my.SDKVersion
  // #endif
  return compareVersion(SDKVersion, version) >= 0;
}
export function canIUseCanvas2d() {
	// #ifdef MP-WEIXIN
	return gte('2.9.2');
	// #endif
	// #ifdef MP-ALIPAY
	return gte('2.7.15');
	// #endif
	// #ifdef MP-TOUTIAO
	return gte('1.78.0');
	// #endif
	return false
}

// #ifdef MP
export const prefix = () => {
	// #ifdef MP-TOUTIAO
	return tt
	// #endif
	// #ifdef MP-WEIXIN
	return wx
	// #endif
	// #ifdef MP-BAIDU
	return swan
	// #endif
	// #ifdef MP-ALIPAY
	return my
	// #endif
	// #ifdef MP-QQ
	return qq
	// #endif
	// #ifdef MP-360
	return qh
	// #endif
}
// #endif



/**
 * base64转路径
 * @param {Object} base64
 */
export function base64ToPath(base64) {
	const [, format] = /^data:image\/(\w+);base64,/.exec(base64) || [];

	return new Promise((resolve, reject) => {
		// #ifdef MP
		const fs = uni.getFileSystemManager()
		//自定义文件名
		if (!format) {
			reject(new Error('ERROR_BASE64SRC_PARSE'))
		}
		const time = new Date().getTime();
		let pre = prefix()
		// #ifdef MP-TOUTIAO
		const filePath = `${pre.getEnvInfoSync().common.USER_DATA_PATH}/${time}.${format}`
		// #endif
		// #ifndef MP-TOUTIAO
		const filePath = `${pre.env.USER_DATA_PATH}/${time}.${format}`
		// #endif
		fs.writeFile({
			filePath,
			data: base64.split(',')[1],
			encoding: 'base64',
			success() {
				resolve(filePath)
			},
			fail(err) {
				console.error(err)
				reject(err)
			}
		})
		// #endif

		// #ifdef H5
		// mime类型
		let mimeString = base64.split(',')[0].split(':')[1].split(';')[0];
		//base64 解码
		let byteString = atob(base64.split(',')[1]);
		//创建缓冲数组
		let arrayBuffer = new ArrayBuffer(byteString.length);
		//创建视图
		let intArray = new Uint8Array(arrayBuffer);
		for (let i = 0; i < byteString.length; i++) {
			intArray[i] = byteString.charCodeAt(i);
		}
		resolve(URL.createObjectURL(new Blob([intArray], {
			type: mimeString
		})))
		// #endif

		// #ifdef APP-PLUS
		const bitmap = new plus.nativeObj.Bitmap('bitmap' + Date.now())
		bitmap.loadBase64Data(base64, () => {
			if (!format) {
				reject(new Error('ERROR_BASE64SRC_PARSE'))
			}
			const time = new Date().getTime();
			const filePath = `_doc/uniapp_temp/${time}.${format}`
			bitmap.save(filePath, {},
				() => {
					bitmap.clear()
					resolve(filePath)
				},
				(error) => {
					bitmap.clear()
					reject(error)
				})
		}, (error) => {
			bitmap.clear()
			reject(error)
		})
		// #endif
	})
}

/**
 * 路径转base64
 * @param {Object} string
 */
export function pathToBase64(path) {
	if (/^data:/.test(path)) return path
	return new Promise((resolve, reject) => {
		// #ifdef H5
		let image = new Image();
		image.setAttribute("crossOrigin", 'Anonymous');
		image.onload = function() {
			let canvas = document.createElement('canvas');
			canvas.width = this.naturalWidth;
			canvas.height = this.naturalHeight;
			canvas.getContext('2d').drawImage(image, 0, 0);
			let result = canvas.toDataURL('image/png')
			resolve(result);
			canvas.height = canvas.width = 0
		}
		image.src = path + '?v=' + Math.random()
		image.onerror = (error) => {
			reject(error);
		};
		// #endif

		// #ifdef MP
		if (uni.canIUse('getFileSystemManager')) {
			uni.getFileSystemManager().readFile({
				filePath: path,
				encoding: 'base64',
				success: (res) => {
					resolve('data:image/png;base64,' + res.data)
				},
				fail: (error) => {
					console.error({error, path})
					reject(error)
				}
			})
		}
		// #endif

		// #ifdef APP-PLUS
		plus.io.resolveLocalFileSystemURL(getLocalFilePath(path), (entry) => {
			entry.file((file) => {
				const fileReader = new plus.io.FileReader()
				fileReader.onload = (data) => {
					resolve(data.target.result)
				}
				fileReader.onerror = (error) => {
					reject(error)
				}
				fileReader.readAsDataURL(file)
			}, reject)
		}, reject)
		// #endif
	})
}



export function getImageInfo(path, useCORS) {
	const isCanvas2D = this && this.canvas && this.canvas.createImage
	return new Promise(async (resolve, reject) => {
		// let time = +new Date()
		let src = path.replace(/^@\//,'/')
		if (cache[path] && cache[path].errMsg) {
			resolve(cache[path])
		} else {
			try {
				// #ifdef MP || APP-PLUS
				if (isBase64(path) && (isCanvas2D ? isPC : true)) {
					src = await base64ToPath(path)
				}
				// #endif
				// #ifdef H5
				if(useCORS) {
					src = await pathToBase64(path)
				}
				// #endif
			} catch (error) {
				reject({
					...error,
					src
				})
			}
			// #ifndef APP-NVUE
			if(isCanvas2D && !isPC) {
				const img = this.canvas.createImage()
				img.onload = function() {
					const image = {
						path: img,
						width:  img.width,
						height:  img.height
					}
					cache[path] = image
					resolve(cache[path])
				}
				img.onerror = function(err) {
					reject({err,path})
				}
				img.src = src
				return
			}
			// #endif
			uni.getImageInfo({
				src,
				success: (image) => {
					const localReg = /^\.|^\/(?=[^\/])/;
					// #ifdef MP-WEIXIN || MP-BAIDU || MP-QQ || MP-TOUTIAO
					image.path = localReg.test(src) ?  `/${image.path}` : image.path;
					// #endif
					if(isCanvas2D) {
						const img = this.canvas.createImage()
						img.onload = function() {
							image.path = img
							cache[path] = image
							resolve(cache[path])
						}
						img.onerror = function(err) {
							reject({err,path})
						}
						img.src = src
						return
					}
					// #ifdef APP-PLUS
					// console.log('getImageInfo', +new Date() - time)
					// ios 比较严格 可能需要设置跨域
					if(uni.getSystemInfoSync().osName == 'ios' && useCORS) {
						pathToBase64(image.path).then(base64 => {
							image.path = base64
							cache[path] = image
							resolve(cache[path])
						}).catch(err => {
							console.error({err, path})
							reject({err,path})
						})
						return
					}
					// #endif
					cache[path] = image
					resolve(cache[path])
				},
				fail(err) {
					console.error({err, path})
					reject({err,path})
				}
			})
		}
	})
}


// #ifdef APP-PLUS
const getLocalFilePath = (path) => {
	if (path.indexOf('_www') === 0 || path.indexOf('_doc') === 0 || path.indexOf('_documents') === 0 || path
		.indexOf('_downloads') === 0) {
		return path
	}
	if (path.indexOf('file://') === 0) {
		return path
	}
	if (path.indexOf('/storage/emulated/0/') === 0) {
		return path
	}
	if (path.indexOf('/') === 0) {
		const localFilePath = plus.io.convertAbsoluteFileSystem(path)
		if (localFilePath !== path) {
			return localFilePath
		} else {
			path = path.substr(1)
		}
	}
	return '_www/' + path
}
// #endif


