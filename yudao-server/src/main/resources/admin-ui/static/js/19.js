(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[19],{

/***/ "./src/components/render/render.js":
/*!*****************************************!*\
  !*** ./src/components/render/render.js ***!
  \*****************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\n\nvar _objectSpread2 = _interopRequireDefault(__webpack_require__(/*! ./node_modules/@babel/runtime/helpers/objectSpread2.js */ \"./node_modules/@babel/runtime/helpers/objectSpread2.js\"));\n\nvar _toConsumableArray2 = _interopRequireDefault(__webpack_require__(/*! ./node_modules/@babel/runtime/helpers/toConsumableArray.js */ \"./node_modules/@babel/runtime/helpers/toConsumableArray.js\"));\n\nvar _typeof2 = _interopRequireDefault(__webpack_require__(/*! ./node_modules/@babel/runtime/helpers/typeof.js */ \"./node_modules/@babel/runtime/helpers/typeof.js\"));\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.iterator.js */ \"./node_modules/core-js/modules/web.dom-collections.iterator.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\n__webpack_require__(/*! core-js/modules/es.regexp.exec.js */ \"./node_modules/core-js/modules/es.regexp.exec.js\");\n\n__webpack_require__(/*! core-js/modules/es.string.replace.js */ \"./node_modules/core-js/modules/es.string.replace.js\");\n\n__webpack_require__(/*! core-js/modules/es.object.keys.js */ \"./node_modules/core-js/modules/es.object.keys.js\");\n\n__webpack_require__(/*! core-js/modules/es.regexp.constructor.js */ \"./node_modules/core-js/modules/es.regexp.constructor.js\");\n\n__webpack_require__(/*! core-js/modules/es.regexp.dot-all.js */ \"./node_modules/core-js/modules/es.regexp.dot-all.js\");\n\n__webpack_require__(/*! core-js/modules/es.regexp.sticky.js */ \"./node_modules/core-js/modules/es.regexp.sticky.js\");\n\n__webpack_require__(/*! core-js/modules/es.regexp.to-string.js */ \"./node_modules/core-js/modules/es.regexp.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/es.array.includes.js */ \"./node_modules/core-js/modules/es.array.includes.js\");\n\n__webpack_require__(/*! core-js/modules/es.array.concat.js */ \"./node_modules/core-js/modules/es.array.concat.js\");\n\nvar _index = __webpack_require__(/*! @/utils/index */ \"./src/utils/index.js\");\n\nvar componentChild = {};\n/**\n * 将./slots中的文件挂载到对象componentChild上\n * 文件名为key，对应JSON配置中的__config__.tag\n * 文件内容为value，解析JSON配置中的__slot__\n */\n\nvar slotsFiles = __webpack_require__(\"./src/components/render/slots sync \\\\.js$\");\n\nvar keys = slotsFiles.keys() || [];\nkeys.forEach(function (key) {\n  var tag = key.replace(/^\\.\\/(.*)\\.\\w+$/, '$1');\n  var value = slotsFiles(key).default;\n  componentChild[tag] = value;\n});\n\nfunction vModel(dataObject, defaultValue) {\n  var _this = this;\n\n  dataObject.props.value = defaultValue;\n\n  dataObject.on.input = function (val) {\n    _this.$emit('input', val);\n  };\n}\n\nfunction mountSlotFiles(h, confClone, children) {\n  var childObjs = componentChild[confClone.__config__.tag];\n\n  if (childObjs) {\n    Object.keys(childObjs).forEach(function (key) {\n      var childFunc = childObjs[key];\n\n      if (confClone.__slot__ && confClone.__slot__[key]) {\n        children.push(childFunc(h, confClone, key));\n      }\n    });\n  }\n}\n\nfunction emitEvents(confClone) {\n  var _this2 = this;\n\n  ['on', 'nativeOn'].forEach(function (attr) {\n    var eventKeyList = Object.keys(confClone[attr] || {});\n    eventKeyList.forEach(function (key) {\n      var val = confClone[attr][key];\n\n      if (typeof val === 'string') {\n        confClone[attr][key] = function (event) {\n          return _this2.$emit(val, event);\n        };\n      }\n    });\n  });\n}\n\nfunction buildDataObject(confClone, dataObject) {\n  var _this3 = this;\n\n  Object.keys(confClone).forEach(function (key) {\n    var val = confClone[key];\n\n    if (key === '__vModel__') {\n      vModel.call(_this3, dataObject, confClone.__config__.defaultValue);\n    } else if (dataObject[key] !== undefined) {\n      if (dataObject[key] === null || dataObject[key] instanceof RegExp || ['boolean', 'string', 'number', 'function'].includes((0, _typeof2.default)(dataObject[key]))) {\n        dataObject[key] = val;\n      } else if (Array.isArray(dataObject[key])) {\n        dataObject[key] = [].concat((0, _toConsumableArray2.default)(dataObject[key]), (0, _toConsumableArray2.default)(val));\n      } else {\n        dataObject[key] = (0, _objectSpread2.default)((0, _objectSpread2.default)({}, dataObject[key]), val);\n      }\n    } else {\n      dataObject.attrs[key] = val;\n    }\n  }); // 清理属性\n\n  clearAttrs(dataObject);\n}\n\nfunction clearAttrs(dataObject) {\n  delete dataObject.attrs.__config__;\n  delete dataObject.attrs.__slot__;\n  delete dataObject.attrs.__methods__;\n}\n\nfunction makeDataObject() {\n  // 深入数据对象：\n  // https://cn.vuejs.org/v2/guide/render-function.html#%E6%B7%B1%E5%85%A5%E6%95%B0%E6%8D%AE%E5%AF%B9%E8%B1%A1\n  return {\n    class: {},\n    attrs: {},\n    props: {},\n    domProps: {},\n    nativeOn: {},\n    on: {},\n    style: {},\n    directives: [],\n    scopedSlots: {},\n    slot: null,\n    key: null,\n    ref: null,\n    refInFor: true\n  };\n}\n\nvar _default = {\n  props: {\n    conf: {\n      type: Object,\n      required: true\n    }\n  },\n  render: function render(h) {\n    var dataObject = makeDataObject();\n    var confClone = (0, _index.deepClone)(this.conf);\n    var children = this.$slots.default || []; // 如果slots文件夹存在与当前tag同名的文件，则执行文件中的代码\n\n    mountSlotFiles.call(this, h, confClone, children); // 将字符串类型的事件，发送为消息\n\n    emitEvents.call(this, confClone); // 将json表单配置转化为vue render可以识别的 “数据对象（dataObject）”\n\n    buildDataObject.call(this, confClone, dataObject);\n    return h(this.conf.__config__.tag, dataObject, children);\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/render.js?");

/***/ }),

/***/ "./src/components/render/slots sync \\.js$":
/*!*************************************************************!*\
  !*** ./src/components/render/slots sync nonrecursive \.js$ ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

eval("var map = {\n\t\"./el-button.js\": \"./src/components/render/slots/el-button.js\",\n\t\"./el-checkbox-group.js\": \"./src/components/render/slots/el-checkbox-group.js\",\n\t\"./el-input.js\": \"./src/components/render/slots/el-input.js\",\n\t\"./el-radio-group.js\": \"./src/components/render/slots/el-radio-group.js\",\n\t\"./el-select.js\": \"./src/components/render/slots/el-select.js\",\n\t\"./el-upload.js\": \"./src/components/render/slots/el-upload.js\"\n};\n\n\nfunction webpackContext(req) {\n\tvar id = webpackContextResolve(req);\n\treturn __webpack_require__(id);\n}\nfunction webpackContextResolve(req) {\n\tif(!__webpack_require__.o(map, req)) {\n\t\tvar e = new Error(\"Cannot find module '\" + req + \"'\");\n\t\te.code = 'MODULE_NOT_FOUND';\n\t\tthrow e;\n\t}\n\treturn map[req];\n}\nwebpackContext.keys = function webpackContextKeys() {\n\treturn Object.keys(map);\n};\nwebpackContext.resolve = webpackContextResolve;\nmodule.exports = webpackContext;\nwebpackContext.id = \"./src/components/render/slots sync \\\\.js$\";\n\n//# sourceURL=webpack:///./src/components/render/slots_sync_nonrecursive_\\.js$?");

/***/ }),

/***/ "./src/components/render/slots/el-button.js":
/*!**************************************************!*\
  !*** ./src/components/render/slots/el-button.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\nvar _default2 = {\n  default: function _default(h, conf, key) {\n    return conf.__slot__[key];\n  }\n};\nexports.default = _default2;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-button.js?");

/***/ }),

/***/ "./src/components/render/slots/el-checkbox-group.js":
/*!**********************************************************!*\
  !*** ./src/components/render/slots/el-checkbox-group.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\nvar _default = {\n  options: function options(h, conf, key) {\n    var list = [];\n\n    conf.__slot__.options.forEach(function (item) {\n      if (conf.__config__.optionType === 'button') {\n        list.push(h(\"el-checkbox-button\", {\n          \"attrs\": {\n            \"label\": item.value\n          }\n        }, [item.label]));\n      } else {\n        list.push(h(\"el-checkbox\", {\n          \"attrs\": {\n            \"label\": item.value,\n            \"border\": conf.border\n          }\n        }, [item.label]));\n      }\n    });\n\n    return list;\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-checkbox-group.js?");

/***/ }),

/***/ "./src/components/render/slots/el-input.js":
/*!*************************************************!*\
  !*** ./src/components/render/slots/el-input.js ***!
  \*************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\nvar _default = {\n  prepend: function prepend(h, conf, key) {\n    return h(\"template\", {\n      \"slot\": \"prepend\"\n    }, [conf.__slot__[key]]);\n  },\n  append: function append(h, conf, key) {\n    return h(\"template\", {\n      \"slot\": \"append\"\n    }, [conf.__slot__[key]]);\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-input.js?");

/***/ }),

/***/ "./src/components/render/slots/el-radio-group.js":
/*!*******************************************************!*\
  !*** ./src/components/render/slots/el-radio-group.js ***!
  \*******************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\nvar _default = {\n  options: function options(h, conf, key) {\n    var list = [];\n\n    conf.__slot__.options.forEach(function (item) {\n      if (conf.__config__.optionType === 'button') {\n        list.push(h(\"el-radio-button\", {\n          \"attrs\": {\n            \"label\": item.value\n          }\n        }, [item.label]));\n      } else {\n        list.push(h(\"el-radio\", {\n          \"attrs\": {\n            \"label\": item.value,\n            \"border\": conf.border\n          }\n        }, [item.label]));\n      }\n    });\n\n    return list;\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-radio-group.js?");

/***/ }),

/***/ "./src/components/render/slots/el-select.js":
/*!**************************************************!*\
  !*** ./src/components/render/slots/el-select.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\nvar _default = {\n  options: function options(h, conf, key) {\n    var list = [];\n\n    conf.__slot__.options.forEach(function (item) {\n      list.push(h(\"el-option\", {\n        \"attrs\": {\n          \"label\": item.label,\n          \"value\": item.value,\n          \"disabled\": item.disabled\n        }\n      }));\n    });\n\n    return list;\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-select.js?");

/***/ }),

/***/ "./src/components/render/slots/el-upload.js":
/*!**************************************************!*\
  !*** ./src/components/render/slots/el-upload.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\nvar _default = {\n  'list-type': function listType(h, conf, key) {\n    var list = [];\n    var config = conf.__config__;\n\n    if (conf['list-type'] === 'picture-card') {\n      list.push(h(\"i\", {\n        \"class\": \"el-icon-plus\"\n      }));\n    } else {\n      list.push(h(\"el-button\", {\n        \"attrs\": {\n          \"size\": \"small\",\n          \"type\": \"primary\",\n          \"icon\": \"el-icon-upload\"\n        }\n      }, [config.buttonText]));\n    }\n\n    if (config.showTip) {\n      list.push(h(\"div\", {\n        \"slot\": \"tip\",\n        \"class\": \"el-upload__tip\"\n      }, [\"\\u53EA\\u80FD\\u4E0A\\u4F20\\u4E0D\\u8D85\\u8FC7 \", config.fileSize, config.sizeUnit, \" \\u7684\", conf.accept, \"\\u6587\\u4EF6\"]));\n    }\n\n    return list;\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/render/slots/el-upload.js?");

/***/ }),

/***/ "./src/utils/formGenerator.js":
/*!************************************!*\
  !*** ./src/utils/formGenerator.js ***!
  \************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.decodeFields = decodeFields;\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\n/**\n * 将服务端返回的 fields 字符串数组，解析成 JSON 数组\n *\n * @param fields JSON 字符串数组\n * @returns {*[]} JSON 数组\n */\nfunction decodeFields(fields) {\n  var drawingList = [];\n  fields.forEach(function (item) {\n    drawingList.push(JSON.parse(item));\n  });\n  return drawingList;\n}\n\n//# sourceURL=webpack:///./src/utils/formGenerator.js?");

/***/ })

}]);