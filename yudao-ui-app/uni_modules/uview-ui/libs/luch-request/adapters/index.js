import buildURL from '../helpers/buildURL'
import buildFullPath from '../core/buildFullPath'
import settle from '../core/settle'
import { isUndefined } from '../utils'

/**
 * 返回可选值存在的配置
 * @param {Array} keys - 可选值数组
 * @param {Object} config2 - 配置
 * @return {{}} - 存在的配置项
 */
const mergeKeys = (keys, config2) => {
    const config = {}
    keys.forEach((prop) => {
        if (!isUndefined(config2[prop])) {
            config[prop] = config2[prop]
        }
    })
    return config
}
export default (config) => new Promise((resolve, reject) => {
    const fullPath = buildURL(buildFullPath(config.baseURL, config.url), config.params)
    const _config = {
        url: fullPath,
        header: config.header,
        complete: (response) => {
            config.fullPath = fullPath
            response.config = config
            try {
                // 对可能字符串不是json 的情况容错
                if (typeof response.data === 'string') {
                    response.data = JSON.parse(response.data)
                }
                // eslint-disable-next-line no-empty
            } catch (e) {
            }
            settle(resolve, reject, response)
        }
    }
    let requestTask
    if (config.method === 'UPLOAD') {
        delete _config.header['content-type']
        delete _config.header['Content-Type']
        const otherConfig = {
        // #ifdef MP-ALIPAY
            fileType: config.fileType,
            // #endif
            filePath: config.filePath,
            name: config.name
        }
        const optionalKeys = [
        // #ifdef APP-PLUS || H5
            'files',
            // #endif
            // #ifdef H5
            'file',
            // #endif
            // #ifdef H5 || APP-PLUS
            'timeout',
            // #endif
            'formData'
        ]
        requestTask = uni.uploadFile({ ..._config, ...otherConfig, ...mergeKeys(optionalKeys, config) })
    } else if (config.method === 'DOWNLOAD') {
        // #ifdef H5 || APP-PLUS
        if (!isUndefined(config.timeout)) {
            _config.timeout = config.timeout
        }
        // #endif
        requestTask = uni.downloadFile(_config)
    } else {
        const optionalKeys = [
            'data',
            'method',
            // #ifdef H5 || APP-PLUS || MP-ALIPAY || MP-WEIXIN
            'timeout',
            // #endif
            'dataType',
            // #ifndef MP-ALIPAY
            'responseType',
            // #endif
            // #ifdef APP-PLUS
            'sslVerify',
            // #endif
            // #ifdef H5
            'withCredentials',
            // #endif
            // #ifdef APP-PLUS
            'firstIpv4'
        // #endif
        ]
        requestTask = uni.request({ ..._config, ...mergeKeys(optionalKeys, config) })
    }
    if (config.getTask) {
        config.getTask(requestTask, config)
    }
})
