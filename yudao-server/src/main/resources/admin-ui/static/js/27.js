(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[27],{

/***/ "./src/api/bpm/processInstance.js":
/*!****************************************!*\
  !*** ./src/api/bpm/processInstance.js ***!
  \****************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar _interopRequireDefault = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/interopRequireDefault.js */ \"./node_modules/@babel/runtime/helpers/interopRequireDefault.js\").default;\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.cancelProcessInstance = cancelProcessInstance;\nexports.createProcessInstance = createProcessInstance;\nexports.getMyProcessInstancePage = getMyProcessInstancePage;\nexports.getProcessInstance = getProcessInstance;\nvar _request = _interopRequireDefault(__webpack_require__(/*! @/utils/request */ \"./src/utils/request.js\"));\nfunction getMyProcessInstancePage(query) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/my-page',\n    method: 'get',\n    params: query\n  });\n}\nfunction createProcessInstance(data) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/create',\n    method: 'POST',\n    data: data\n  });\n}\nfunction cancelProcessInstance(id, reason) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/cancel',\n    method: 'DELETE',\n    data: {\n      id: id,\n      reason: reason\n    }\n  });\n}\nfunction getProcessInstance(id) {\n  return (0, _request.default)({\n    url: '/bpm/process-instance/get?id=' + id,\n    method: 'get'\n  });\n}\n\n//# sourceURL=webpack:///./src/api/bpm/processInstance.js?");

/***/ })

}]);