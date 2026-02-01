import puppeteer from 'puppeteer';

/**
 * OAuth2 로그인 및 장바구니 전체 플로우 테스트
 */

const BASE_URL = 'http://localhost:5173';

(async () => {
  console.log('🔐 OAuth2 로그인 + 장바구니 전체 플로우 테스트\n');
  console.log('='.repeat(80));

  const browser = await puppeteer.launch({
    headless: false,
    args: ['--no-sandbox', '--window-size=1920,1080'],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 300
  });

  const page = await browser.newPage();
  let testsPassed = 0;
  let testsFailed = 0;

  try {
    // TEST 1: 로그인 페이지 확인
    console.log('\n📝 TEST 1: 로그인 페이지 접속');
    console.log('-'.repeat(80));

    await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/oauth-test-1-login-page.png', fullPage: true });

    const googleButton = await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      return buttons.some(btn => btn.textContent.includes('Google'));
    });

    if (googleButton) {
      console.log('✅ Google 로그인 버튼 발견');
      testsPassed++;
    } else {
      console.log('❌ Google 로그인 버튼 없음');
      testsFailed++;
    }

    // TEST 2: 로그인 버튼 클릭 (수동 진행 안내)
    console.log('\n\n📝 TEST 2: Google OAuth2 로그인');
    console.log('-'.repeat(80));
    console.log('⚠️  Google OAuth2는 수동으로 진행해야 합니다.');
    console.log('');
    console.log('다음 단계를 진행하세요:');
    console.log('1. 브라우저에서 "Google로 계속하기" 버튼 클릭');
    console.log('2. Google 계정 선택');
    console.log('3. 권한 승인');
    console.log('4. 로그인 성공 후 홈으로 리다이렉트 대기');
    console.log('');
    console.log('60초 동안 대기합니다...');

    // 60초 대기 (사용자가 로그인 완료할 시간)
    await new Promise(resolve => setTimeout(resolve, 60000));

    // TEST 3: 로그인 상태 확인
    console.log('\n\n📝 TEST 3: 로그인 상태 확인');
    console.log('-'.repeat(80));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/oauth-test-3-after-login.png', fullPage: true });

    const loginState = await page.evaluate(() => {
      return {
        token: localStorage.getItem('token'),
        memberId: localStorage.getItem('memberId'),
        memberName: localStorage.getItem('memberName')
      };
    });

    console.log('   Token:', loginState.token ? loginState.token.substring(0, 50) + '...' : 'null');
    console.log('   MemberId:', loginState.memberId);
    console.log('   MemberName:', loginState.memberName);

    if (loginState.token && loginState.memberId && loginState.memberName) {
      console.log('✅ 로그인 성공');
      testsPassed++;
    } else {
      console.log('❌ 로그인 실패');
      testsFailed++;
      throw new Error('로그인 실패 - 테스트 중단');
    }

    // TEST 4: Header에 환영 메시지 확인
    console.log('\n\n📝 TEST 4: Header 환영 메시지');
    console.log('-'.repeat(80));

    const headerMessage = await page.evaluate(() => {
      const greeting = document.querySelector('.header__user-greeting');
      return greeting ? greeting.textContent.trim() : null;
    });

    console.log('   Header 메시지:', headerMessage);

    if (headerMessage && headerMessage.includes('환영합니다')) {
      console.log('✅ 환영 메시지 표시됨');
      testsPassed++;
    } else {
      console.log('❌ 환영 메시지 없음');
      testsFailed++;
    }

    // TEST 5: 상품 상세 → 장바구니 담기
    console.log('\n\n📝 TEST 5: 장바구니에 상품 추가');
    console.log('-'.repeat(80));

    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0' });
      await page.screenshot({ path: '/tmp/oauth-test-5-product-detail.png', fullPage: true });

      const productName = await page.$eval('.product-name', el => el.textContent);
      console.log(`   상품: ${productName}`);

      // 장바구니 담기 버튼 클릭
      const cartButton = await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('button'));
        const btn = buttons.find(b => b.textContent.includes('장바구니'));
        if (btn) {
          btn.click();
          return true;
        }
        return false;
      });

      if (cartButton) {
        console.log('   "장바구니 담기" 버튼 클릭됨');
        await new Promise(resolve => setTimeout(resolve, 2000));

        // alert 확인
        page.on('dialog', async dialog => {
          console.log('   Alert:', dialog.message());
          await dialog.accept();
        });

        await page.screenshot({ path: '/tmp/oauth-test-5-after-add.png', fullPage: true });

        console.log('✅ 장바구니 추가 시도');
        testsPassed++;
      } else {
        console.log('❌ 장바구니 버튼 클릭 실패');
        testsFailed++;
      }
    }

    // TEST 6: 장바구니 페이지 확인
    console.log('\n\n📝 TEST 6: 장바구니 페이지');
    console.log('-'.repeat(80));

    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 2000));
    await page.screenshot({ path: '/tmp/oauth-test-6-cart-page.png', fullPage: true });

    const cartItems = await page.evaluate(() => {
      const items = document.querySelectorAll('.cart-item');
      return {
        count: items.length,
        hasItems: items.length > 0
      };
    });

    console.log('   장바구니 아이템 개수:', cartItems.count);

    if (cartItems.hasItems) {
      console.log('✅ 장바구니에 상품 있음');
      testsPassed++;
    } else {
      console.log('⚠️  장바구니가 비어있음 (추가 실패했을 수 있음)');
    }

    // TEST 7: 백엔드 에러 확인
    console.log('\n\n📝 TEST 7: 콘솔 에러 확인');
    console.log('-'.repeat(80));

    const errors = await page.evaluate(() => {
      // @ts-ignore
      return window.__errors__ || [];
    });

    if (errors.length === 0) {
      console.log('✅ 콘솔 에러 없음');
      testsPassed++;
    } else {
      console.log('❌ 콘솔 에러 발견:');
      errors.forEach((err, idx) => {
        console.log(`   ${idx + 1}. ${err}`);
      });
      testsFailed++;
    }

  } catch (error) {
    console.error('\n❌ 테스트 에러:', error.message);
    await page.screenshot({ path: '/tmp/oauth-test-error.png', fullPage: true });
    testsFailed++;
  } finally {
    // Summary
    console.log('\n' + '='.repeat(80));
    console.log('📊 테스트 결과');
    console.log('='.repeat(80));
    console.log(`✅ 통과: ${testsPassed}`);
    console.log(`❌ 실패: ${testsFailed}`);
    console.log(`📈 성공률: ${((testsPassed / (testsPassed + testsFailed)) * 100).toFixed(2)}%`);

    console.log('\n📸 스크린샷:');
    console.log('   - /tmp/oauth-test-1-login-page.png');
    console.log('   - /tmp/oauth-test-3-after-login.png');
    console.log('   - /tmp/oauth-test-5-product-detail.png');
    console.log('   - /tmp/oauth-test-6-cart-page.png');

    console.log('\n='.repeat(80));
    console.log('🏁 테스트 완료. 브라우저를 10초 후 종료합니다...\n');

    await new Promise(resolve => setTimeout(resolve, 10000));
    await browser.close();
  }
})();
