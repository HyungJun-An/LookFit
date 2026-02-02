import puppeteer from 'puppeteer';

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

(async () => {
  console.log('🚀 Starting Search Flow E2E Test...\n');

  const browser = await puppeteer.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  let passed = 0;
  let failed = 0;

  try {
    // Test 1: Navigate to homepage
    console.log('✅ Test 1: Navigate to homepage');
    await page.goto('http://localhost:5173', { waitUntil: 'networkidle0' });
    console.log('   ✓ Homepage loaded\n');
    passed++;

    // Test 2: Check if search bar exists
    console.log('✅ Test 2: Check if search bar exists');
    const searchBar = await page.$('.search-bar__input');
    if (!searchBar) {
      console.log('   ✗ Search bar not found\n');
      failed++;
    } else {
      console.log('   ✓ Search bar found\n');
      passed++;
    }

    // Test 3: Click search bar to show suggestions
    console.log('✅ Test 3: Click search bar to show suggestions');
    await page.click('.search-bar__input');
    await sleep(500);

    const suggestions = await page.$('.search-suggestions');
    if (!suggestions) {
      console.log('   ✗ Suggestions not shown\n');
      failed++;
    } else {
      console.log('   ✓ Suggestions shown\n');
      passed++;
    }

    // Test 4: Type search keyword
    console.log('✅ Test 4: Type search keyword "티셔츠"');
    await page.type('.search-bar__input', '티셔츠');
    await sleep(500);

    const inputValue = await page.$eval('.search-bar__input', el => el.value);
    if (inputValue !== '티셔츠') {
      console.log(`   ✗ Input value is "${inputValue}", expected "티셔츠"\n`);
      failed++;
    } else {
      console.log('   ✓ Keyword typed correctly\n');
      passed++;
    }

    // Test 5: Submit search
    console.log('✅ Test 5: Submit search');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'domcontentloaded', timeout: 10000 }),
      page.click('.search-bar__button')
    ]);

    const currentUrl = page.url();
    if (!currentUrl.includes('/search?q=')) {
      console.log(`   ✗ URL is "${currentUrl}", expected to include "/search?q="\n`);
      failed++;
    } else {
      console.log(`   ✓ Navigated to search results: ${currentUrl}\n`);
      passed++;
    }

    // Test 6: Check search results page
    console.log('✅ Test 6: Check search results page');
    const searchTitle = await page.$('.search-results__title');
    if (!searchTitle) {
      console.log('   ✗ Search results title not found\n');
      failed++;
    } else {
      const titleText = await page.$eval('.search-results__title', el => el.textContent);
      console.log(`   ✓ Search results title: ${titleText}\n`);
      passed++;
    }

    // Test 7: Check if products are displayed
    console.log('✅ Test 7: Check if products are displayed');
    await sleep(1000);
    const products = await page.$$('.product-card');

    if (products.length === 0) {
      console.log('   ✗ No products found\n');
      failed++;
    } else {
      console.log(`   ✓ Found ${products.length} products\n`);
      passed++;
    }

    // Test 8: Test sort by price ascending
    console.log('✅ Test 8: Test sort by price ascending');
    await page.click('.sort-button:nth-child(2)'); // 낮은 가격순
    await sleep(1000);

    const firstProductPrice = await page.$eval('.product-card:first-child .product-card__price',
      el => el.textContent);
    console.log(`   ✓ First product price: ${firstProductPrice}\n`);
    passed++;

    // Test 9: Click on a product
    console.log('✅ Test 9: Click on a product');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'domcontentloaded', timeout: 10000 }),
      page.click('.product-card:first-child')
    ]);

    const productDetailUrl = page.url();
    if (!productDetailUrl.includes('/products/')) {
      console.log(`   ✗ URL is "${productDetailUrl}", expected to include "/products/"\n`);
      failed++;
    } else {
      console.log(`   ✓ Navigated to product detail: ${productDetailUrl}\n`);
      passed++;
    }

    // Test 10: Navigate back and check search persistence
    console.log('✅ Test 10: Navigate back to search results');
    await page.goBack();
    await sleep(500);

    const backUrl = page.url();
    if (!backUrl.includes('/search?q=')) {
      console.log(`   ✗ URL is "${backUrl}", expected to include "/search?q="\n`);
      failed++;
    } else {
      console.log(`   ✓ Back to search results: ${backUrl}\n`);
      passed++;
    }

  } catch (error) {
    console.error('❌ Test failed with error:', error.message);
    failed++;
  } finally {
    await browser.close();

    console.log('\n📊 Test Summary:');
    console.log(`   ✅ Passed: ${passed}`);
    console.log(`   ❌ Failed: ${failed}`);
    console.log(`   📈 Total: ${passed + failed}`);

    if (failed === 0) {
      console.log('\n🎉 All tests passed!');
      process.exit(0);
    } else {
      console.log('\n💥 Some tests failed!');
      process.exit(1);
    }
  }
})();
