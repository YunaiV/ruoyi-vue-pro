import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  formatDate,
  formatDateTime,
  getCurrentTimezone,
  getSystemTimezone,
  isDate,
  isDayjsObject,
  setCurrentTimezone,
} from '../date';

dayjs.extend(utc);
dayjs.extend(timezone);

describe('dateUtils', () => {
  const sampleISO = '2024-10-30T12:34:56Z';
  const sampleTimestamp = Date.parse(sampleISO);

  beforeEach(() => {
    // 重置时区
    dayjs.tz.setDefault();
    setCurrentTimezone(); // 重置为系统默认
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ===============================
  // formatDate
  // ===============================
  describe('formatDate', () => {
    it('should format a valid ISO date string', () => {
      const formatted = formatDate(sampleISO, 'YYYY/MM/DD');
      expect(formatted).toMatch(/2024\/10\/30/);
    });

    it('should format a timestamp correctly', () => {
      const formatted = formatDate(sampleTimestamp);
      expect(formatted).toMatch(/2024-10-30/);
    });

    it('should format a Date object', () => {
      const formatted = formatDate(new Date(sampleISO));
      expect(formatted).toMatch(/2024-10-30/);
    });

    it('should format a dayjs object', () => {
      const formatted = formatDate(dayjs(sampleISO));
      expect(formatted).toMatch(/2024-10-30/);
    });

    it('should return original input if date is invalid', () => {
      const invalid = 'not-a-date';
      const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const formatted = formatDate(invalid);
      expect(formatted).toBe(invalid);
      expect(spy).toHaveBeenCalledOnce();
    });

    it('should apply given format', () => {
      const formatted = formatDate(sampleISO, 'YYYY-MM-DD HH:mm');
      expect(formatted).toMatch(/\d{4}-\d{2}-\d{2} \d{2}:\d{2}/);
    });
  });

  // ===============================
  // formatDateTime
  // ===============================
  describe('formatDateTime', () => {
    it('should format date into full datetime', () => {
      const result = formatDateTime(sampleISO);
      expect(result).toMatch(/2024-10-30 \d{2}:\d{2}:\d{2}/);
    });
  });

  // ===============================
  // isDate
  // ===============================
  describe('isDate', () => {
    it('should return true for Date instances', () => {
      expect(isDate(new Date())).toBe(true);
    });

    it('should return false for non-Date values', () => {
      expect(isDate('2024-10-30')).toBe(false);
      expect(isDate(null)).toBe(false);
      expect(isDate(undefined)).toBe(false);
    });
  });

  // ===============================
  // isDayjsObject
  // ===============================
  describe('isDayjsObject', () => {
    it('should return true for dayjs objects', () => {
      expect(isDayjsObject(dayjs())).toBe(true);
    });

    it('should return false for other values', () => {
      expect(isDayjsObject(new Date())).toBe(false);
      expect(isDayjsObject('string')).toBe(false);
    });
  });

  // ===============================
  // getSystemTimezone
  // ===============================
  describe('getSystemTimezone', () => {
    it('should return a valid IANA timezone string', () => {
      const tz = getSystemTimezone();
      expect(typeof tz).toBe('string');
      expect(tz).toMatch(/^[A-Z]+\/[A-Z_]+/i);
    });
  });

  // ===============================
  // setCurrentTimezone / getCurrentTimezone
  // ===============================
  describe('setCurrentTimezone & getCurrentTimezone', () => {
    it('should set and retrieve the current timezone', () => {
      setCurrentTimezone('Asia/Shanghai');
      expect(getCurrentTimezone()).toBe('Asia/Shanghai');
    });

    it('should reset to system timezone when called with no args', () => {
      const guessed = getSystemTimezone();
      setCurrentTimezone();
      expect(getCurrentTimezone()).toBe(guessed);
    });

    it('should update dayjs default timezone', () => {
      setCurrentTimezone('America/New_York');
      const d = dayjs('2024-01-01T00:00:00Z');
      // 校验时区转换生效（小时变化）
      expect(d.tz().format('HH')).not.toBe('00');
    });
  });
});
