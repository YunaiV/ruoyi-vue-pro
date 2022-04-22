(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[0],{

/***/ "./node_modules/@vue/babel-helper-vue-jsx-merge-props/dist/helper.js":
/*!***************************************************************************!*\
  !*** ./node_modules/@vue/babel-helper-vue-jsx-merge-props/dist/helper.js ***!
  \***************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("function _extends(){return _extends=Object.assign||function(a){for(var b,c=1;c<arguments.length;c++)for(var d in b=arguments[c],b)Object.prototype.hasOwnProperty.call(b,d)&&(a[d]=b[d]);return a},_extends.apply(this,arguments)}var normalMerge=[\"attrs\",\"props\",\"domProps\"],toArrayMerge=[\"class\",\"style\",\"directives\"],functionalMerge=[\"on\",\"nativeOn\"],mergeJsxProps=function(a){return a.reduce(function(c,a){for(var b in a)if(!c[b])c[b]=a[b];else if(-1!==normalMerge.indexOf(b))c[b]=_extends({},c[b],a[b]);else if(-1!==toArrayMerge.indexOf(b)){var d=c[b]instanceof Array?c[b]:[c[b]],e=a[b]instanceof Array?a[b]:[a[b]];c[b]=d.concat(e)}else if(-1!==functionalMerge.indexOf(b)){for(var f in a[b])if(c[b][f]){var g=c[b][f]instanceof Array?c[b][f]:[c[b][f]],h=a[b][f]instanceof Array?a[b][f]:[a[b][f]];c[b][f]=g.concat(h)}else c[b][f]=a[b][f];}else if(\"hook\"==b)for(var i in a[b])c[b][i]=c[b][i]?mergeFn(c[b][i],a[b][i]):a[b][i];else c[b]=a[b];return c},{})},mergeFn=function(a,b){return function(){a&&a.apply(this,arguments),b&&b.apply(this,arguments)}};module.exports=mergeJsxProps;\n\n\n//# sourceURL=webpack:///./node_modules/@vue/babel-helper-vue-jsx-merge-props/dist/helper.js?");

/***/ }),

/***/ "./node_modules/cache-loader/dist/cjs.js?!./node_modules/babel-loader/lib/index.js!./node_modules/cache-loader/dist/cjs.js?!./node_modules/vue-loader/lib/index.js?!./src/components/parser/Parser.vue?vue&type=script&lang=js&":
/*!**********************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/cache-loader/dist/cjs.js??ref--12-0!./node_modules/babel-loader/lib!./node_modules/cache-loader/dist/cjs.js??ref--0-0!./node_modules/vue-loader/lib??vue-loader-options!./src/components/parser/Parser.vue?vue&type=script&lang=js& ***!
  \**********************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.default = void 0;\n\n__webpack_require__(/*! core-js/modules/es.array.map.js */ \"./node_modules/core-js/modules/es.array.map.js\");\n\n__webpack_require__(/*! core-js/modules/es.error.cause.js */ \"./node_modules/core-js/modules/es.error.cause.js\");\n\n__webpack_require__(/*! core-js/modules/es.object.to-string.js */ \"./node_modules/core-js/modules/es.object.to-string.js\");\n\n__webpack_require__(/*! core-js/modules/web.dom-collections.for-each.js */ \"./node_modules/core-js/modules/web.dom-collections.for-each.js\");\n\n__webpack_require__(/*! core-js/modules/es.object.keys.js */ \"./node_modules/core-js/modules/es.object.keys.js\");\n\nvar _defineProperty2 = _interopRequireDefault(__webpack_require__(/*! ./node_modules/@babel/runtime/helpers/defineProperty.js */ \"./node_modules/@babel/runtime/helpers/defineProperty.js\"));\n\nvar _babelHelperVueJsxMergeProps = _interopRequireDefault(__webpack_require__(/*! @vue/babel-helper-vue-jsx-merge-props */ \"./node_modules/@vue/babel-helper-vue-jsx-merge-props/dist/helper.js\"));\n\nvar _index = __webpack_require__(/*! @/utils/index */ \"./src/utils/index.js\");\n\nvar _render = _interopRequireDefault(__webpack_require__(/*! @/components/render/render.js */ \"./src/components/render/render.js\"));\n\nvar ruleTrigger = {\n  'el-input': 'blur',\n  'el-input-number': 'blur',\n  'el-select': 'change',\n  'el-radio-group': 'change',\n  'el-checkbox-group': 'change',\n  'el-cascader': 'change',\n  'el-time-picker': 'change',\n  'el-date-picker': 'change',\n  'el-rate': 'change'\n};\nvar layouts = {\n  colFormItem: function colFormItem(h, scheme) {\n    var config = scheme.__config__;\n    var listeners = buildListeners.call(this, scheme);\n    var labelWidth = config.labelWidth ? \"\".concat(config.labelWidth, \"px\") : null;\n    if (config.showLabel === false) labelWidth = '0';\n    return h(\"el-col\", {\n      \"attrs\": {\n        \"span\": config.span\n      }\n    }, [h(\"el-form-item\", {\n      \"attrs\": {\n        \"label-width\": labelWidth,\n        \"prop\": scheme.__vModel__,\n        \"label\": config.showLabel ? config.label : ''\n      }\n    }, [h(_render.default, (0, _babelHelperVueJsxMergeProps.default)([{\n      \"attrs\": {\n        \"conf\": scheme\n      }\n    }, {\n      \"on\": listeners\n    }]))])]);\n  },\n  rowFormItem: function rowFormItem(h, scheme) {\n    var child = renderChildren.apply(this, arguments);\n\n    if (scheme.type === 'flex') {\n      child = h(\"el-row\", {\n        \"attrs\": {\n          \"type\": scheme.type,\n          \"justify\": scheme.justify,\n          \"align\": scheme.align\n        }\n      }, [child]);\n    }\n\n    return h(\"el-col\", {\n      \"attrs\": {\n        \"span\": scheme.span\n      }\n    }, [h(\"el-row\", {\n      \"attrs\": {\n        \"gutter\": scheme.gutter\n      }\n    }, [child])]);\n  }\n};\n\nfunction renderFrom(h) {\n  var formConfCopy = this.formConfCopy;\n  return h(\"el-row\", {\n    \"attrs\": {\n      \"gutter\": formConfCopy.gutter\n    }\n  }, [h(\"el-form\", (0, _babelHelperVueJsxMergeProps.default)([{\n    \"attrs\": {\n      \"size\": formConfCopy.size,\n      \"label-position\": formConfCopy.labelPosition,\n      \"disabled\": formConfCopy.disabled,\n      \"label-width\": \"\".concat(formConfCopy.labelWidth, \"px\")\n    },\n    \"ref\": formConfCopy.formRef\n  }, {\n    \"props\": {\n      model: this[formConfCopy.formModel]\n    }\n  }, {\n    \"attrs\": {\n      \"rules\": this[formConfCopy.formRules]\n    }\n  }]), [renderFormItem.call(this, h, formConfCopy.fields), formConfCopy.formBtns && formBtns.call(this, h)])]);\n}\n\nfunction formBtns(h) {\n  return h(\"el-col\", [h(\"el-form-item\", {\n    \"attrs\": {\n      \"size\": \"large\"\n    }\n  }, [h(\"el-button\", {\n    \"attrs\": {\n      \"type\": \"primary\"\n    },\n    \"on\": {\n      \"click\": this.submitForm\n    }\n  }, [\"\\u63D0\\u4EA4\"]), h(\"el-button\", {\n    \"on\": {\n      \"click\": this.resetForm\n    }\n  }, [\"\\u91CD\\u7F6E\"])])]);\n}\n\nfunction renderFormItem(h, elementList) {\n  var _this = this;\n\n  return elementList.map(function (scheme) {\n    var config = scheme.__config__;\n    var layout = layouts[config.layout];\n\n    if (layout) {\n      return layout.call(_this, h, scheme);\n    }\n\n    throw new Error(\"\\u6CA1\\u6709\\u4E0E\".concat(config.layout, \"\\u5339\\u914D\\u7684layout\"));\n  });\n}\n\nfunction renderChildren(h, scheme) {\n  var config = scheme.__config__;\n  if (!Array.isArray(config.children)) return null;\n  return renderFormItem.call(this, h, config.children);\n}\n\nfunction setValue(event, config, scheme) {\n  this.$set(config, 'defaultValue', event);\n  this.$set(this[this.formConf.formModel], scheme.__vModel__, event);\n}\n\nfunction buildListeners(scheme) {\n  var _this2 = this;\n\n  var config = scheme.__config__;\n  var methods = this.formConf.__methods__ || {};\n  var listeners = {}; // 给__methods__中的方法绑定this和event\n\n  Object.keys(methods).forEach(function (key) {\n    listeners[key] = function (event) {\n      return methods[key].call(_this2, event);\n    };\n  }); // 响应 render.js 中的 vModel $emit('input', val)\n\n  listeners.input = function (event) {\n    return setValue.call(_this2, event, config, scheme);\n  };\n\n  return listeners;\n}\n\nvar _default = {\n  components: {\n    render: _render.default\n  },\n  props: {\n    formConf: {\n      type: Object,\n      required: true\n    }\n  },\n  data: function data() {\n    var _data;\n\n    var data = (_data = {\n      formConfCopy: (0, _index.deepClone)(this.formConf)\n    }, (0, _defineProperty2.default)(_data, this.formConf.formModel, {}), (0, _defineProperty2.default)(_data, this.formConf.formRules, {}), _data);\n    this.initFormData(data.formConfCopy.fields, data[this.formConf.formModel]);\n    this.buildRules(data.formConfCopy.fields, data[this.formConf.formRules]);\n    return data;\n  },\n  methods: {\n    initFormData: function initFormData(componentList, formData) {\n      var _this3 = this;\n\n      componentList.forEach(function (cur) {\n        var config = cur.__config__;\n        if (cur.__vModel__) formData[cur.__vModel__] = config.defaultValue;\n        if (config.children) _this3.initFormData(config.children, formData);\n      });\n    },\n    buildRules: function buildRules(componentList, rules) {\n      var _this4 = this;\n\n      componentList.forEach(function (cur) {\n        var config = cur.__config__;\n\n        if (Array.isArray(config.regList)) {\n          if (config.required) {\n            var required = {\n              required: config.required,\n              message: cur.placeholder\n            };\n\n            if (Array.isArray(config.defaultValue)) {\n              required.type = 'array';\n              required.message = \"\\u8BF7\\u81F3\\u5C11\\u9009\\u62E9\\u4E00\\u4E2A\".concat(config.label);\n            }\n\n            required.message === undefined && (required.message = \"\".concat(config.label, \"\\u4E0D\\u80FD\\u4E3A\\u7A7A\"));\n            config.regList.push(required);\n          }\n\n          rules[cur.__vModel__] = config.regList.map(function (item) {\n            item.pattern && (item.pattern = eval(item.pattern));\n            item.trigger = ruleTrigger && ruleTrigger[config.tag];\n            return item;\n          });\n        }\n\n        if (config.children) _this4.buildRules(config.children, rules);\n      });\n    },\n    resetForm: function resetForm() {\n      this.formConfCopy = (0, _index.deepClone)(this.formConf);\n      this.$refs[this.formConf.formRef].resetFields();\n    },\n    submitForm: function submitForm() {\n      var _this5 = this;\n\n      this.$refs[this.formConf.formRef].validate(function (valid) {\n        if (!valid) return false; // 触发 submit 事件\n        // update by 芋道源码\n        // this.$emit('submit', this[this.formConf.formModel])\n\n        _this5.$emit('submit', {\n          conf: _this5.formConfCopy,\n          values: _this5[_this5.formConf.formModel]\n        });\n\n        return true;\n      });\n    }\n  },\n  render: function render(h) {\n    return renderFrom.call(this, h);\n  }\n};\nexports.default = _default;\n\n//# sourceURL=webpack:///./src/components/parser/Parser.vue?./node_modules/cache-loader/dist/cjs.js??ref--12-0!./node_modules/babel-loader/lib!./node_modules/cache-loader/dist/cjs.js??ref--0-0!./node_modules/vue-loader/lib??vue-loader-options");

/***/ }),

/***/ "./src/api/bpm/definition.js":
/*!***********************************!*\
  !*** ./src/api/bpm/definition.js ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.getProcessDefinitionBpmnXML = getProcessDefinitionBpmnXML;\nexports.getProcessDefinitionList = getProcessDefinitionList;\nexports.getProcessDefinitionPage = getProcessDefinitionPage;\n\nvar _request = _interopRequireDefault(__webpack_require__(/*! @/utils/request */ \"./src/utils/request.js\"));\n\nfunction getProcessDefinitionPage(query) {\n  return (0, _request.default)({\n    url: '/bpm/process-definition/page',\n    method: 'get',\n    params: query\n  });\n}\n\nfunction getProcessDefinitionList(query) {\n  return (0, _request.default)({\n    url: '/bpm/process-definition/list',\n    method: 'get',\n    params: query\n  });\n}\n\nfunction getProcessDefinitionBpmnXML(id) {\n  return (0, _request.default)({\n    url: '/bpm/process-definition/get-bpmn-xml?id=' + id,\n    method: 'get'\n  });\n}\n\n//# sourceURL=webpack:///./src/api/bpm/definition.js?");

/***/ }),

/***/ "./src/components/parser/Parser.vue":
/*!******************************************!*\
  !*** ./src/components/parser/Parser.vue ***!
  \******************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var _Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./Parser.vue?vue&type=script&lang=js& */ \"./src/components/parser/Parser.vue?vue&type=script&lang=js&\");\n/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__) if([\"default\"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));\n/* harmony import */ var _node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../../node_modules/vue-loader/lib/runtime/componentNormalizer.js */ \"./node_modules/vue-loader/lib/runtime/componentNormalizer.js\");\nvar render, staticRenderFns\n\n\n\n\n/* normalize component */\n\nvar component = Object(_node_modules_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_1__[\"default\"])(\n  _Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__[\"default\"],\n  render,\n  staticRenderFns,\n  false,\n  null,\n  null,\n  null\n  \n)\n\n/* hot reload */\nif (false) { var api; }\ncomponent.options.__file = \"src/components/parser/Parser.vue\"\n/* harmony default export */ __webpack_exports__[\"default\"] = (component.exports);\n\n//# sourceURL=webpack:///./src/components/parser/Parser.vue?");

/***/ }),

/***/ "./src/components/parser/Parser.vue?vue&type=script&lang=js&":
/*!*******************************************************************!*\
  !*** ./src/components/parser/Parser.vue?vue&type=script&lang=js& ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var _node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../../node_modules/cache-loader/dist/cjs.js??ref--12-0!../../../node_modules/babel-loader/lib!../../../node_modules/cache-loader/dist/cjs.js??ref--0-0!../../../node_modules/vue-loader/lib??vue-loader-options!./Parser.vue?vue&type=script&lang=js& */ \"./node_modules/cache-loader/dist/cjs.js?!./node_modules/babel-loader/lib/index.js!./node_modules/cache-loader/dist/cjs.js?!./node_modules/vue-loader/lib/index.js?!./src/components/parser/Parser.vue?vue&type=script&lang=js&\");\n/* harmony import */ var _node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__);\n/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__) if([\"default\"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));\n /* harmony default export */ __webpack_exports__[\"default\"] = (_node_modules_cache_loader_dist_cjs_js_ref_12_0_node_modules_babel_loader_lib_index_js_node_modules_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_lib_index_js_vue_loader_options_Parser_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default.a); \n\n//# sourceURL=webpack:///./src/components/parser/Parser.vue?");

/***/ }),

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