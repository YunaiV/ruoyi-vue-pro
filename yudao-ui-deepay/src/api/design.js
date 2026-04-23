/**
 * Design-specific API — re-exports from unified api module.
 * Import from here in Generate.vue to keep concerns tidy.
 */
export {
  createGenerateTask,
  getTaskResult,
  selectImage,
  getQuotaInfo,
  getPricingPlans,
} from './index'
