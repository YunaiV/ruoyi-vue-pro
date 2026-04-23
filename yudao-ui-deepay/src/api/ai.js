/**
 * ai.js — AI 全链路接口
 */
import http from '@/utils/request'

export function generateProduct(params)    { return http.post('/api/ai/product',   params) }
export function generateShop(params)       { return http.post('/api/ai/shop',       params) }
export function optimizeCopy(params)       { return http.post('/api/ai/optimize',   params) }
export function redesignImages(params)     { return http.post('/api/ai/redesign',   params) }
export function editImage(params)          { return http.post('/api/ai/edit',       params) }
export function scoreImages(params)        { return http.post('/api/ai/score',      params) }
export function selectBestImages(params)   { return http.post('/api/ai/select',     params) }
export function deduplicateImages(params)  { return http.post('/api/ai/deduplicate',params) }
export function deduplicateAdvanced(params){ return http.post('/api/ai/deduplicateAdvanced', params) }
export function generateCollection(params) { return http.post('/api/ai/generateCollection',  params) }
export function updateDetail(params)       { return http.post('/api/ai/updateDetail', params) }
export function recolorImage(params)       { return http.post('/api/ai/recolor',    params) }
export function refineImage(params)        { return http.post('/api/ai/refine',     params) }
export function designScore(params)        { return http.post('/api/ai/designScore',params) }
export function generateTechPack(params)   { return http.post('/api/ai/generateTechPack', params) }
export function selectRefs(params)         { return http.post('/api/ai/selectRefs', params) }
export function createStyleProfile(params) { return http.post('/api/ai/styleProfile/create', params) }
export function generateSeason(params)     { return http.post('/api/ai/generateSeason', params) }
export function processInspiration(params) { return http.post('/api/inspiration/process',  params) }
export function filterInspiration(params)  { return http.post('/api/inspiration/filter',   params) }
export function scoreInspiration(params)   { return http.post('/api/inspiration/score',    params) }
export function classifyInspiration(params){ return http.post('/api/inspiration/classify', params) }
export function saveDesign(params)         { return http.post('/api/design/save', params) }
export function getMyDesigns(params = {})  { return http.get('/api/design/my',   { params }) }
export function getInspirationImages(params = {}) { return http.get('/api/inspiration/images', { params }) }
