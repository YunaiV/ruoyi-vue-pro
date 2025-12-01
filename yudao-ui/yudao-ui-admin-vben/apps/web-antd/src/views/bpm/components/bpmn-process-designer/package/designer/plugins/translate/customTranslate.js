// import translations from "./zh";
//
// export default function customTranslate(template, replacements) {
//   replacements = replacements || {};
//
//   // Translate
//   template = translations[template] || template;
//
//   // Replace
//   return template.replace(/{([^}]+)}/g, function(_, key) {
//     let str = replacements[key];
//     if (
//       translations[replacements[key]] !== null &&
//       translations[replacements[key]] !== "undefined"
//     ) {
//       // eslint-disable-next-line no-mixed-spaces-and-tabs
//       str = translations[replacements[key]];
//       // eslint-disable-next-line no-mixed-spaces-and-tabs
//     }
//     return str || "{" + key + "}";
//   });
// }

export default function customTranslate(translations) {
  return function (template, replacements) {
    replacements = replacements || {};
    // 将模板和翻译字典的键统一转换为小写进行匹配
    const lowerTemplate = template.toLowerCase();
    const translation = Object.keys(translations).find(
      (key) => key.toLowerCase() === lowerTemplate,
    );

    // 如果找到匹配的翻译，使用翻译后的模板
    if (translation) {
      template = translations[translation];
    }

    // 替换模板中的占位符
    return template.replaceAll(/\{([^}]+)\}/g, (_, key) => {
      // 如果替换值存在，返回替换值；否则返回原始占位符
      return replacements[key] === undefined ? `{${key}}` : replacements[key];
    });
  };
}
