function pickExclude(obj, keys) {
	// 某些情况下，type可能会为
    if (!['[object Object]', '[object File]'].includes(Object.prototype.toString.call(obj))) {
        return {}
    }
    return Object.keys(obj).reduce((prev, key) => {
        if (!keys.includes(key)) {
            prev[key] = obj[key]
        }
        return prev
    }, {})
}

function formatImage(res) {
    return res.tempFiles.map((item) => ({
        ...pickExclude(item, ['path']),
        type: 'image',
        url: item.path,
        thumb: item.path,
		size: item.size,
		// #ifdef H5
		name: item.name
		// #endif
    }))
}

function formatVideo(res) {
    return [
        {
            ...pickExclude(res, ['tempFilePath', 'thumbTempFilePath', 'errMsg']),
            type: 'video',
            url: res.tempFilePath,
            thumb: res.thumbTempFilePath,
			size: res.size,
			// #ifdef H5
			name: res.name
			// #endif
        }
    ]
}

function formatMedia(res) {
    return res.tempFiles.map((item) => ({
        ...pickExclude(item, ['fileType', 'thumbTempFilePath', 'tempFilePath']),
        type: res.type,
        url: item.tempFilePath,
        thumb: res.type === 'video' ? item.thumbTempFilePath : item.tempFilePath,
		size: item.size
    }))
}

function formatFile(res) {
    return res.tempFiles.map((item) => ({ 
		...pickExclude(item, ['path']), 
		url: item.path, 
		size:item.size,
		// #ifdef H5
		name: item.name,
		type: item.type
		// #endif 
	}))
}
export function chooseFile({
    accept,
    multiple,
    capture,
    compressed,
    maxDuration,
    sizeType,
    camera,
    maxCount
}) {
    return new Promise((resolve, reject) => {
        switch (accept) {
        case 'image':
            uni.chooseImage({
                count: multiple ? Math.min(maxCount, 9) : 1,
                sourceType: capture,
                sizeType,
                success: (res) => resolve(formatImage(res)),
                fail: reject
            })
            break
            // #ifdef MP-WEIXIN
            // 只有微信小程序才支持chooseMedia接口
        case 'media':
            wx.chooseMedia({
                count: multiple ? Math.min(maxCount, 9) : 1,
                sourceType: capture,
                maxDuration,
                sizeType,
                camera,
                success: (res) => resolve(formatMedia(res)),
                fail: reject
            })
            break
            // #endif
        case 'video':
            uni.chooseVideo({
                sourceType: capture,
                compressed,
                maxDuration,
                camera,
                success: (res) => resolve(formatVideo(res)),
                fail: reject
            })
            break
            // #ifdef MP-WEIXIN || H5
            // 只有微信小程序才支持chooseMessageFile接口
        case 'file':
            // #ifdef MP-WEIXIN
            wx.chooseMessageFile({
                count: multiple ? maxCount : 1,
                type: accept,
                success: (res) => resolve(formatFile(res)),
                fail: reject
            })
            // #endif
            // #ifdef H5
            // 需要hx2.9.9以上才支持uni.chooseFile
            uni.chooseFile({
                count: multiple ? maxCount : 1,
                type: accept,
                success: (res) => resolve(formatFile(res)),
                fail: reject
            })
            // #endif
            break
				// #endif
		default: 
			// 此为保底选项，在accept不为上面任意一项的时候选取全部文件
			// #ifdef MP-WEIXIN
			wx.chooseMessageFile({
			    count: multiple ? maxCount : 1,
			    type: 'all',
			    success: (res) => resolve(formatFile(res)),
			    fail: reject
			})
			// #endif
			// #ifdef H5
			// 需要hx2.9.9以上才支持uni.chooseFile
			uni.chooseFile({
				count: multiple ? maxCount : 1,
				type: 'all',
				success: (res) => resolve(formatFile(res)),
				fail: reject
			})
			// #endif
        }
    })
}
