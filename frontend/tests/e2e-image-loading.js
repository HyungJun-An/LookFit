import puppeteer from 'puppeteer';

/**
 * LookFit Image Loading E2E Test
 *
 * Tests:
 * 1. Product images load successfully
 * 2. No 404 errors for images
 * 3. Image alt attributes (accessibility)
 * 4. Image dimensions are appropriate
 * 5. Lazy loading (if implemented)
 * 6. Image gallery functionality
 */

const BASE_URL = 'http://localhost:5173';

(async () => {
  console.log('🚀 LookFit Image Loading E2E Test\n');
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
  const imageErrors = [];
  const loadedImages = [];

  // Track failed image loads
  page.on('response', response => {
    const url = response.url();
    const status = response.status();
    const isImage = /\.(jpg|jpeg|png|gif|webp|svg)(\?.*)?$/i.test(url) ||
                    url.includes('unsplash.com');

    if (isImage) {
      if (status >= 400) {
        imageErrors.push({
          url,
          status,
          statusText: response.statusText()
        });
      } else if (status >= 200 && status < 300) {
        loadedImages.push({ url, status });
      }
    }
  });

  // Track console errors
  page.on('console', msg => {
    if (msg.type() === 'error' && msg.text().toLowerCase().includes('image')) {
      console.log('🔴 Image Error:', msg.text());
    }
  });

  try {
    // ============================================================
    // TEST 1: Product List Images
    // ============================================================
    console.log('\n📝 TEST 1: Product List Page Images');
    console.log('-'.repeat(70));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0', timeout: 10000 });
    await page.screenshot({ path: '/tmp/test-images-home.png', fullPage: true });

    // Wait a bit for all images to load
    await new Promise(resolve => setTimeout(resolve, 2000));

    const productImages = await page.evaluate(() => {
      const cards = Array.from(document.querySelectorAll('.product-card'));
      return cards.map((card, index) => {
        const img = card.querySelector('img');
        return {
          index,
          src: img?.src || null,
          alt: img?.alt || null,
          naturalWidth: img?.naturalWidth || 0,
          naturalHeight: img?.naturalHeight || 0,
          displayWidth: img?.width || 0,
          displayHeight: img?.height || 0,
          complete: img?.complete || false,
          hasError: img && (img.naturalWidth === 0 && img.complete)
        };
      });
    });

    console.log(`   Found ${productImages.length} product images`);

    let loadedCount = 0;
    let errorCount = 0;
    let missingAltCount = 0;

    productImages.forEach((img, i) => {
      if (!img.src) {
        console.log(`   ❌ Product ${i + 1}: No image src`);
        errorCount++;
        return;
      }

      if (img.hasError) {
        console.log(`   ❌ Product ${i + 1}: Failed to load - ${img.src.substring(0, 50)}...`);
        errorCount++;
      } else if (img.complete && img.naturalWidth > 0) {
        loadedCount++;
        if (!img.alt || img.alt.trim() === '') {
          missingAltCount++;
        }
      }
    });

    console.log(`\n   Summary:`);
    console.log(`   - Loaded successfully: ${loadedCount}/${productImages.length}`);
    console.log(`   - Failed to load: ${errorCount}`);
    console.log(`   - Missing alt text: ${missingAltCount}`);

    if (loadedCount === productImages.length) {
      console.log(`   ✅ All product images loaded successfully`);
      testsPassed++;
    } else if (loadedCount > 0) {
      console.log(`   ⚠️  Some images failed to load`);
    } else {
      console.log(`   ❌ No images loaded`);
      testsFailed++;
    }

    if (missingAltCount === 0) {
      console.log(`   ✅ All images have alt text (accessibility)`);
      testsPassed++;
    } else {
      console.log(`   ⚠️  ${missingAltCount} images missing alt text`);
    }

    // ============================================================
    // TEST 2: Product Detail Page Images
    // ============================================================
    console.log('\n\n📝 TEST 2: Product Detail Page Images');
    console.log('-'.repeat(70));

    // Click first product
    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 });
      await new Promise(resolve => setTimeout(resolve, 2000));
      await page.screenshot({ path: '/tmp/test-images-detail.png', fullPage: true });

      const detailImages = await page.evaluate(() => {
        const mainImg = document.querySelector('.main-image img');
        const thumbnails = Array.from(document.querySelectorAll('.thumbnail-list img'));

        const getImageInfo = (img) => ({
          src: img?.src || null,
          alt: img?.alt || null,
          naturalWidth: img?.naturalWidth || 0,
          naturalHeight: img?.naturalHeight || 0,
          complete: img?.complete || false,
          hasError: img && (img.naturalWidth === 0 && img.complete)
        });

        return {
          mainImage: mainImg ? getImageInfo(mainImg) : null,
          thumbnails: thumbnails.map(getImageInfo),
          totalImages: (mainImg ? 1 : 0) + thumbnails.length
        };
      });

      console.log(`   Found ${detailImages.totalImages} images on detail page`);
      console.log(`   - Main image: ${detailImages.mainImage ? '✅' : '❌'}`);
      console.log(`   - Thumbnails: ${detailImages.thumbnails.length}`);

      if (detailImages.mainImage && detailImages.mainImage.complete &&
          detailImages.mainImage.naturalWidth > 0) {
        console.log(`   ✅ Main image loaded successfully`);
        console.log(`      Size: ${detailImages.mainImage.naturalWidth}x${detailImages.mainImage.naturalHeight}px`);
        testsPassed++;
      } else {
        console.log(`   ❌ Main image failed to load`);
        testsFailed++;
      }

      // Test thumbnails
      const loadedThumbnails = detailImages.thumbnails.filter(
        img => img.complete && img.naturalWidth > 0
      ).length;

      console.log(`   Thumbnails: ${loadedThumbnails}/${detailImages.thumbnails.length} loaded`);

      if (loadedThumbnails === detailImages.thumbnails.length && loadedThumbnails > 0) {
        console.log(`   ✅ All thumbnails loaded`);
        testsPassed++;
      } else if (loadedThumbnails === 0 && detailImages.thumbnails.length === 0) {
        console.log(`   ⚠️  No thumbnails found (may not be implemented)`);
      }

      // ============================================================
      // TEST 3: Image Gallery Interaction
      // ============================================================
      console.log('\n\n📝 TEST 3: Image Gallery Click Test');
      console.log('-'.repeat(70));

      if (detailImages.thumbnails.length > 1) {
        // Get initial main image src
        const initialMainSrc = await page.$eval('.main-image img', img => img.src);
        console.log(`   Initial main image: ${initialMainSrc.substring(initialMainSrc.lastIndexOf('/'))}`);

        // Click second thumbnail
        const thumbnailElements = await page.$$('.thumbnail-list img');
        if (thumbnailElements.length > 1) {
          await thumbnailElements[1].click();
          await new Promise(resolve => setTimeout(resolve, 500));

          const newMainSrc = await page.$eval('.main-image img', img => img.src);
          console.log(`   After click: ${newMainSrc.substring(newMainSrc.lastIndexOf('/'))}`);

          if (initialMainSrc !== newMainSrc) {
            console.log(`   ✅ Thumbnail click changes main image`);
            testsPassed++;

            // Verify new image loaded
            const newImageLoaded = await page.$eval('.main-image img', img =>
              img.complete && img.naturalWidth > 0
            );

            if (newImageLoaded) {
              console.log(`   ✅ New main image loaded successfully`);
              testsPassed++;
            } else {
              console.log(`   ❌ New main image failed to load`);
              testsFailed++;
            }
          } else {
            console.log(`   ⚠️  Thumbnail click did not change main image`);
          }
        }
      } else {
        console.log(`   ⚠️  Not enough thumbnails to test gallery interaction`);
      }

      await page.screenshot({ path: '/tmp/test-images-gallery.png', fullPage: true });
    }

    // ============================================================
    // TEST 4: Network Image Errors
    // ============================================================
    console.log('\n\n📝 TEST 4: Network Image Errors');
    console.log('-'.repeat(70));

    console.log(`   Tracked ${loadedImages.length + imageErrors.length} image requests`);

    if (imageErrors.length === 0) {
      console.log(`   ✅ No HTTP errors for images`);
      testsPassed++;
    } else {
      console.log(`   ❌ Found ${imageErrors.length} image errors:`);
      imageErrors.forEach((err, i) => {
        console.log(`      ${i + 1}. ${err.status} ${err.statusText}`);
        console.log(`         ${err.url.substring(0, 80)}...`);
      });
      testsFailed++;
    }

    console.log(`\n   Successfully loaded images: ${loadedImages.length}`);

    // Show some successfully loaded images
    if (loadedImages.length > 0) {
      console.log(`   Sample loaded images:`);
      loadedImages.slice(0, 3).forEach((img, i) => {
        const shortUrl = img.url.substring(img.url.lastIndexOf('/'));
        console.log(`      ${i + 1}. ${img.status} - ${shortUrl}`);
      });
    }

    // ============================================================
    // TEST 5: Image Optimization Check
    // ============================================================
    console.log('\n\n📝 TEST 5: Image Optimization');
    console.log('-'.repeat(70));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const imageOptimization = await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      const stats = {
        total: images.length,
        withSrcset: images.filter(img => img.srcset).length,
        withLoading: images.filter(img => img.loading === 'lazy').length,
        webp: images.filter(img => img.src.includes('.webp')).length,
        oversized: 0
      };

      // Check for oversized images (natural size much larger than display size)
      images.forEach(img => {
        if (img.naturalWidth > img.width * 2 || img.naturalHeight > img.height * 2) {
          stats.oversized++;
        }
      });

      return stats;
    });

    console.log(`   Total images: ${imageOptimization.total}`);
    console.log(`   Using srcset (responsive): ${imageOptimization.withSrcset}`);
    console.log(`   Using lazy loading: ${imageOptimization.withLoading}`);
    console.log(`   WebP format: ${imageOptimization.webp}`);
    console.log(`   Oversized images: ${imageOptimization.oversized}`);

    if (imageOptimization.oversized === 0) {
      console.log(`   ✅ No oversized images detected`);
      testsPassed++;
    } else {
      console.log(`   ⚠️  ${imageOptimization.oversized} images may be too large`);
    }

    // ============================================================
    // TEST 6: Accessibility
    // ============================================================
    console.log('\n\n📝 TEST 6: Image Accessibility');
    console.log('-'.repeat(70));

    const accessibilityIssues = await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      const issues = {
        noAlt: [],
        emptyAlt: [],
        decorativeNoRole: []
      };

      images.forEach((img, index) => {
        if (!img.hasAttribute('alt')) {
          issues.noAlt.push(index);
        } else if (img.alt.trim() === '' && !img.hasAttribute('role')) {
          issues.decorativeNoRole.push(index);
        }
      });

      return {
        total: images.length,
        issues
      };
    });

    console.log(`   Images checked: ${accessibilityIssues.total}`);
    console.log(`   Missing alt attribute: ${accessibilityIssues.issues.noAlt.length}`);
    console.log(`   Empty alt (decorative): ${accessibilityIssues.issues.decorativeNoRole.length}`);

    if (accessibilityIssues.issues.noAlt.length === 0) {
      console.log(`   ✅ All images have alt attributes`);
      testsPassed++;
    } else {
      console.log(`   ⚠️  ${accessibilityIssues.issues.noAlt.length} images missing alt attribute`);
    }

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    await page.screenshot({ path: '/tmp/error-images.png', fullPage: true });
    testsFailed++;
  } finally {
    // ============================================================
    // TEST SUMMARY
    // ============================================================
    console.log('\n' + '='.repeat(70));
    console.log('📊 IMAGE LOADING TEST SUMMARY');
    console.log('='.repeat(70));
    console.log(`✅ Tests Passed: ${testsPassed}`);
    console.log(`❌ Tests Failed: ${testsFailed}`);
    console.log(`📈 Success Rate: ${((testsPassed / (testsPassed + testsFailed)) * 100).toFixed(2)}%`);

    console.log('\n📸 Screenshots:');
    console.log('   - /tmp/test-images-home.png');
    console.log('   - /tmp/test-images-detail.png');
    console.log('   - /tmp/test-images-gallery.png');

    console.log('\n💡 Recommendations:');
    if (imageErrors.length > 0) {
      console.log('   ⚠️  Fix broken image links');
    }
    console.log('   - Consider implementing lazy loading for better performance');
    console.log('   - Use WebP format for better compression');
    console.log('   - Implement srcset for responsive images');
    console.log('   - Ensure all images have descriptive alt text');

    console.log('='.repeat(70));

    console.log('\n🏁 Test finished. Browser will close in 5 seconds...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
