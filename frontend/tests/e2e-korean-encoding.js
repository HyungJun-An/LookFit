import puppeteer from 'puppeteer';

/**
 * LookFit Korean Encoding E2E Test
 *
 * Tests Korean character encoding from database to frontend display
 */

const BASE_URL = 'http://localhost:5173';
const API_URL = 'http://localhost:8080';

// Expected Korean values from database
const EXPECTED_KOREAN_VALUES = {
  productNames: ['오버핏 울 코트', '숏패딩 점퍼', '데님 트러커 자켓', '레더 라이더 자켓', '베이직 티셔츠'],
  categories: ['아우터', '상의', '하의', '신발', '악세서리'],
  descriptions: ['고급 울 소재 코트', '가벼운 숏패딩', '클래식 자켓', '정통 라이더']
};

// Helper function to check if text is properly encoded Korean
function isValidKorean(text) {
  if (!text) return false;

  // Check if text contains Korean characters (Hangul Unicode range: AC00-D7AF)
  const koreanRegex = /[\uAC00-\uD7AF]/;

  // Check if text contains broken encoding patterns
  const brokenEncodingPatterns = [
    /ì[^한-힣]/,  // Common broken UTF-8 pattern
    /Ã[^한-힣]/,  // Another common pattern
    /â/,          // Garbled characters
    /Â/,
  ];

  // Must contain Korean AND not contain broken patterns
  const hasKorean = koreanRegex.test(text);
  const hasBrokenEncoding = brokenEncodingPatterns.some(pattern => pattern.test(text));

  return hasKorean && !hasBrokenEncoding;
}

// Helper function to detect encoding issues
function detectEncodingIssue(text) {
  if (!text) return 'empty';

  if (/ì[^한-힣]/.test(text)) return 'utf8-latin1-double-encoding';
  if (/Ã/.test(text)) return 'utf8-windows1252';
  if (/â|Â/.test(text)) return 'garbled-multibyte';
  if (!/[\uAC00-\uD7AF]/.test(text)) return 'no-korean-characters';

  return 'none';
}

(async () => {
  console.log('🚀 LookFit Korean Encoding E2E Test\n');
  console.log('='.repeat(70));

  const browser = await puppeteer.launch({
    headless: false,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1920,1080'],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 100
  });

  const page = await browser.newPage();
  let testsPassed = 0;
  let testsFailed = 0;
  const encodingIssues = [];

  try {
    // ============================================================
    // TEST 1: API Response Encoding (Raw JSON)
    // ============================================================
    console.log('\n📝 TEST 1: API Response Encoding');
    console.log('-'.repeat(70));

    const apiResponse = await page.evaluate(async (apiUrl) => {
      const response = await fetch(`${apiUrl}/api/v1/products?page=0&size=5`);
      const data = await response.json();
      return data.content || [];
    }, API_URL);

    console.log('   Checking product names from API:');

    apiResponse.forEach((product, index) => {
      const productName = product.pname;
      const category = product.pcategory;
      const description = product.description;

      console.log(`\n   Product ${index + 1}:`);
      console.log(`     Name: "${productName}"`);
      console.log(`     Category: "${category}"`);
      console.log(`     Description: "${description}"`);

      // Check encoding
      const nameEncoding = detectEncodingIssue(productName);
      const categoryEncoding = detectEncodingIssue(category);
      const descEncoding = detectEncodingIssue(description);

      if (nameEncoding !== 'none') {
        console.log(`     ❌ Name encoding issue: ${nameEncoding}`);
        encodingIssues.push({
          field: 'pname',
          value: productName,
          issue: nameEncoding,
          productId: product.pid
        });
        testsFailed++;
      } else if (isValidKorean(productName)) {
        console.log(`     ✅ Name is properly encoded`);
        testsPassed++;
      } else {
        console.log(`     ⚠️  Name contains no Korean or invalid encoding`);
      }

      if (categoryEncoding !== 'none') {
        console.log(`     ❌ Category encoding issue: ${categoryEncoding}`);
        encodingIssues.push({
          field: 'pcategory',
          value: category,
          issue: categoryEncoding,
          productId: product.pid
        });
      }

      if (descEncoding !== 'none') {
        console.log(`     ❌ Description encoding issue: ${descEncoding}`);
        encodingIssues.push({
          field: 'description',
          value: description,
          issue: descEncoding,
          productId: product.pid
        });
      }
    });

    // ============================================================
    // TEST 2: Frontend Display Encoding
    // ============================================================
    console.log('\n\n📝 TEST 2: Frontend Display Encoding');
    console.log('-'.repeat(70));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test-korean-encoding-home.png', fullPage: true });

    // Get product names from DOM
    const displayedProducts = await page.evaluate(() => {
      const cards = Array.from(document.querySelectorAll('.product-card'));
      return cards.map(card => ({
        name: card.querySelector('.product-card__name')?.textContent.trim(),
        brand: card.querySelector('.product-card__brand')?.textContent.trim(),
        price: card.querySelector('.product-card__price')?.textContent.trim(),
      }));
    });

    console.log('   Checking displayed product names:');

    displayedProducts.slice(0, 5).forEach((product, index) => {
      console.log(`\n   Product ${index + 1}:`);
      console.log(`     Displayed Name: "${product.name}"`);
      console.log(`     Displayed Brand: "${product.brand}"`);

      const nameEncoding = detectEncodingIssue(product.name);

      if (nameEncoding !== 'none') {
        console.log(`     ❌ Display encoding issue: ${nameEncoding}`);
        encodingIssues.push({
          field: 'display_name',
          value: product.name,
          issue: nameEncoding,
          location: 'product-card'
        });
        testsFailed++;
      } else if (isValidKorean(product.name)) {
        console.log(`     ✅ Displayed correctly`);
        testsPassed++;
      }
    });

    // ============================================================
    // TEST 3: Product Detail Page Encoding
    // ============================================================
    console.log('\n\n📝 TEST 3: Product Detail Page Encoding');
    console.log('-'.repeat(70));

    // Click on first product
    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0' });
      await page.screenshot({ path: '/tmp/test-korean-encoding-detail.png', fullPage: true });

      const detailPageData = await page.evaluate(() => {
        return {
          brand: document.querySelector('.product-brand')?.textContent,
          name: document.querySelector('.product-name')?.textContent,
          description: document.querySelector('.product-description')?.textContent,
          category: document.querySelector('.meta-value')?.textContent,
        };
      });

      console.log('   Detail page data:');
      console.log(`     Brand: "${detailPageData.brand}"`);
      console.log(`     Name: "${detailPageData.name}"`);
      console.log(`     Description: "${detailPageData.description?.substring(0, 50)}..."`);
      console.log(`     Category: "${detailPageData.category}"`);

      const detailNameEncoding = detectEncodingIssue(detailPageData.name);
      const detailDescEncoding = detectEncodingIssue(detailPageData.description);

      if (detailNameEncoding !== 'none') {
        console.log(`     ❌ Name encoding issue on detail page: ${detailNameEncoding}`);
        testsFailed++;
      } else if (isValidKorean(detailPageData.name)) {
        console.log(`     ✅ Name properly displayed on detail page`);
        testsPassed++;
      }

      if (detailDescEncoding !== 'none') {
        console.log(`     ❌ Description encoding issue: ${detailDescEncoding}`);
        testsFailed++;
      } else if (isValidKorean(detailPageData.description)) {
        console.log(`     ✅ Description properly displayed`);
        testsPassed++;
      }
    }

    // ============================================================
    // TEST 4: Database Charset Check (via API)
    // ============================================================
    console.log('\n\n📝 TEST 4: Database Configuration Analysis');
    console.log('-'.repeat(70));

    console.log('   Analyzing encoding patterns from API responses...');

    if (encodingIssues.length > 0) {
      console.log('\n   🔍 Detected Encoding Issues:');

      const issueTypes = {};
      encodingIssues.forEach(issue => {
        issueTypes[issue.issue] = (issueTypes[issue.issue] || 0) + 1;
      });

      Object.entries(issueTypes).forEach(([type, count]) => {
        console.log(`     - ${type}: ${count} occurrences`);
      });

      console.log('\n   💡 Diagnosis:');
      if (issueTypes['utf8-latin1-double-encoding']) {
        console.log('     ❌ UTF-8 data is being read as Latin1');
        console.log('     🔧 Solution: Set connection charset to UTF-8');
        console.log('        Add to application.properties:');
        console.log('        spring.datasource.url=jdbc:mysql://...?characterEncoding=UTF-8');
      }
      if (issueTypes['utf8-windows1252']) {
        console.log('     ❌ Character set mismatch detected');
        console.log('     🔧 Solution: Ensure database and connection both use UTF-8');
      }

      testsFailed++;
    } else {
      console.log('   ✅ No encoding issues detected');
      console.log('   Database charset configuration appears correct');
      testsPassed++;
    }

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    await page.screenshot({ path: '/tmp/error-korean-encoding.png', fullPage: true });
    testsFailed++;
  } finally {
    // ============================================================
    // TEST SUMMARY
    // ============================================================
    console.log('\n' + '='.repeat(70));
    console.log('📊 KOREAN ENCODING TEST SUMMARY');
    console.log('='.repeat(70));
    console.log(`✅ Tests Passed: ${testsPassed}`);
    console.log(`❌ Tests Failed: ${testsFailed}`);
    console.log(`🐛 Encoding Issues Found: ${encodingIssues.length}`);

    if (testsFailed > 0) {
      console.log('\n🔧 RECOMMENDED FIXES:');
      console.log('   1. Check MySQL database charset:');
      console.log('      SHOW VARIABLES LIKE "character_set%";');
      console.log('      SHOW VARIABLES LIKE "collation%";');
      console.log('');
      console.log('   2. Update application.properties:');
      console.log('      spring.datasource.url=jdbc:mysql://localhost:3306/lookfit?characterEncoding=UTF-8&useUnicode=true');
      console.log('');
      console.log('   3. Verify table charset:');
      console.log('      ALTER TABLE Product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;');
      console.log('');
      console.log('   4. Re-insert data with correct encoding');
    } else {
      console.log('\n✨ All Korean characters are properly encoded!');
    }

    console.log('='.repeat(70));
    console.log('\n📸 Screenshots saved to /tmp/test-korean-encoding-*.png');

    console.log('\n🏁 Test finished. Browser will close in 5 seconds...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
