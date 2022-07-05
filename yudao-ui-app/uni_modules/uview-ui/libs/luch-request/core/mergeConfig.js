import { deepMerge, isUndefined } from '../utils'

/**
 * 合并局部配置优先的配置，如果局部有该配置项则用局部，如果全局有该配置项则用全局
 * @param {Array} keys - 配置项
 * @param {Object} globalsConfig - 当前的全局配置
 * @param {Object} config2 - 局部配置
 * @return {{}}
 */
const mergeKeys = (keys, globalsConfig, config2) => {
    const config = {}
    keys.forEach((prop) => {
        if (!isUndefined(config2[prop])) {
            config[prop] = config2[prop]
        } else if (!isUndefined(globalsConfig[prop])) {
            config[prop] = globalsConfig[prop]
        }
    })
    return config
}
/**
 *
 * @param globalsConfig - 当前实例的全局配置
 * @param config2 - 当前的局部配置
 * @return - 合并后的配置
 */
export default (globalsConfig, config2 = {}) => {
    const method = config2.method || globalsConfig.method || 'GET'
    let config = {
        baseURL: globalsConfig.baseURL || '',
        method,
        url: config2.url || '',
        params: config2.params || {},
        custom: { ...(globalsConfig.custom || {}), ...(config2.custom || {}) },
        header: deepMerge(globalsConfig.header || {}, config2.header || {})
    }
    const defaultToConfig2Keys = ['getTask', 'validateStatus']
    config = { ...config, ...mergeKeys(defaultToConfig2Keys, globalsConfig, config2) }

    // eslint-disable-next-line no-empty
    if (method === 'DOWNLOAD') {
    // #ifdef H5 || APP-PLUS
        if (!isUndefined(config2.timeout)) {
            config.timeout = config2.timeout
        } else if (!isUndefined(globalsConfig.timeout)) {
            config.timeout = globalsConfig.timeout
        }
    // #endif
    } else if (method === 'UPLOAD') {
        delete config.header['content-type']
        delete config.header['Content-Type']
        const uploadKeys = [
            // #ifdef APP-PLUS || H5
            'files',
            // #endif
            // #ifdef MP-ALIPAY
            'fileType',
            // #endif
            // #ifdef H5
            'file',
            // #endif
            'filePath',
            'name',
            // #ifdef H5 || APP-PLUS
            'timeout',
            // #endif
            'formData'
        ]
        uploadKeys.forEach((prop) => {
            if (!isUndefined(config2[prop])) {
                config[prop] = config2[prop]
            }
        })
        // #ifdef H5 || APP-PLUS
        if (isUndefined(config.timeout) && !isUndefined(globalsConfig.timeout)) {
            config.timeout = globalsConfig.timeout
        }
    // #endif
    } else {
        const defaultsKeys = [
            'data',
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
        config = { ...config, ...mergeKeys(defaultsKeys, globalsConfig, config2) }
    }

    return config
}
