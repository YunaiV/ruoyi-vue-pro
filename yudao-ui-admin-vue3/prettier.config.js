module.exports = {
  printWidth: 100, // 每行代码长度（默认80）
  tabWidth: 2, // 每个tab相当于多少个空格（默认2）ab进行缩进（默认false）
  useTabs: false, // 是否使用tab
  semi: false, // 声明结尾使用分号(默认true)
  vueIndentScriptAndStyle: false,
  singleQuote: true, // 使用单引号（默认false）
  quoteProps: 'as-needed',
  bracketSpacing: true, // 对象字面量的大括号间使用空格（默认true）
  trailingComma: 'none', // 多行使用拖尾逗号（默认none）
  jsxSingleQuote: false,
  // 箭头函数参数括号 默认avoid 可选 avoid| always
  // avoid 能省略括号的时候就省略 例如x => x
  // always 总是有括号
  arrowParens: 'always',
  insertPragma: false,
  requirePragma: false,
  proseWrap: 'never',
  htmlWhitespaceSensitivity: 'strict',
  endOfLine: 'auto',
  rangeStart: 0
}
