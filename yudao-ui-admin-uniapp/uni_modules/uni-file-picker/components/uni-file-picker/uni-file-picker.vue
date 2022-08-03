<template>
	<view class="uni-file-picker">
		<view v-if="title" class="uni-file-picker__header">
			<text class="file-title">{{ title }}</text>
			<text class="file-count">{{ filesList.length }}/{{ limitLength }}</text>
		</view>
		<upload-image v-if="fileMediatype === 'image' && showType === 'grid'" :readonly="readonly"
			:image-styles="imageStyles" :files-list="filesList" :limit="limitLength" :disablePreview="disablePreview"
			:delIcon="delIcon" @uploadFiles="uploadFiles" @choose="choose" @delFile="delFile">
			<slot>
				<view class="is-add">
					<view class="icon-add"></view>
					<view class="icon-add rotate"></view>
				</view>
			</slot>
		</upload-image>
		<upload-file v-if="fileMediatype !== 'image' || showType !== 'grid'" :readonly="readonly"
			:list-styles="listStyles" :files-list="filesList" :showType="showType" :delIcon="delIcon"
			@uploadFiles="uploadFiles" @choose="choose" @delFile="delFile">
			<slot><button type="primary" size="mini">选择文件</button></slot>
		</upload-file>
	</view>
</template>

<script>
	import {
		chooseAndUploadFile,
		uploadCloudFiles
	} from './choose-and-upload-file.js'
	import {
		get_file_ext,
		get_extname,
		get_files_and_is_max,
		get_file_info,
		get_file_data
	} from './utils.js'
	import uploadImage from './upload-image.vue'
	import uploadFile from './upload-file.vue'
	let fileInput = null
	/**
	 * FilePicker 文件选择上传
	 * @description 文件选择上传组件，可以选择图片、视频等任意文件并上传到当前绑定的服务空间
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=4079
	 * @property {Object|Array}	value	组件数据，通常用来回显 ,类型由return-type属性决定
	 * @property {Boolean}	disabled = [true|false]	组件禁用
	 * 	@value true 	禁用
	 * 	@value false 	取消禁用
	 * @property {Boolean}	readonly = [true|false]	组件只读，不可选择，不显示进度，不显示删除按钮
	 * 	@value true 	只读
	 * 	@value false 	取消只读
	 * @property {String}	return-type = [array|object]	限制 value 格式，当为 object 时 ，组件只能单选，且会覆盖
	 * 	@value array	规定 value 属性的类型为数组
	 * 	@value object	规定 value 属性的类型为对象
	 * @property {Boolean}	disable-preview = [true|false]	禁用图片预览，仅 mode:grid 时生效
	 * 	@value true 	禁用图片预览
	 * 	@value false 	取消禁用图片预览
	 * @property {Boolean}	del-icon = [true|false]	是否显示删除按钮
	 * 	@value true 	显示删除按钮
	 * 	@value false 	不显示删除按钮
	 * @property {Boolean}	auto-upload = [true|false]	是否自动上传，值为true则只触发@select,可自行上传
	 * 	@value true 	自动上传
	 * 	@value false 	取消自动上传
	 * @property {Number|String}	limit	最大选择个数 ，h5 会自动忽略多选的部分
	 * @property {String}	title	组件标题，右侧显示上传计数
	 * @property {String}	mode = [list|grid]	选择文件后的文件列表样式
	 * 	@value list 	列表显示
	 * 	@value grid 	宫格显示
	 * @property {String}	file-mediatype = [image|video|all]	选择文件类型
	 * 	@value image	只选择图片
	 * 	@value video	只选择视频
	 * 	@value all		选择所有文件
	 * @property {Array}	file-extname	选择文件后缀，根据 file-mediatype 属性而不同
	 * @property {Object}	list-style	mode:list 时的样式
	 * @property {Object}	image-styles	选择文件后缀，根据 file-mediatype 属性而不同
	 * @event {Function} select 	选择文件后触发
	 * @event {Function} progress 文件上传时触发
	 * @event {Function} success 	上传成功触发
	 * @event {Function} fail 		上传失败触发
	 * @event {Function} delete 	文件从列表移除时触发
	 */
	export default {
		name: 'uniFilePicker',
		components: {
			uploadImage,
			uploadFile
		},
		options: {
			virtualHost: true
		},
		emits: ['select', 'success', 'fail', 'progress', 'delete', 'update:modelValue', 'input'],
		props: {
			// #ifdef VUE3
			modelValue: {
				type: [Array, Object],
				default () {
					return []
				}
			},
			// #endif

			// #ifndef VUE3
			value: {
				type: [Array, Object],
				default () {
					return []
				}
			},
			// #endif

			disabled: {
				type: Boolean,
				default: false
			},
			disablePreview: {
				type: Boolean,
				default: false
			},
			delIcon: {
				type: Boolean,
				default: true
			},
			// 自动上传
			autoUpload: {
				type: Boolean,
				default: true
			},
			// 最大选择个数 ，h5只能限制单选或是多选
			limit: {
				type: [Number, String],
				default: 9
			},
			// 列表样式 grid | list | list-card
			mode: {
				type: String,
				default: 'grid'
			},
			// 选择文件类型  image/video/all
			fileMediatype: {
				type: String,
				default: 'image'
			},
			// 文件类型筛选
			fileExtname: {
				type: [Array, String],
				default () {
					return []
				}
			},
			title: {
				type: String,
				default: ''
			},
			listStyles: {
				type: Object,
				default () {
					return {
						// 是否显示边框
						border: true,
						// 是否显示分隔线
						dividline: true,
						// 线条样式
						borderStyle: {}
					}
				}
			},
			imageStyles: {
				type: Object,
				default () {
					return {
						width: 'auto',
						height: 'auto'
					}
				}
			},
			readonly: {
				type: Boolean,
				default: false
			},
			returnType: {
				type: String,
				default: 'array'
			},
			sizeType: {
				type: Array,
				default () {
					return ['original', 'compressed']
				}
			}
		},
		data() {
			return {
				files: [],
				localValue: []
			}
		},
		watch: {
			// #ifndef VUE3
			value: {
				handler(newVal, oldVal) {
					this.setValue(newVal, oldVal)
				},
				immediate: true
			},
			// #endif
			// #ifdef VUE3
			modelValue: {
				handler(newVal, oldVal) {
					this.setValue(newVal, oldVal)
				},
				immediate: true
			},
			// #endif
		},
		computed: {
			filesList() {
				let files = []
				this.files.forEach(v => {
					files.push(v)
				})
				return files
			},
			showType() {
				if (this.fileMediatype === 'image') {
					return this.mode
				}
				return 'list'
			},
			limitLength() {
				if (this.returnType === 'object') {
					return 1
				}
				if (!this.limit) {
					return 1
				}
				if (this.limit >= 9) {
					return 9
				}
				return this.limit
			}
		},
		created() {
			// TODO 兼容不开通服务空间的情况
			if (!(uniCloud.config && uniCloud.config.provider)) {
				this.noSpace = true
				uniCloud.chooseAndUploadFile = chooseAndUploadFile
			}
			this.form = this.getForm('uniForms')
			this.formItem = this.getForm('uniFormsItem')
			if (this.form && this.formItem) {
				if (this.formItem.name) {
					this.rename = this.formItem.name
					this.form.inputChildrens.push(this)
				}
			}
		},
		methods: {
			/**
			 * 公开用户使用，清空文件
			 * @param {Object} index
			 */
			clearFiles(index) {
				if (index !== 0 && !index) {
					this.files = []
					this.$nextTick(() => {
						this.setEmit()
					})
				} else {
					this.files.splice(index, 1)
				}
				this.$nextTick(() => {
					this.setEmit()
				})
			},
			/**
			 * 公开用户使用，继续上传
			 */
			upload() {
				let files = []
				this.files.forEach((v, index) => {
					if (v.status === 'ready' || v.status === 'error') {
						files.push(Object.assign({}, v))
					}
				})
				return this.uploadFiles(files)
			},
			async setValue(newVal, oldVal) {
				const newData =  async (v) => {
					const reg = /cloud:\/\/([\w.]+\/?)\S*/
					let url = ''
					if(v.fileID){
						url = v.fileID
					}else{
						url = v.url
					}
					if (reg.test(url)) {
						v.fileID = url
						v.url = await this.getTempFileURL(url)
					}
					if(v.url) v.path = v.url
					return v
				}
				if (this.returnType === 'object') {
					if (newVal) {
						await newData(newVal)
					} else {
						newVal = {}
					}
				} else {
					if (!newVal) newVal = []
					for(let i =0 ;i < newVal.length ;i++){
						let v = newVal[i]
						await newData(v)
					}
				}
				this.localValue = newVal
				if (this.form && this.formItem &&!this.is_reset) {
					this.is_reset = false
					this.formItem.setValue(this.localValue)
				}
				let filesData = Object.keys(newVal).length > 0 ? newVal : [];
				this.files = [].concat(filesData)
			},

			/**
			 * 选择文件
			 */
			choose() {

				if (this.disabled) return
				if (this.files.length >= Number(this.limitLength) && this.showType !== 'grid' && this.returnType ===
					'array') {
					uni.showToast({
						title: `您最多选择 ${this.limitLength} 个文件`,
						icon: 'none'
					})
					return
				}
				this.chooseFiles()
			},

			/**
			 * 选择文件并上传
			 */
			chooseFiles() {
				const _extname = get_extname(this.fileExtname)
				// 获取后缀
				uniCloud
					.chooseAndUploadFile({
						type: this.fileMediatype,
						compressed: false,
						sizeType: this.sizeType,
						// TODO 如果为空，video 有问题
						extension: _extname.length > 0 ? _extname : undefined,
						count: this.limitLength - this.files.length, //默认9
						onChooseFile: this.chooseFileCallback,
						onUploadProgress: progressEvent => {
							this.setProgress(progressEvent, progressEvent.index)
						}
					})
					.then(result => {
						this.setSuccessAndError(result.tempFiles)
					})
					.catch(err => {
						console.log('选择失败', err)
					})
			},

			/**
			 * 选择文件回调
			 * @param {Object} res
			 */
			async chooseFileCallback(res) {
				const _extname = get_extname(this.fileExtname)
				const is_one = (Number(this.limitLength) === 1 &&
						this.disablePreview &&
						!this.disabled) ||
					this.returnType === 'object'
				// 如果这有一个文件 ，需要清空本地缓存数据
				if (is_one) {
					this.files = []
				}

				let {
					filePaths,
					files
				} = get_files_and_is_max(res, _extname)
				if (!(_extname && _extname.length > 0)) {
					filePaths = res.tempFilePaths
					files = res.tempFiles
				}

				let currentData = []
				for (let i = 0; i < files.length; i++) {
					if (this.limitLength - this.files.length <= 0) break
					files[i].uuid = Date.now()
					let filedata = await get_file_data(files[i], this.fileMediatype)
					filedata.progress = 0
					filedata.status = 'ready'
					this.files.push(filedata)
					currentData.push({
						...filedata,
						file: files[i]
					})
				}
				this.$emit('select', {
					tempFiles: currentData,
					tempFilePaths: filePaths
				})
				res.tempFiles = files
				// 停止自动上传
				if (!this.autoUpload || this.noSpace) {
					res.tempFiles = []
				}
			},

			/**
			 * 批传
			 * @param {Object} e
			 */
			uploadFiles(files) {
				files = [].concat(files)
				return uploadCloudFiles.call(this, files, 5, res => {
						this.setProgress(res, res.index, true)
					})
					.then(result => {
						this.setSuccessAndError(result)
						return result;
					})
					.catch(err => {
						console.log(err)
					})
			},

			/**
			 * 成功或失败
			 */
			async setSuccessAndError(res, fn) {
				let successData = []
				let errorData = []
				let tempFilePath = []
				let errorTempFilePath = []
				for (let i = 0; i < res.length; i++) {
					const item = res[i]
					const index = item.uuid ? this.files.findIndex(p => p.uuid === item.uuid) : item.index

					if (index === -1 || !this.files) break
					if (item.errMsg === 'request:fail') {
						this.files[index].url = item.path
						this.files[index].status = 'error'
						this.files[index].errMsg = item.errMsg
						// this.files[index].progress = -1
						errorData.push(this.files[index])
						errorTempFilePath.push(this.files[index].url)
					} else {
						this.files[index].errMsg = ''
						this.files[index].fileID = item.url
						const reg = /cloud:\/\/([\w.]+\/?)\S*/
						if (reg.test(item.url)) {
							this.files[index].url = await this.getTempFileURL(item.url)
						}else{
							this.files[index].url = item.url
						}

						this.files[index].status = 'success'
						this.files[index].progress += 1
						successData.push(this.files[index])
						tempFilePath.push(this.files[index].fileID)
					}
				}

				if (successData.length > 0) {
					this.setEmit()
					// 状态改变返回
					this.$emit('success', {
						tempFiles: this.backObject(successData),
						tempFilePaths: tempFilePath
					})
				}

				if (errorData.length > 0) {
					this.$emit('fail', {
						tempFiles: this.backObject(errorData),
						tempFilePaths: errorTempFilePath
					})
				}
			},

			/**
			 * 获取进度
			 * @param {Object} progressEvent
			 * @param {Object} index
			 * @param {Object} type
			 */
			setProgress(progressEvent, index, type) {
				const fileLenth = this.files.length
				const percentNum = (index / fileLenth) * 100
				const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
				let idx = index
				if (!type) {
					idx = this.files.findIndex(p => p.uuid === progressEvent.tempFile.uuid)
				}
				if (idx === -1 || !this.files[idx]) return
				// fix by mehaotian 100 就会消失，-1 是为了让进度条消失
				this.files[idx].progress = percentCompleted - 1
				// 上传中
				this.$emit('progress', {
					index: idx,
					progress: parseInt(percentCompleted),
					tempFile: this.files[idx]
				})
			},

			/**
			 * 删除文件
			 * @param {Object} index
			 */
			delFile(index) {
				this.$emit('delete', {
					tempFile: this.files[index],
					tempFilePath: this.files[index].url
				})
				this.files.splice(index, 1)
				this.$nextTick(() => {
					this.setEmit()
				})
			},

			/**
			 * 获取文件名和后缀
			 * @param {Object} name
			 */
			getFileExt(name) {
				const last_len = name.lastIndexOf('.')
				const len = name.length
				return {
					name: name.substring(0, last_len),
					ext: name.substring(last_len + 1, len)
				}
			},

			/**
			 * 处理返回事件
			 */
			setEmit() {
				let data = []
				if (this.returnType === 'object') {
					data = this.backObject(this.files)[0]
					this.localValue = data?data:null
				} else {
					data = this.backObject(this.files)
					if (!this.localValue) {
						this.localValue = []
					}
					this.localValue = [...data]
				}
				// #ifdef VUE3
				this.$emit('update:modelValue', this.localValue)
				// #endif
				// #ifndef VUE3
				this.$emit('input', this.localValue)
				// #endif
			},

			/**
			 * 处理返回参数
			 * @param {Object} files
			 */
			backObject(files) {
				let newFilesData = []
				files.forEach(v => {
					newFilesData.push({
						extname: v.extname,
						fileType: v.fileType,
						image: v.image,
						name: v.name,
						path: v.path,
						size: v.size,
						fileID:v.fileID,
						url: v.url
					})
				})
				return newFilesData
			},
			async getTempFileURL(fileList) {
				fileList = {
					fileList: [].concat(fileList)
				}
				const urls = await uniCloud.getTempFileURL(fileList)
				return urls.fileList[0].tempFileURL || ''
			},
			/**
			 * 获取父元素实例
			 */
			getForm(name = 'uniForms') {
				let parent = this.$parent;
				let parentName = parent.$options.name;
				while (parentName !== name) {
					parent = parent.$parent;
					if (!parent) return false;
					parentName = parent.$options.name;
				}
				return parent;
			}
		}
	}
</script>

<style>
	.uni-file-picker {
		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		overflow: hidden;
		width: 100%;
		/* #endif */
		flex: 1;
	}

	.uni-file-picker__header {
		padding-top: 5px;
		padding-bottom: 10px;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: space-between;
	}

	.file-title {
		font-size: 14px;
		color: #333;
	}

	.file-count {
		font-size: 14px;
		color: #999;
	}

	.is-add {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		align-items: center;
		justify-content: center;
	}

	.icon-add {
		width: 50px;
		height: 5px;
		background-color: #f1f1f1;
		border-radius: 2px;
	}

	.rotate {
		position: absolute;
		transform: rotate(90deg);
	}
</style>
