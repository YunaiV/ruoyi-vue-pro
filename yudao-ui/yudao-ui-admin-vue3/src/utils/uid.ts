/**
 * Generate a lightweight collision-resistant ID.
 * Format: base-36 timestamp + 4-char random suffix.
 */
export function uid(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
}
