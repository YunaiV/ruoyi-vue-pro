(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[24],{

/***/ "./src/api/bpm/form.js":
/*!*****************************!*\
  !*** ./src/api/bpm/form.js ***!
  \*****************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.createForm = createForm;\nexports.deleteForm = deleteForm;\nexports.getForm = getForm;\nexports.getFormPage = getFormPage;\nexports.getSimpleForms = getSimpleForms;\nexports.updateForm = updateForm;\n\nvar _request = _interopRequireDefault(__webpack_require__(/*! @/utils/request */ \"./src/utils/request.js\"));\n\n// 创建工作流的表单定义\nfunction createForm(data) {\n  return (0, _request.default)({\n    url: '/bpm/form/create',\n    method: 'post',\n    data: data\n  });\n} // 更新工作流的表单定义\n\n\nfunction updateForm(data) {\n  return (0, _request.default)({\n    url: '/bpm/form/update',\n    method: 'put',\n    data: data\n  });\n} // 删除工作流的表单定义\n\n\nfunction deleteForm(id) {\n  return (0, _request.default)({\n    url: '/bpm/form/delete?id=' + id,\n    method: 'delete'\n  });\n} // 获得工作流的表单定义\n\n\nfunction getForm(id) {\n  return (0, _request.default)({\n    url: '/bpm/form/get?id=' + id,\n    method: 'get'\n  });\n} // 获得工作流的表单定义分页\n\n\nfunction getFormPage(query) {\n  return (0, _request.default)({\n    url: '/bpm/form/page',\n    method: 'get',\n    params: query\n  });\n} // 获得动态表单的精简列表\n\n\nfunction getSimpleForms() {\n  return (0, _request.default)({\n    url: '/bpm/form/list-all-simple',\n    method: 'get'\n  });\n}\n\n//# sourceURL=webpack:///./src/api/bpm/form.js?");

/***/ }),

/***/ "./src/api/bpm/processInstance.js":
/*!****************************************!*\
  !*** ./src/api/bpm/processInstance.js ***!
  \****************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.cancelProcessInstance = cancelProcessInstance;\nexports.createProcessInstance = createProcessInstance;\nexports.getMyProcessInstancePage = getMyProcessInstancePage;\nexports.getProcessInstance = getProcessInstance;\n\nvar _request = _interopRequireDefault(__webpack_require__(/*! @/utils/request */ \"./src/utils/request.js\"));\n\nfunction getMyProcessInstancePage(query) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/my-page',\n    method: 'get',\n    params: query\n  });\n}\n\nfunction createProcessInstance(data) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/create',\n    method: 'POST',\n    data: data\n  });\n}\n\nfunction cancelProcessInstance(id, reason) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/cancel',\n    method: 'DELETE',\n    data: {\n      id: id,\n      reason: reason\n    }\n  });\n}\n\nfunction getProcessInstance(id) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/get?id=' + id,\n    method: 'get'\n  });\n}\n\n//# sourceURL=webpack:///./src/api/bpm/processInstance.js?");

/***/ })

}]);