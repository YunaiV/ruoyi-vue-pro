/** 手机号正则表达式（中国） */
const MOBILE_REGEX = /(?:0|86|\+86)?1[3-9]\d{9}/;
/** 身份证号正则表达式 */
const ID_CARD_REGEX = /^\d{15}|\d{18}$/;
/** 邮箱正则表达式 */
const EMAIL_REGEX = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
/** 密码正则表达式 以字母开头，长度在6~18之间，只能包含字母、数字和下划线 */
const PASSWORD_REGEX = /^[a-z]\w{5,17}$/i;
/** 强密码 必须包含大小写字母和数字的组合，不能使用特殊字符，长度在8-10之间  */
const STRONG_PASSWORD_REGEX = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$/;

export {
  EMAIL_REGEX,
  ID_CARD_REGEX,
  MOBILE_REGEX,
  PASSWORD_REGEX,
  STRONG_PASSWORD_REGEX,
};
