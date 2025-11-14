export type {
  AlertProps,
  BeforeCloseScope,
  IconType,
  PromptProps,
} from './alert';
export { useAlertContext } from './alert';
export { default as Alert } from './alert.vue';
export {
  vbenAlert as alert,
  clearAllAlerts,
  vbenConfirm as confirm,
  vbenPrompt as prompt,
} from './AlertBuilder';
