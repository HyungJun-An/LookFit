import puppeteer from 'puppeteer';

(async () => {
  console.log('🚀 Starting OAuth2 Login Test...\n');

  const browser = await puppeteer.launch({
    headless: false, // Show browser for debugging
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-blink-features=AutomationControlled' // Hide automation detection
    ],
    slowMo: 100, // Slow down by 100ms for visibility
    defaultViewport: null,
    // Use user's actual Chrome profile (not incognito)
    // This allows using already logged-in Google account
    ignoreDefaultArgs: ['--enable-automation']
  });

  const page = await browser.newPage();

  // Set viewport
  await page.setViewport({ width: 1280, height: 800 });

  try {
    // Step 1: Navigate to login page
    console.log('📍 Step 1: Navigate to login page');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/01-login-page.png' });
    console.log('✅ Login page loaded');
    console.log('   Screenshot: /tmp/01-login-page.png\n');

    // Step 2: Click Google login button
    console.log('📍 Step 2: Click Google login button');
    const googleButton = await page.waitForSelector('.google-login-btn', { timeout: 5000 });

    if (!googleButton) {
      throw new Error('Google login button not found!');
    }

    // Listen for navigation
    const navigationPromise = page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 });
    await googleButton.click();
    console.log('✅ Google login button clicked');

    // Wait for redirect to OAuth2 endpoint
    await navigationPromise;
    const currentUrl = page.url();
    console.log('✅ Redirected to:', currentUrl);
    await page.screenshot({ path: '/tmp/02-oauth-redirect.png' });
    console.log('   Screenshot: /tmp/02-oauth-redirect.png\n');

    // Check if redirected to Google OAuth
    if (currentUrl.includes('accounts.google.com')) {
      console.log('✅ Successfully redirected to Google OAuth page');
      console.log('👤 Please login with your Google account in the browser window');
      console.log('⏳ Waiting for login completion (60 seconds timeout)...\n');

      // Wait for redirect back to our app (with extended timeout)
      try {
        await page.waitForFunction(
          () => window.location.href.includes('localhost:5173'),
          { timeout: 60000 }
        );

        const finalUrl = page.url();
        console.log('✅ Redirected back to app:', finalUrl);
        await page.screenshot({ path: '/tmp/03-final-page.png' });
        console.log('   Screenshot: /tmp/03-final-page.png');

        // Check if token exists in URL or localStorage
        if (finalUrl.includes('token=')) {
          console.log('✅ Token found in URL!');
          const urlParams = new URL(finalUrl).searchParams;
          const token = urlParams.get('token');
          const memberId = urlParams.get('memberId');
          console.log('   Token:', token?.substring(0, 20) + '...');
          console.log('   MemberID:', memberId);
        }

        // Check localStorage
        const token = await page.evaluate(() => localStorage.getItem('token'));
        const memberId = await page.evaluate(() => localStorage.getItem('memberId'));

        if (token && memberId) {
          console.log('✅ Token stored in localStorage!');
          console.log('   MemberID:', memberId);
        } else {
          console.log('❌ Token not found in localStorage');
        }

      } catch (error) {
        console.log('⏱️  Timeout: Login not completed within 60 seconds');
        console.log('   Please try running the test again and complete login faster\n');
      }

    } else if (currentUrl.includes('localhost:8080')) {
      console.log('✅ Redirected to backend OAuth2 endpoint');
      console.log('   URL:', currentUrl);

      // Check for errors in the page
      const pageContent = await page.content();
      if (pageContent.includes('error') || pageContent.includes('Error')) {
        console.log('❌ Error detected on backend response');
        console.log('   Page content:', pageContent.substring(0, 500));
      }

    } else if (currentUrl.includes('localhost:5173/login/success')) {
      console.log('✅ Redirected to login success page!');

      // Extract token and memberId from URL
      const urlParams = new URL(currentUrl).searchParams;
      const token = urlParams.get('token');
      const memberId = urlParams.get('memberId');

      console.log('✅ Token:', token?.substring(0, 20) + '...');
      console.log('✅ MemberID:', memberId);

      // Wait for redirect to home
      await new Promise(resolve => setTimeout(resolve, 2000));
      const homeUrl = page.url();
      console.log('✅ Final redirect to:', homeUrl);

    } else {
      console.log('❌ Unexpected redirect URL:', currentUrl);
    }

    console.log('\n✅ OAuth2 Login Flow Test Complete!');

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    await page.screenshot({ path: '/tmp/error-screenshot.png' });
    console.log('Error screenshot saved to: /tmp/error-screenshot.png');
  } finally {
    console.log('\n🏁 Test finished. Browser will close in 5 seconds...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
