import config from '@/config'
const baseUrl = config.baseUrl
export const myRequest = (option = {}) => {
	return new Promise((reslove, reject) => {
		uni.request({
			url: baseUrl + option.url,
			data: option.data,
			method: option.method || "GET",
			success: (result) => {
				reslove(result)
			},
			fail: (error) => {
				reject(error)
			}
		})
	})
}
