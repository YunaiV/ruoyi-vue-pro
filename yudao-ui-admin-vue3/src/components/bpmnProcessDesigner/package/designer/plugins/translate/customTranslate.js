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
    replacements = replacements || {}
    // Translate
    template = translations[template] || template

    // Replace
    return template.replace(/{([^}]+)}/g, function (_, key) {
      let str = replacements[key]
      if (
        translations[replacements[key]] !== null &&
        translations[replacements[key]] !== undefined
      ) {
        // eslint-disable-next-line no-mixed-spaces-and-tabs
        str = translations[replacements[key]]
        // eslint-disable-next-line no-mixed-spaces-and-tabs
      }
      return str || '{' + key + '}'
    })
  }
}
