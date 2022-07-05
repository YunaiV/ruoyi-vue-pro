function _extends() {
    _extends = Object.assign || function (target) {
        for (let i = 1; i < arguments.length; i++) {
            const source = arguments[i]

            for (const key in source) {
                if (Object.prototype.hasOwnProperty.call(source, key)) {
                    target[key] = source[key]
                }
            }
        }

        return target
    }

    return _extends.apply(this, arguments)
}

/* eslint no-console:0 */
const formatRegExp = /%[sdj%]/g
let warning = function warning() {} // don't print warning message when in production env or node runtime

if (typeof process !== 'undefined' && process.env && process.env.NODE_ENV !== 'production' && typeof window
	!== 'undefined' && typeof document !== 'undefined') {
    warning = function warning(type, errors) {
        if (typeof console !== 'undefined' && console.warn) {
            if (errors.every((e) => typeof e === 'string')) {
                console.warn(type, errors)
            }
        }
    }
}

function convertFieldsError(errors) {
    if (!errors || !errors.length) return null
    const fields = {}
    errors.forEach((error) => {
        const { field } = error
        fields[field] = fields[field] || []
        fields[field].push(error)
    })
    return fields
}

function format() {
    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
        args[_key] = arguments[_key]
    }

    let i = 1
    const f = args[0]
    const len = args.length

    if (typeof f === 'function') {
        return f.apply(null, args.slice(1))
    }

    if (typeof f === 'string') {
        let str = String(f).replace(formatRegExp, (x) => {
            if (x === '%%') {
                return '%'
            }

            if (i >= len) {
                return x
            }

            switch (x) {
            case '%s':
                return String(args[i++])

            case '%d':
                return Number(args[i++])

            case '%j':
                try {
                    return JSON.stringify(args[i++])
                } catch (_) {
                    return '[Circular]'
                }

                break

            default:
                return x
            }
        })

        for (let arg = args[i]; i < len; arg = args[++i]) {
            str += ` ${arg}`
        }

        return str
    }

    return f
}

function isNativeStringType(type) {
    return type === 'string' || type === 'url' || type === 'hex' || type === 'email' || type === 'pattern'
}

function isEmptyValue(value, type) {
    if (value === undefined || value === null) {
        return true
    }

    if (type === 'array' && Array.isArray(value) && !value.length) {
        return true
    }

    if (isNativeStringType(type) && typeof value === 'string' && !value) {
        return true
    }

    return false
}

function asyncParallelArray(arr, func, callback) {
    const results = []
    let total = 0
    const arrLength = arr.length

    function count(errors) {
        results.push.apply(results, errors)
        total++

        if (total === arrLength) {
            callback(results)
        }
    }

    arr.forEach((a) => {
        func(a, count)
    })
}

function asyncSerialArray(arr, func, callback) {
    let index = 0
    const arrLength = arr.length

    function next(errors) {
        if (errors && errors.length) {
            callback(errors)
            return
        }

        const original = index
        index += 1

        if (original < arrLength) {
            func(arr[original], next)
        } else {
            callback([])
        }
    }

    next([])
}

function flattenObjArr(objArr) {
    const ret = []
    Object.keys(objArr).forEach((k) => {
        ret.push.apply(ret, objArr[k])
    })
    return ret
}

function asyncMap(objArr, option, func, callback) {
    if (option.first) {
        const _pending = new Promise((resolve, reject) => {
            const next = function next(errors) {
                callback(errors)
                return errors.length ? reject({
                    errors,
                    fields: convertFieldsError(errors)
                }) : resolve()
            }

            const flattenArr = flattenObjArr(objArr)
            asyncSerialArray(flattenArr, func, next)
        })

        _pending.catch((e) => e)

        return _pending
    }

    let firstFields = option.firstFields || []

    if (firstFields === true) {
        firstFields = Object.keys(objArr)
    }

    const objArrKeys = Object.keys(objArr)
    const objArrLength = objArrKeys.length
    let total = 0
    const results = []
    const pending = new Promise((resolve, reject) => {
        const next = function next(errors) {
            results.push.apply(results, errors)
            total++

            if (total === objArrLength) {
                callback(results)
                return results.length ? reject({
                    errors: results,
                    fields: convertFieldsError(results)
                }) : resolve()
            }
        }

        if (!objArrKeys.length) {
            callback(results)
            resolve()
        }

        objArrKeys.forEach((key) => {
            const arr = objArr[key]

            if (firstFields.indexOf(key) !== -1) {
                asyncSerialArray(arr, func, next)
            } else {
                asyncParallelArray(arr, func, next)
            }
        })
    })
    pending.catch((e) => e)
    return pending
}

function complementError(rule) {
    return function (oe) {
        if (oe && oe.message) {
            oe.field = oe.field || rule.fullField
            return oe
        }

        return {
            message: typeof oe === 'function' ? oe() : oe,
            field: oe.field || rule.fullField
        }
    }
}

function deepMerge(target, source) {
    if (source) {
        for (const s in source) {
            if (source.hasOwnProperty(s)) {
                const value = source[s]

                if (typeof value === 'object' && typeof target[s] === 'object') {
                    target[s] = { ...target[s], ...value }
                } else {
                    target[s] = value
                }
            }
        }
    }

    return target
}

/**
 *  Rule for validating required fields.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function required(rule, value, source, errors, options, type) {
    if (rule.required && (!source.hasOwnProperty(rule.field) || isEmptyValue(value, type || rule.type))) {
        errors.push(format(options.messages.required, rule.fullField))
    }
}

/**
 *  Rule for validating whitespace.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function whitespace(rule, value, source, errors, options) {
    if (/^\s+$/.test(value) || value === '') {
        errors.push(format(options.messages.whitespace, rule.fullField))
    }
}

/* eslint max-len:0 */

const pattern = {
    // http://emailregex.com/
    email: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
    url: new RegExp(
        '^(?!mailto:)(?:(?:http|https|ftp)://|//)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$',
        'i'
    ),
    hex: /^#?([a-f0-9]{6}|[a-f0-9]{3})$/i
}
var types = {
    integer: function integer(value) {
        return /^(-)?\d+$/.test(value);
    },
    float: function float(value) {
        return /^(-)?\d+(\.\d+)?$/.test(value);
    },
    array: function array(value) {
        return Array.isArray(value)
    },
    regexp: function regexp(value) {
        if (value instanceof RegExp) {
            return true
        }

        try {
            return !!new RegExp(value)
        } catch (e) {
            return false
        }
    },
    date: function date(value) {
        return typeof value.getTime === 'function' && typeof value.getMonth === 'function' && typeof value.getYear
			=== 'function'
    },
    number: function number(value) {
        if (isNaN(value)) {
            return false
        }

        // 修改源码，将字符串数值先转为数值
        return typeof +value === 'number'
    },
    object: function object(value) {
        return typeof value === 'object' && !types.array(value)
    },
    method: function method(value) {
        return typeof value === 'function'
    },
    email: function email(value) {
        return typeof value === 'string' && !!value.match(pattern.email) && value.length < 255
    },
    url: function url(value) {
        return typeof value === 'string' && !!value.match(pattern.url)
    },
    hex: function hex(value) {
        return typeof value === 'string' && !!value.match(pattern.hex)
    }
}
/**
 *  Rule for validating the type of a value.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function type(rule, value, source, errors, options) {
    if (rule.required && value === undefined) {
        required(rule, value, source, errors, options)
        return
    }

    const custom = ['integer', 'float', 'array', 'regexp', 'object', 'method', 'email', 'number', 'date', 'url', 'hex']
    const ruleType = rule.type

    if (custom.indexOf(ruleType) > -1) {
        if (!types[ruleType](value)) {
            errors.push(format(options.messages.types[ruleType], rule.fullField, rule.type))
        } // straight typeof check
    } else if (ruleType && typeof value !== rule.type) {
        errors.push(format(options.messages.types[ruleType], rule.fullField, rule.type))
    }
}

/**
 *  Rule for validating minimum and maximum allowed values.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function range(rule, value, source, errors, options) {
    const len = typeof rule.len === 'number'
    const min = typeof rule.min === 'number'
    const max = typeof rule.max === 'number' // 正则匹配码点范围从U+010000一直到U+10FFFF的文字（补充平面Supplementary Plane）

    const spRegexp = /[\uD800-\uDBFF][\uDC00-\uDFFF]/g
    let val = value
    let key = null
    const num = typeof value === 'number'
    const str = typeof value === 'string'
    const arr = Array.isArray(value)

    if (num) {
        key = 'number'
    } else if (str) {
        key = 'string'
    } else if (arr) {
        key = 'array'
    } // if the value is not of a supported type for range validation
    // the validation rule rule should use the
    // type property to also test for a particular type

    if (!key) {
        return false
    }

    if (arr) {
        val = value.length
    }

    if (str) {
        // 处理码点大于U+010000的文字length属性不准确的bug，如"𠮷𠮷𠮷".lenght !== 3
        val = value.replace(spRegexp, '_').length
    }

    if (len) {
        if (val !== rule.len) {
            errors.push(format(options.messages[key].len, rule.fullField, rule.len))
        }
    } else if (min && !max && val < rule.min) {
        errors.push(format(options.messages[key].min, rule.fullField, rule.min))
    } else if (max && !min && val > rule.max) {
        errors.push(format(options.messages[key].max, rule.fullField, rule.max))
    } else if (min && max && (val < rule.min || val > rule.max)) {
        errors.push(format(options.messages[key].range, rule.fullField, rule.min, rule.max))
    }
}

const ENUM = 'enum'
/**
 *  Rule for validating a value exists in an enumerable list.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function enumerable(rule, value, source, errors, options) {
    rule[ENUM] = Array.isArray(rule[ENUM]) ? rule[ENUM] : []

    if (rule[ENUM].indexOf(value) === -1) {
        errors.push(format(options.messages[ENUM], rule.fullField, rule[ENUM].join(', ')))
    }
}

/**
 *  Rule for validating a regular expression pattern.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param source The source object being validated.
 *  @param errors An array of errors that this rule may add
 *  validation errors to.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function pattern$1(rule, value, source, errors, options) {
    if (rule.pattern) {
        if (rule.pattern instanceof RegExp) {
            // if a RegExp instance is passed, reset `lastIndex` in case its `global`
            // flag is accidentally set to `true`, which in a validation scenario
            // is not necessary and the result might be misleading
            rule.pattern.lastIndex = 0

            if (!rule.pattern.test(value)) {
                errors.push(format(options.messages.pattern.mismatch, rule.fullField, value, rule.pattern))
            }
        } else if (typeof rule.pattern === 'string') {
            const _pattern = new RegExp(rule.pattern)

            if (!_pattern.test(value)) {
                errors.push(format(options.messages.pattern.mismatch, rule.fullField, value, rule.pattern))
            }
        }
    }
}

const rules = {
    required,
    whitespace,
    type,
    range,
    enum: enumerable,
    pattern: pattern$1
}

/**
 *  Performs validation for string types.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function string(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value, 'string') && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options, 'string')

        if (!isEmptyValue(value, 'string')) {
            rules.type(rule, value, source, errors, options)
            rules.range(rule, value, source, errors, options)
            rules.pattern(rule, value, source, errors, options)

            if (rule.whitespace === true) {
                rules.whitespace(rule, value, source, errors, options)
            }
        }
    }

    callback(errors)
}

/**
 *  Validates a function.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function method(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates a number.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function number(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (value === '') {
            value = undefined
        }

        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
            rules.range(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates a boolean.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function _boolean(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates the regular expression type.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function regexp(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (!isEmptyValue(value)) {
            rules.type(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates a number is an integer.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function integer(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
            rules.range(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates a number is a floating point number.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function floatFn(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
            rules.range(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates an array.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function array(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value, 'array') && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options, 'array')

        if (!isEmptyValue(value, 'array')) {
            rules.type(rule, value, source, errors, options)
            rules.range(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates an object.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function object(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules.type(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

const ENUM$1 = 'enum'
/**
 *  Validates an enumerable list.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function enumerable$1(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (value !== undefined) {
            rules[ENUM$1](rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Validates a regular expression pattern.
 *
 *  Performs validation when a rule only contains
 *  a pattern property but is not declared as a string type.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function pattern$2(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value, 'string') && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (!isEmptyValue(value, 'string')) {
            rules.pattern(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

function date(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)

        if (!isEmptyValue(value)) {
            let dateObject

            if (typeof value === 'number') {
                dateObject = new Date(value)
            } else {
                dateObject = value
            }

            rules.type(rule, dateObject, source, errors, options)

            if (dateObject) {
                rules.range(rule, dateObject.getTime(), source, errors, options)
            }
        }
    }

    callback(errors)
}

function required$1(rule, value, callback, source, options) {
    const errors = []
    const type = Array.isArray(value) ? 'array' : typeof value
    rules.required(rule, value, source, errors, options, type)
    callback(errors)
}

function type$1(rule, value, callback, source, options) {
    const ruleType = rule.type
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value, ruleType) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options, ruleType)

        if (!isEmptyValue(value, ruleType)) {
            rules.type(rule, value, source, errors, options)
        }
    }

    callback(errors)
}

/**
 *  Performs validation for any type.
 *
 *  @param rule The validation rule.
 *  @param value The value of the field on the source object.
 *  @param callback The callback function.
 *  @param source The source object being validated.
 *  @param options The validation options.
 *  @param options.messages The validation messages.
 */

function any(rule, value, callback, source, options) {
    const errors = []
    const validate = rule.required || !rule.required && source.hasOwnProperty(rule.field)

    if (validate) {
        if (isEmptyValue(value) && !rule.required) {
            return callback()
        }

        rules.required(rule, value, source, errors, options)
    }

    callback(errors)
}

const validators = {
    string,
    method,
    number,
    boolean: _boolean,
    regexp,
    integer,
    float: floatFn,
    array,
    object,
    enum: enumerable$1,
    pattern: pattern$2,
    date,
    url: type$1,
    hex: type$1,
    email: type$1,
    required: required$1,
    any
}

function newMessages() {
    return {
        default: 'Validation error on field %s',
        required: '%s is required',
        enum: '%s must be one of %s',
        whitespace: '%s cannot be empty',
        date: {
            format: '%s date %s is invalid for format %s',
            parse: '%s date could not be parsed, %s is invalid ',
            invalid: '%s date %s is invalid'
        },
        types: {
            string: '%s is not a %s',
            method: '%s is not a %s (function)',
            array: '%s is not an %s',
            object: '%s is not an %s',
            number: '%s is not a %s',
            date: '%s is not a %s',
            boolean: '%s is not a %s',
            integer: '%s is not an %s',
            float: '%s is not a %s',
            regexp: '%s is not a valid %s',
            email: '%s is not a valid %s',
            url: '%s is not a valid %s',
            hex: '%s is not a valid %s'
        },
        string: {
            len: '%s must be exactly %s characters',
            min: '%s must be at least %s characters',
            max: '%s cannot be longer than %s characters',
            range: '%s must be between %s and %s characters'
        },
        number: {
            len: '%s must equal %s',
            min: '%s cannot be less than %s',
            max: '%s cannot be greater than %s',
            range: '%s must be between %s and %s'
        },
        array: {
            len: '%s must be exactly %s in length',
            min: '%s cannot be less than %s in length',
            max: '%s cannot be greater than %s in length',
            range: '%s must be between %s and %s in length'
        },
        pattern: {
            mismatch: '%s value %s does not match pattern %s'
        },
        clone: function clone() {
            const cloned = JSON.parse(JSON.stringify(this))
            cloned.clone = this.clone
            return cloned
        }
    }
}
const messages = newMessages()

/**
 *  Encapsulates a validation schema.
 *
 *  @param descriptor An object declaring validation rules
 *  for this schema.
 */

function Schema(descriptor) {
    this.rules = null
    this._messages = messages
    this.define(descriptor)
}

Schema.prototype = {
    messages: function messages(_messages) {
        if (_messages) {
            this._messages = deepMerge(newMessages(), _messages)
        }

        return this._messages
    },
    define: function define(rules) {
        if (!rules) {
            throw new Error('Cannot configure a schema with no rules')
        }

        if (typeof rules !== 'object' || Array.isArray(rules)) {
            throw new Error('Rules must be an object')
        }

        this.rules = {}
        let z
        let item

        for (z in rules) {
            if (rules.hasOwnProperty(z)) {
                item = rules[z]
                this.rules[z] = Array.isArray(item) ? item : [item]
            }
        }
    },
    validate: function validate(source_, o, oc) {
        const _this = this

        if (o === void 0) {
            o = {}
        }

        if (oc === void 0) {
            oc = function oc() {}
        }

        let source = source_
        let options = o
        let callback = oc

        if (typeof options === 'function') {
            callback = options
            options = {}
        }

        if (!this.rules || Object.keys(this.rules).length === 0) {
            if (callback) {
                callback()
            }

            return Promise.resolve()
        }

        function complete(results) {
            let i
            let errors = []
            let fields = {}

            function add(e) {
                if (Array.isArray(e)) {
                    let _errors

                    errors = (_errors = errors).concat.apply(_errors, e)
                } else {
                    errors.push(e)
                }
            }

            for (i = 0; i < results.length; i++) {
                add(results[i])
            }

            if (!errors.length) {
                errors = null
                fields = null
            } else {
                fields = convertFieldsError(errors)
            }

            callback(errors, fields)
        }

        if (options.messages) {
            let messages$1 = this.messages()

            if (messages$1 === messages) {
                messages$1 = newMessages()
            }

            deepMerge(messages$1, options.messages)
            options.messages = messages$1
        } else {
            options.messages = this.messages()
        }

        let arr
        let value
        const series = {}
        const keys = options.keys || Object.keys(this.rules)
        keys.forEach((z) => {
            arr = _this.rules[z]
            value = source[z]
            arr.forEach((r) => {
                let rule = r

                if (typeof rule.transform === 'function') {
                    if (source === source_) {
                        source = { ...source }
                    }

                    value = source[z] = rule.transform(value)
                }

                if (typeof rule === 'function') {
                    rule = {
                        validator: rule
                    }
                } else {
                    rule = { ...rule }
                }

                rule.validator = _this.getValidationMethod(rule)
                rule.field = z
                rule.fullField = rule.fullField || z
                rule.type = _this.getType(rule)

                if (!rule.validator) {
                    return
                }

                series[z] = series[z] || []
                series[z].push({
                    rule,
                    value,
                    source,
                    field: z
                })
            })
        })
        const errorFields = {}
        return asyncMap(series, options, (data, doIt) => {
            const { rule } = data
            let deep = (rule.type === 'object' || rule.type === 'array') && (typeof rule.fields === 'object' || typeof rule.defaultField
				=== 'object')
            deep = deep && (rule.required || !rule.required && data.value)
            rule.field = data.field

            function addFullfield(key, schema) {
                return { ...schema, fullField: `${rule.fullField}.${key}` }
            }

            function cb(e) {
                if (e === void 0) {
                    e = []
                }

                let errors = e

                if (!Array.isArray(errors)) {
                    errors = [errors]
                }

                if (!options.suppressWarning && errors.length) {
                    Schema.warning('async-validator:', errors)
                }

                if (errors.length && rule.message) {
                    errors = [].concat(rule.message)
                }

                errors = errors.map(complementError(rule))

                if (options.first && errors.length) {
                    errorFields[rule.field] = 1
                    return doIt(errors)
                }

                if (!deep) {
                    doIt(errors)
                } else {
                    // if rule is required but the target object
                    // does not exist fail at the rule level and don't
                    // go deeper
                    if (rule.required && !data.value) {
                        if (rule.message) {
                            errors = [].concat(rule.message).map(complementError(rule))
                        } else if (options.error) {
                            errors = [options.error(rule, format(options.messages.required, rule.field))]
                        } else {
                            errors = []
                        }

                        return doIt(errors)
                    }

                    let fieldsSchema = {}

                    if (rule.defaultField) {
                        for (const k in data.value) {
                            if (data.value.hasOwnProperty(k)) {
                                fieldsSchema[k] = rule.defaultField
                            }
                        }
                    }

                    fieldsSchema = { ...fieldsSchema, ...data.rule.fields }

                    for (const f in fieldsSchema) {
                        if (fieldsSchema.hasOwnProperty(f)) {
                            const fieldSchema = Array.isArray(fieldsSchema[f]) ? fieldsSchema[f] : [fieldsSchema[f]]
                            fieldsSchema[f] = fieldSchema.map(addFullfield.bind(null, f))
                        }
                    }

                    const schema = new Schema(fieldsSchema)
                    schema.messages(options.messages)

                    if (data.rule.options) {
                        data.rule.options.messages = options.messages
                        data.rule.options.error = options.error
                    }

                    schema.validate(data.value, data.rule.options || options, (errs) => {
                        const finalErrors = []

                        if (errors && errors.length) {
                            finalErrors.push.apply(finalErrors, errors)
                        }

                        if (errs && errs.length) {
                            finalErrors.push.apply(finalErrors, errs)
                        }

                        doIt(finalErrors.length ? finalErrors : null)
                    })
                }
            }

            let res

            if (rule.asyncValidator) {
                res = rule.asyncValidator(rule, data.value, cb, data.source, options)
            } else if (rule.validator) {
                res = rule.validator(rule, data.value, cb, data.source, options)

                if (res === true) {
                    cb()
                } else if (res === false) {
                    cb(rule.message || `${rule.field} fails`)
                } else if (res instanceof Array) {
                    cb(res)
                } else if (res instanceof Error) {
                    cb(res.message)
                }
            }

            if (res && res.then) {
                res.then(() => cb(), (e) => cb(e))
            }
        }, (results) => {
            complete(results)
        })
    },
    getType: function getType(rule) {
        if (rule.type === undefined && rule.pattern instanceof RegExp) {
            rule.type = 'pattern'
        }

        if (typeof rule.validator !== 'function' && rule.type && !validators.hasOwnProperty(rule.type)) {
            throw new Error(format('Unknown rule type %s', rule.type))
        }

        return rule.type || 'string'
    },
    getValidationMethod: function getValidationMethod(rule) {
        if (typeof rule.validator === 'function') {
            return rule.validator
        }

        const keys = Object.keys(rule)
        const messageIndex = keys.indexOf('message')

        if (messageIndex !== -1) {
            keys.splice(messageIndex, 1)
        }

        if (keys.length === 1 && keys[0] === 'required') {
            return validators.required
        }

        return validators[this.getType(rule)] || false
    }
}

Schema.register = function register(type, validator) {
    if (typeof validator !== 'function') {
        throw new Error('Cannot register a validator by type, validator is not a function')
    }

    validators[type] = validator
}

Schema.warning = warning
Schema.messages = messages

export default Schema
// # sourceMappingURL=index.js.map
