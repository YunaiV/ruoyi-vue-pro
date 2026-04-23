'use strict';

var setFunctionLength = require('set-function-length');

var $defineProperty = require('es-define-property');

var callBindBasic = require('call-bind-apply-helpers');
var applyBind = require('call-bind-apply-helpers/applyBind');

module.exports = function callBind(originalFunction) {
	var func = callBindBasic(arguments);
	var adjustedLength = 1 + originalFunction.length - (arguments.length - 1);
	return setFunctionLength(
		func,
		adjustedLength > 0 ? adjustedLength : 0,
		true
	);
};

if ($defineProperty) {
	$defineProperty(module.exports, 'apply', { value: applyBind });
} else {
	module.exports.apply = applyBind;
}
