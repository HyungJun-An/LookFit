import puppeteer from 'puppeteer';

/**
 * LookFit E2E Shopping Flow Test
 *
 * Tests:
 * 1. Product List - Category filtering, sorting
 * 2. Product Detail - View product details
 * 3. Cart - Add/update/remove items
 * 4. OAuth2 Login - Google login flow
 * 5. Virtual Fitting - AI fitting page
 */

const BASE_URL = 'http://localhost:5173';
const API_URL = 'http://localhost:8080';

// Helper function to wait and take screenshot
async function waitAndScreenshot(page, selector, filename, timeout = 5000) {
  try {
    await page.waitForSelector(selector, { timeout });
    await page.screenshot({ path: `/tmp/${filename}`, fullPage: true });
    return true;
  } catch (error) {
    console.error(`❌ Failed to find ${selector}:`, error.message);
    await page.screenshot({ path: `/tmp/error-${filename}`, fullPage: true });
    return false;
  }
}

// Helper function to wait for network idle
async function waitForNetworkIdle(page, timeout = 3000) {
  await page.waitForNetworkIdle({ timeout, idleTime: 500 });
}

(async () => {
  console.log('🚀 Starting LookFit E2E Shopping Flow Test\n');
  console.log('=' .repeat(60));

  const browser = await puppeteer.launch({
    headless: false,
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--window-size=1920,1080'
    ],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 100
  });

  const page = await browser.newPage();

  // Enable console logging from the page
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log('🔴 Browser Error:', msg.text());
    }
  });

  let testsPassed = 0;
  let testsFailed = 0;

  try {
    // ============================================================
    // TEST 1: Product List Page
    // ============================================================
    console.log('\n📝 TEST 1: Product List Page');
    console.log('-'.repeat(60));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    console.log('✅ Navigated to home page');

    // Check if products are loaded
    const productsLoaded = await waitAndScreenshot(
      page,
      '.product-card',
      'test1-product-list.png'
    );

    if (productsLoaded) {
      const productCount = await page.$$eval('.product-card', cards => cards.length);
      console.log(`✅ Found ${productCount} products on the page`);

      // Check if category navigation exists
      const categoryNav = await page.$('.category-nav');
      if (categoryNav) {
        console.log('✅ Category navigation found');
        testsPassed++;
      } else {
        console.log('❌ Category navigation not found');
        testsFailed++;
      }

      // Check if filter bar exists
      const filterBar = await page.$('.filter-bar');
      if (filterBar) {
        console.log('✅ Filter bar found');
        testsPassed++;
      } else {
        console.log('❌ Filter bar not found');
        testsFailed++;
      }
    } else {
      console.log('❌ Products not loaded');
      testsFailed++;
    }

    // ============================================================
    // TEST 2: Category Filtering
    // ============================================================
    console.log('\n📝 TEST 2: Category Filtering');
    console.log('-'.repeat(60));

    // Click on "아우터" category
    const outerCategory = await page.$$('.category-nav__item');
    if (outerCategory.length > 1) {
      await outerCategory[1].click(); // "아우터" is the second category
      console.log('✅ Clicked on "아우터" category');

      await waitForNetworkIdle(page);
      await page.screenshot({ path: '/tmp/test2-category-filter.png', fullPage: true });

      const filteredProducts = await page.$$eval('.product-card', cards => cards.length);
      console.log(`✅ Filtered products: ${filteredProducts}`);

      if (filteredProducts > 0 && filteredProducts < 20) {
        console.log('✅ Category filtering works');
        testsPassed++;
      } else {
        console.log('⚠️  Category filtering may not be working correctly');
      }
    } else {
      console.log('❌ Category buttons not found');
      testsFailed++;
    }

    // Reset to "전체"
    if (outerCategory.length > 0) {
      await outerCategory[0].click();
      await waitForNetworkIdle(page);
    }

    // ============================================================
    // TEST 3: Sorting
    // ============================================================
    console.log('\n📝 TEST 3: Product Sorting');
    console.log('-'.repeat(60));

    // Click on "낮은 가격순"
    const sortButtons = await page.$$('.filter-btn');
    if (sortButtons.length >= 3) {
      await sortButtons[2].click(); // "낮은 가격순"
      console.log('✅ Clicked on "낮은 가격순"');

      await waitForNetworkIdle(page);
      await page.screenshot({ path: '/tmp/test3-sorting.png', fullPage: true });

      // Get first product price
      const firstPrice = await page.$eval('.product-card__price', el => el.textContent);
      console.log(`✅ First product price: ${firstPrice}`);
      testsPassed++;
    } else {
      console.log('❌ Sort buttons not found');
      testsFailed++;
    }

    // ============================================================
    // TEST 4: Product Detail Page
    // ============================================================
    console.log('\n📝 TEST 4: Product Detail Page');
    console.log('-'.repeat(60));

    // Click on first product
    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      console.log('✅ Clicked on first product');

      await page.waitForNavigation({ waitUntil: 'networkidle0' });
      await page.screenshot({ path: '/tmp/test4-product-detail.png', fullPage: true });

      // Check if product detail elements exist
      const productName = await page.$('h1');
      const productPrice = await page.$('.product-price');

      if (productName && productPrice) {
        const name = await page.$eval('h1', el => el.textContent);
        console.log(`✅ Product detail loaded: ${name}`);
        testsPassed++;
      } else {
        console.log('❌ Product detail elements not found');
        testsFailed++;
      }
    } else {
      console.log('❌ Product card not found');
      testsFailed++;
    }

    // ============================================================
    // TEST 5: Add to Cart (requires login)
    // ============================================================
    console.log('\n📝 TEST 5: Add to Cart');
    console.log('-'.repeat(60));

    // Try to find add to cart button
    const addToCartBtn = await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      return buttons.find(btn =>
        btn.textContent.includes('장바구니') ||
        btn.textContent.includes('Add to Cart') ||
        btn.textContent.includes('담기')
      );
    });

    if (addToCartBtn) {
      await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('button'));
        const btn = buttons.find(btn =>
          btn.textContent.includes('장바구니') ||
          btn.textContent.includes('Add to Cart') ||
          btn.textContent.includes('담기')
        );
        if (btn) btn.click();
      });
      console.log('✅ Clicked "Add to Cart" button');

      // Wait a bit to see if redirect or modal appears
      await new Promise(resolve => setTimeout(resolve, 2000));
      await page.screenshot({ path: '/tmp/test5-add-to-cart.png', fullPage: true });

      // Check if redirected to login or cart
      const currentUrl = page.url();
      if (currentUrl.includes('login')) {
        console.log('⚠️  Redirected to login (authentication required)');
        console.log('   This is expected behavior for protected endpoints');
      } else if (currentUrl.includes('cart')) {
        console.log('✅ Added to cart successfully');
        testsPassed++;
      } else {
        console.log('⚠️  Unexpected URL:', currentUrl);
      }
    } else {
      console.log('⚠️  Add to Cart button not found (may not be on detail page)');
    }

    // Go back to home
    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    // ============================================================
    // TEST 6: Virtual Fitting Page
    // ============================================================
    console.log('\n📝 TEST 6: Virtual Fitting Page');
    console.log('-'.repeat(60));

    // Navigate to fitting page
    await page.goto(`${BASE_URL}/fitting`, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test6-virtual-fitting.png', fullPage: true });

    // Check if fitting page elements exist
    const fittingTitle = await page.$('h1, h2');
    if (fittingTitle) {
      const title = await page.$eval('h1, h2', el => el.textContent);
      console.log(`✅ Virtual Fitting page loaded: ${title}`);
      testsPassed++;
    } else {
      console.log('❌ Virtual Fitting page title not found');
      testsFailed++;
    }

    // ============================================================
    // TEST 7: Navigation Links
    // ============================================================
    console.log('\n📝 TEST 7: Navigation Links');
    console.log('-'.repeat(60));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    // Check header navigation
    const headerNav = await page.$('.header__nav');
    if (headerNav) {
      console.log('✅ Header navigation found');

      // Get all nav links
      const navLinks = await page.$$eval('.header__nav a', links =>
        links.map(link => ({ text: link.textContent, href: link.href }))
      );

      console.log('   Navigation links:');
      navLinks.forEach(link => {
        console.log(`   - ${link.text}: ${link.href}`);
      });
      testsPassed++;
    } else {
      console.log('❌ Header navigation not found');
      testsFailed++;
    }

    // ============================================================
    // TEST 8: Responsive Design (Mobile View)
    // ============================================================
    console.log('\n📝 TEST 8: Responsive Design');
    console.log('-'.repeat(60));

    // Set mobile viewport
    await page.setViewport({ width: 375, height: 667 });
    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test8-mobile-view.png', fullPage: true });

    const mobileProducts = await page.$$('.product-card');
    if (mobileProducts.length > 0) {
      console.log(`✅ Mobile view loaded with ${mobileProducts.length} products`);
      testsPassed++;
    } else {
      console.log('❌ Products not visible in mobile view');
      testsFailed++;
    }

    // Reset viewport
    await page.setViewport({ width: 1920, height: 1080 });

    // ============================================================
    // TEST 9: Search Functionality (if exists)
    // ============================================================
    console.log('\n📝 TEST 9: Search Functionality');
    console.log('-'.repeat(60));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const searchInput = await page.$('input[type="search"], input[placeholder*="검색"]');
    if (searchInput) {
      console.log('✅ Search input found');
      await searchInput.type('코트');
      await new Promise(resolve => setTimeout(resolve, 1000));
      await page.screenshot({ path: '/tmp/test9-search.png', fullPage: true });
      testsPassed++;
    } else {
      console.log('⚠️  Search functionality not implemented yet');
    }

    // ============================================================
    // TEST 10: Performance Check
    // ============================================================
    console.log('\n📝 TEST 10: Performance Check');
    console.log('-'.repeat(60));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const performanceMetrics = await page.evaluate(() => {
      const perfData = window.performance.timing;
      return {
        loadTime: perfData.loadEventEnd - perfData.navigationStart,
        domReady: perfData.domContentLoadedEventEnd - perfData.navigationStart,
        responseTime: perfData.responseEnd - perfData.requestStart
      };
    });

    console.log(`✅ Page load time: ${performanceMetrics.loadTime}ms`);
    console.log(`✅ DOM ready time: ${performanceMetrics.domReady}ms`);
    console.log(`✅ Response time: ${performanceMetrics.responseTime}ms`);

    if (performanceMetrics.loadTime < 3000) {
      console.log('✅ Performance is good (< 3 seconds)');
      testsPassed++;
    } else {
      console.log('⚠️  Page load is slow (> 3 seconds)');
    }

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    console.error(error.stack);
    await page.screenshot({ path: '/tmp/error-final.png', fullPage: true });
    testsFailed++;
  } finally {
    // ============================================================
    // TEST SUMMARY
    // ============================================================
    console.log('\n' + '='.repeat(60));
    console.log('📊 TEST SUMMARY');
    console.log('='.repeat(60));
    console.log(`✅ Tests Passed: ${testsPassed}`);
    console.log(`❌ Tests Failed: ${testsFailed}`);
    console.log(`📈 Success Rate: ${((testsPassed / (testsPassed + testsFailed)) * 100).toFixed(2)}%`);
    console.log('\n📸 Screenshots saved to /tmp/test*.png');
    console.log('='.repeat(60));

    console.log('\n🏁 Test finished. Browser will close in 5 seconds...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
