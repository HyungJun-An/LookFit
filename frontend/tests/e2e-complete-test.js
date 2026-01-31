import puppeteer from 'puppeteer';

/**
 * LookFit Complete E2E Test Suite
 *
 * Comprehensive testing including ProductDetail page
 */

const BASE_URL = 'http://localhost:5173';

async function waitAndScreenshot(page, selector, filename, timeout = 5000) {
  try {
    await page.waitForSelector(selector, { timeout });
    await page.screenshot({ path: `/tmp/${filename}`, fullPage: true });
    return true;
  } catch (error) {
    console.error(`❌ Failed to find ${selector}`);
    await page.screenshot({ path: `/tmp/error-${filename}`, fullPage: true });
    return false;
  }
}

async function waitForNetworkIdle(page, timeout = 3000) {
  try {
    await page.waitForNetworkIdle({ timeout, idleTime: 500 });
  } catch (e) {
    // Ignore timeout errors
  }
}

(async () => {
  console.log('🚀 LookFit Complete E2E Test Suite\n');
  console.log('='.repeat(70));

  const browser = await puppeteer.launch({
    headless: false,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1920,1080'],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 50
  });

  const page = await browser.newPage();
  let testsPassed = 0;
  let testsFailed = 0;

  try {
    // ============================================================
    // TEST 1: Product List Page
    // ============================================================
    console.log('\n📝 TEST 1: Product List Page');
    console.log('-'.repeat(70));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const productsLoaded = await waitAndScreenshot(page, '.product-card', 'test1-products.png');
    if (productsLoaded) {
      const count = await page.$$eval('.product-card', cards => cards.length);
      console.log(`✅ Loaded ${count} products`);
      testsPassed++;
    } else {
      testsFailed++;
    }

    // ============================================================
    // TEST 2: Navigate to Product Detail
    // ============================================================
    console.log('\n📝 TEST 2: Navigate to Product Detail');
    console.log('-'.repeat(70));

    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0' });

      const hasProductName = await page.$('.product-name');
      if (hasProductName) {
        const name = await page.$eval('.product-name', el => el.textContent);
        console.log(`✅ Product Detail Page: ${name}`);
        testsPassed++;
      }
    } else {
      console.log('❌ No products found');
      testsFailed++;
    }

    await page.screenshot({ path: '/tmp/test2-product-detail.png', fullPage: true });

    // ============================================================
    // TEST 3: Image Gallery
    // ============================================================
    console.log('\n📝 TEST 3: Image Gallery');
    console.log('-'.repeat(70));

    const mainImage = await page.$('.main-image img');
    if (mainImage) {
      console.log('✅ Main image displayed');

      const thumbnails = await page.$$('.thumbnail-list img');
      console.log(`✅ Found ${thumbnails.length} thumbnails`);

      if (thumbnails.length > 1) {
        const firstSrc = await page.$eval('.main-image img', img => img.src);
        await thumbnails[1].click();
        await new Promise(resolve => setTimeout(resolve, 500));
        const newSrc = await page.$eval('.main-image img', img => img.src);

        if (firstSrc !== newSrc) {
          console.log('✅ Thumbnail click changes main image');
          testsPassed++;
        } else {
          console.log('⚠️  Thumbnail click did not change image');
        }
      }
    } else {
      console.log('❌ Main image not found');
      testsFailed++;
    }

    await page.screenshot({ path: '/tmp/test3-gallery.png', fullPage: true });

    // ============================================================
    // TEST 4: Quantity Controls
    // ============================================================
    console.log('\n📝 TEST 4: Quantity Controls');
    console.log('-'.repeat(70));

    const quantityInput = await page.$('input[type="number"]');
    if (quantityInput) {
      const initial = await page.$eval('input[type="number"]', el => el.value);
      console.log(`   Initial quantity: ${initial}`);

      // Click + button
      await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('.quantity-control button'));
        const plusBtn = buttons.find(btn => btn.textContent.includes('+'));
        if (plusBtn) plusBtn.click();
      });

      await new Promise(resolve => setTimeout(resolve, 300));
      const after = await page.$eval('input[type="number"]', el => el.value);
      console.log(`   After +: ${after}`);

      if (parseInt(after) > parseInt(initial)) {
        console.log('✅ Quantity controls work');
        testsPassed++;
      } else {
        console.log('❌ Quantity controls not working');
        testsFailed++;
      }
    }

    await page.screenshot({ path: '/tmp/test4-quantity.png', fullPage: true });

    // ============================================================
    // TEST 5: Product Information Display
    // ============================================================
    console.log('\n📝 TEST 5: Product Information Display');
    console.log('-'.repeat(70));

    const productInfo = await page.evaluate(() => {
      return {
        brand: document.querySelector('.product-brand')?.textContent,
        name: document.querySelector('.product-name')?.textContent,
        price: document.querySelector('.product-price')?.textContent,
        description: document.querySelector('.product-description')?.textContent
      };
    });

    console.log(`   Brand: ${productInfo.brand}`);
    console.log(`   Name: ${productInfo.name}`);
    console.log(`   Price: ${productInfo.price}`);
    console.log(`   Description: ${productInfo.description?.substring(0, 50)}...`);

    if (productInfo.brand && productInfo.name && productInfo.price) {
      console.log('✅ Product information displayed correctly');
      testsPassed++;
    } else {
      console.log('❌ Some product information missing');
      testsFailed++;
    }

    // ============================================================
    // TEST 6: Product Meta Data
    // ============================================================
    console.log('\n📝 TEST 6: Product Meta Data');
    console.log('-'.repeat(70));

    const metaInfo = await page.evaluate(() => {
      const items = Array.from(document.querySelectorAll('.meta-item'));
      return items.map(item => ({
        label: item.querySelector('.meta-label')?.textContent,
        value: item.querySelector('.meta-value')?.textContent
      }));
    });

    if (metaInfo.length > 0) {
      console.log(`✅ Found ${metaInfo.length} meta information items:`);
      metaInfo.forEach(info => {
        console.log(`   ${info.label}: ${info.value}`);
      });
      testsPassed++;
    } else {
      console.log('❌ No meta information found');
      testsFailed++;
    }

    await page.screenshot({ path: '/tmp/test6-meta.png', fullPage: true });

    // ============================================================
    // TEST 7: Product Features
    // ============================================================
    console.log('\n📝 TEST 7: Product Features');
    console.log('-'.repeat(70));

    const features = await page.$$('.feature-item');
    if (features.length > 0) {
      console.log(`✅ Found ${features.length} product features`);

      const featureTexts = await page.evaluate(() => {
        const items = Array.from(document.querySelectorAll('.feature-item'));
        return items.map(item =>
          item.querySelector('h4')?.textContent
        );
      });

      featureTexts.forEach(text => {
        console.log(`   - ${text}`);
      });
      testsPassed++;
    } else {
      console.log('⚠️  No product features found');
    }

    // ============================================================
    // TEST 8: Add to Cart Button (Unauthenticated)
    // ============================================================
    console.log('\n📝 TEST 8: Add to Cart (Unauthenticated)');
    console.log('-'.repeat(70));

    const hasCartButton = await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      return buttons.some(btn => btn.textContent.includes('장바구니 담기'));
    });

    if (hasCartButton) {
      console.log('✅ "장바구니 담기" button found');

      // Set up alert handler
      page.on('dialog', async dialog => {
        console.log(`   Alert: "${dialog.message()}"`);
        await dialog.accept();
      });

      // Click the button
      await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('button'));
        const btn = buttons.find(btn => btn.textContent.includes('장바구니 담기'));
        if (btn && !btn.disabled) btn.click();
      });

      await new Promise(resolve => setTimeout(resolve, 1500));

      const currentUrl = page.url();
      if (currentUrl.includes('login')) {
        console.log('✅ Redirected to login (expected for unauthenticated user)');
        testsPassed++;

        // Go back to product
        await page.goBack();
        await waitForNetworkIdle(page);
      } else {
        console.log('⚠️  No redirect to login');
      }
    } else {
      console.log('❌ Cart button not found');
      testsFailed++;
    }

    await page.screenshot({ path: '/tmp/test8-add-cart-unauth.png', fullPage: true });

    // ============================================================
    // TEST 9: Other Action Buttons
    // ============================================================
    console.log('\n📝 TEST 9: Action Buttons');
    console.log('-'.repeat(70));

    const buttons = await page.evaluate(() => {
      const allButtons = Array.from(document.querySelectorAll('button'));
      return {
        buyNow: allButtons.some(btn => btn.textContent.includes('바로 구매')),
        wishlist: document.querySelector('.btn-wishlist') !== null,
        fitting: allButtons.some(btn => btn.textContent.includes('AI 착장샷'))
      };
    });

    console.log(`   바로 구매: ${buttons.buyNow ? '✅' : '❌'}`);
    console.log(`   찜하기: ${buttons.wishlist ? '✅' : '❌'}`);
    console.log(`   AI 착장샷: ${buttons.fitting ? '✅' : '❌'}`);

    if (buttons.buyNow && buttons.wishlist && buttons.fitting) {
      console.log('✅ All action buttons present');
      testsPassed++;
    } else {
      console.log('⚠️  Some action buttons missing');
    }

    await page.screenshot({ path: '/tmp/test9-buttons.png', fullPage: true });

    // ============================================================
    // TEST 10: Wishlist Button (Unauthenticated)
    // ============================================================
    console.log('\n📝 TEST 10: Wishlist (Unauthenticated)');
    console.log('-'.repeat(70));

    const wishlistBtn = await page.$('.btn-wishlist');
    if (wishlistBtn) {
      await wishlistBtn.click();
      await new Promise(resolve => setTimeout(resolve, 1500));

      const urlAfterWishlist = page.url();
      if (urlAfterWishlist.includes('login')) {
        console.log('✅ Wishlist redirects to login when not authenticated');
        testsPassed++;
        await page.goBack();
        await waitForNetworkIdle(page);
      }
    }

    // ============================================================
    // TEST 11: AI Fitting Navigation
    // ============================================================
    console.log('\n📝 TEST 11: AI Fitting Navigation');
    console.log('-'.repeat(70));

    const fittingBtnExists = await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      return buttons.some(btn => btn.textContent.includes('AI 착장샷'));
    });

    if (fittingBtnExists) {
      await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('button'));
        const btn = buttons.find(btn => btn.textContent.includes('AI 착장샷'));
        if (btn) btn.click();
      });

      await new Promise(resolve => setTimeout(resolve, 1000));

      const urlAfterFitting = page.url();
      if (urlAfterFitting.includes('/fitting')) {
        console.log('✅ AI fitting button navigates to /fitting page');
        testsPassed++;
      }

      // Go back
      await page.goBack();
      await waitForNetworkIdle(page);
    }

    await page.screenshot({ path: '/tmp/test11-fitting-nav.png', fullPage: true });

    // ============================================================
    // TEST 12: Responsive Design (Mobile)
    // ============================================================
    console.log('\n📝 TEST 12: Responsive Design (Mobile)');
    console.log('-'.repeat(70));

    await page.setViewport({ width: 375, height: 667 });
    await new Promise(resolve => setTimeout(resolve, 1000));
    await page.screenshot({ path: '/tmp/test12-mobile.png', fullPage: true });

    const mobileLayout = await page.evaluate(() => {
      const detail = document.querySelector('.product-detail');
      if (!detail) return null;
      const styles = window.getComputedStyle(detail);
      return {
        gridColumns: styles.gridTemplateColumns,
        hasMainImage: document.querySelector('.main-image') !== null
      };
    });

    if (mobileLayout && mobileLayout.hasMainImage) {
      console.log('✅ Mobile layout renders correctly');
      testsPassed++;
    } else {
      console.log('⚠️  Mobile layout issues detected');
    }

    // Reset viewport
    await page.setViewport({ width: 1920, height: 1080 });

    // ============================================================
    // TEST 13: Back to Home
    // ============================================================
    console.log('\n📝 TEST 13: Navigation');
    console.log('-'.repeat(70));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const backToHome = await page.$('.product-grid');
    if (backToHome) {
      console.log('✅ Successfully navigated back to home');
      testsPassed++;
    }

    await page.screenshot({ path: '/tmp/test13-back-home.png', fullPage: true });

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    console.error(error.stack);
    await page.screenshot({ path: '/tmp/error-final.png', fullPage: true });
    testsFailed++;
  } finally {
    // ============================================================
    // TEST SUMMARY
    // ============================================================
    console.log('\n' + '='.repeat(70));
    console.log('📊 TEST SUMMARY');
    console.log('='.repeat(70));
    console.log(`✅ Tests Passed: ${testsPassed}`);
    console.log(`❌ Tests Failed: ${testsFailed}`);
    console.log(`📈 Success Rate: ${((testsPassed / (testsPassed + testsFailed)) * 100).toFixed(2)}%`);
    console.log('\n📸 Screenshots saved to /tmp/test*.png');
    console.log('='.repeat(70));

    console.log('\n🏁 Test finished. Browser will close in 5 seconds...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
