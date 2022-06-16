import { Browser, chromium, FullConfig, request } from '@playwright/test';
import * as fs from 'fs';
import { defaultLogin } from './constants';
import { getStorageStatePath, LoginCredentials } from './util';

async function saveLogin(
    browser: Browser,
    baseURL: string,
    credentials: LoginCredentials,
): Promise<void> {
    const storageStatePath = getStorageStatePath(credentials);
    if (await fileExistsAndIsNoOlderThan(storageStatePath, 600)) {
        return;
    }
    const page = await browser.newPage();
    await page.goto(baseURL);
    await page.locator('input[name="username"]').fill(credentials.username);
    await page.locator('input[type="password"]').fill(credentials.password);
    await Promise.all([
        page.locator('input[type="password"]').press('Enter'),
        page.waitForNavigation(),
    ]);
    const storageState = await page.context().storageState({ path: storageStatePath });
    const context = await request.newContext({ baseURL, storageState });
    await context.put('./rest/iam/v1/people/-home-/-me-/preferences', {
        data: { language: 'en' },
    });
}

async function globalSetup(config: FullConfig) {
    const browser = await chromium.launch();
    const baseURL = config.projects[0].use.baseURL;
    // await saveLogin(browser, baseURL, 'admin', 'admin');
    await saveLogin(browser, baseURL, defaultLogin);
    await browser.close();
}

async function fileExistsAndIsNoOlderThan(path: string, seconds: number): Promise<boolean> {
    try {
        const stat = await fs.promises.lstat(path);
        const diffMs = new Date().getTime() - stat.ctime.getTime();
        return diffMs <= seconds * 1000;
    } catch (e) {
        if (e.code === 'ENOENT') {
            return false;
        } else {
            throw e;
        }
    }
}

export default globalSetup;
