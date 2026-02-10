/**
 * Quick Mobile Responsive Test
 * - Fast headless test with key checks
 */

const puppeteer = require('puppeteer');

const FRONTEND_URL = 'http://localhost:5173';

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function quickTest() {
  console.log('🚀 Quick Mobile Responsive Test\n');

  const browser = await puppeteer.launch({
    headless: true,
    args: ['--no-sandbox']
  });

  try {
    const page = await browser.newPage();

    // Test 1: Mobile Hamburger Menu
    console.log('📱 Test 1: Mobile Hamburger Menu');
    await page.setViewport({ width: 390, height: 844, isMobile: true });
    await page.goto(FRONTEND_URL, { waitUntil: 'networkidle0', timeout: 10000 });

    const hamburgerExists = await page.$('.header__mobile-toggle');
    console.log(hamburgerExists ? '✅ Hamburger button exists' : '❌ Hamburger button missing');

    const hamburgerVisible = await page.evaluate(() => {
      const btn = document.querySelector('.header__mobile-toggle');
      return btn && window.getComputedStyle(btn).display !== 'none';
    });
    console.log(hamburgerVisible ? '✅ Hamburger visible on mobile' : '❌ Hamburger not visible');

    const buttonSize = await page.evaluate(() => {
      const btn = document.querySelector('.header__mobile-toggle');
      if (!btn) return null;
      const rect = btn.getBoundingClientRect();
      return { width: Math.round(rect.width), height: Math.round(rect.height) };
    });
    if (buttonSize) {
      const sizeOk = buttonSize.width >= 44 && buttonSize.height >= 44;
      console.log(`${sizeOk ? '✅' : '❌'} Touch target: ${buttonSize.width}x${buttonSize.height}px`);
    }

    // Click hamburger
    await page.click('.header__mobile-toggle');
    await sleep(500);

    const menuOpen = await page.evaluate(() => {
      return document.querySelector('.header__nav')?.classList.contains('active');
    });
    console.log(menuOpen ? '✅ Menu opens on click' : '❌ Menu did not open');

    console.log('');

    // Test 2: Product Grid Responsive
    console.log('📱 Test 2: Product Grid Layout');

    // Mobile (390px)
    let gridColumns = await page.evaluate(() => {
      const grid = document.querySelector('.product-grid');
      if (!grid) return 0;
      return window.getComputedStyle(grid).gridTemplateColumns.split(' ').length;
    });
    console.log(`Mobile (390px): ${gridColumns} columns ${gridColumns === 2 ? '✅' : '⚠️'}`);

    // Tablet (768px)
    await page.setViewport({ width: 768, height: 1024 });
    await sleep(500);
    gridColumns = await page.evaluate(() => {
      const grid = document.querySelector('.product-grid');
      if (!grid) return 0;
      return window.getComputedStyle(grid).gridTemplateColumns.split(' ').length;
    });
    console.log(`Tablet (768px): ${gridColumns} columns ${gridColumns >= 2 ? '✅' : '⚠️'}`);

    // Desktop (1280px)
    await page.setViewport({ width: 1280, height: 800 });
    await sleep(500);
    gridColumns = await page.evaluate(() => {
      const grid = document.querySelector('.product-grid');
      if (!grid) return 0;
      return window.getComputedStyle(grid).gridTemplateColumns.split(' ').length;
    });
    console.log(`Desktop (1280px): ${gridColumns} columns ${gridColumns >= 3 ? '✅' : '⚠️'}`);

    console.log('');

    // Test 3: No Horizontal Scroll
    console.log('📱 Test 3: Horizontal Scroll Check');

    const viewports = [
      { name: 'iPhone SE', width: 375 },
      { name: 'iPhone 12', width: 390 },
      { name: 'iPad Mini', width: 768 }
    ];

    for (const vp of viewports) {
      await page.setViewport({ width: vp.width, height: 800 });
      await page.goto(FRONTEND_URL, { waitUntil: 'networkidle0' });
      await sleep(500);

      const hasScroll = await page.evaluate(() => {
        return document.documentElement.scrollWidth > document.documentElement.clientWidth;
      });

      console.log(`${vp.name} (${vp.width}px): ${hasScroll ? '⚠️ Has horizontal scroll' : '✅ No horizontal scroll'}`);
    }

    console.log('');
    console.log('✅ Quick mobile responsive test complete!\n');

  } catch (error) {
    console.error('❌ Test error:', error.message);
    throw error;
  } finally {
    await browser.close();
  }
}

quickTest().catch(error => {
  console.error('Fatal error:', error);
  process.exit(1);
});
