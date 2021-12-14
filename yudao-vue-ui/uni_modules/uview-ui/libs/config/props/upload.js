/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:09:50
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/upload.js
 */
export default {
	// upload组件
	upload: {
		accept: 'image',
		capture: () => ['album', 'camera'],
		compressed: true,
		camera: 'back',
		maxDuration: 60,
		uploadIcon: 'camera-fill',
		uploadIconColor: '#D3D4D6',
		useBeforeRead: false,
		previewFullImage: true,
		maxCount: 52,
		disabled: false,
		imageMode: 'aspectFill',
		name: '',
		sizeType: () => ['original', 'compressed'],
		multiple: false,
		deletable: true,
		maxSize: Number.MAX_VALUE,
		fileList: () => [],
		uploadText: '',
		width: 80,
		height: 80,
		previewImage: true
	}
}
