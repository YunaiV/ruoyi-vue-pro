import { promises as fs } from 'node:fs';
import { dirname } from 'node:path';

export async function outputJSON(
  filePath: string,
  data: any,
  spaces: number = 2,
) {
  try {
    const dir = dirname(filePath);
    await fs.mkdir(dir, { recursive: true });
    const jsonData = JSON.stringify(data, null, spaces);
    await fs.writeFile(filePath, jsonData, 'utf8');
  } catch (error) {
    console.error('Error writing JSON file:', error);
    throw error;
  }
}

export async function ensureFile(filePath: string) {
  try {
    const dir = dirname(filePath);
    await fs.mkdir(dir, { recursive: true });
    await fs.writeFile(filePath, '', { flag: 'a' });
  } catch (error) {
    console.error('Error ensuring file:', error);
    throw error;
  }
}

export async function readJSON(filePath: string) {
  try {
    const data = await fs.readFile(filePath, 'utf8');
    return JSON.parse(data);
  } catch (error) {
    console.error('Error reading JSON file:', error);
    throw error;
  }
}
