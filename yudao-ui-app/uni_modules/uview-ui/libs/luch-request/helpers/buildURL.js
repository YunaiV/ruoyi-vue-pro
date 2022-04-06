'use strict'

import * as utils from '../utils'

function encode(val) {
    return encodeURIComponent(val)
        .replace(/%40/gi, '@')
        .replace(/%3A/gi, ':')
        .replace(/%24/g, '$')
        .replace(/%2C/gi, ',')
        .replace(/%20/g, '+')
        .replace(/%5B/gi, '[')
        .replace(/%5D/gi, ']')
}

/**
 * Build a URL by appending params to the end
 *
 * @param {string} url The base of the url (e.g., http://www.google.com)
 * @param {object} [params] The params to be appended
 * @returns {string} The formatted url
 */
export default function buildURL(url, params) {
    /* eslint no-param-reassign:0 */
    if (!params) {
        return url
    }

    let serializedParams
    if (utils.isURLSearchParams(params)) {
        serializedParams = params.toString()
    } else {
        const parts = []

        utils.forEach(params, (val, key) => {
            if (val === null || typeof val === 'undefined') {
                return
            }

            if (utils.isArray(val)) {
                key = `${key}[]`
            } else {
                val = [val]
            }

            utils.forEach(val, (v) => {
                if (utils.isDate(v)) {
                    v = v.toISOString()
                } else if (utils.isObject(v)) {
                    v = JSON.stringify(v)
                }
                parts.push(`${encode(key)}=${encode(v)}`)
            })
        })

        serializedParams = parts.join('&')
    }

    if (serializedParams) {
        const hashmarkIndex = url.indexOf('#')
        if (hashmarkIndex !== -1) {
            url = url.slice(0, hashmarkIndex)
        }

        url += (url.indexOf('?') === -1 ? '?' : '&') + serializedParams
    }

    return url
}
