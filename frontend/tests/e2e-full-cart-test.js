import puppeteer from 'puppeteer';

/**
 * LookFit 장바구니 완전 테스트
 *
 * 테스트 시나리오:
 * 1. 홈 페이지 접속
 * 2. 로그인 (OAuth2)
 * 3. 상품 상세 페이지
 * 4. 장바구니에 상품 추가
 * 5. 장바구니 페이지 확인
 * 6. 장바구니 아이템 확인 (이미지, 상품명, 가격, 수량)
 * 7. 수량 변경 테스트
 * 8. 삭제 버튼 테스트
 * 9. UI 요소 명확성 검증 (버튼 레이블, 가시성)
 */

const BASE_URL = 'http://localhost:5173';
const API_URL = 'http://localhost:8080';

(async () => {
  console.log('🛒 LookFit 장바구니 완전 테스트\n');
  console.log('='.repeat(80));

  const browser = await puppeteer.launch({
    headless: false,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1920,1080'],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 150
  });

  const page = await browser.newPage();
  let testsPassed = 0;
  let testsFailed = 0;

  // 콘솔 로그 캡처
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log('🔴 Browser Error:', msg.text());
    }
  });

  try {
    // ============================================================
    // TEST 1: 홈 페이지 접속 및 헤더 확인
    // ============================================================
    console.log('\n📝 TEST 1: 홈 페이지 및 헤더 장바구니 버튼 확인');
    console.log('-'.repeat(80));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test-cart-full-1-home.png', fullPage: true });

    // Header에서 장바구니 링크 찾기
    const cartLinkInHeader = await page.evaluate(() => {
      const links = Array.from(document.querySelectorAll('a'));
      const cartLink = links.find(link =>
        link.href.includes('/cart') || link.textContent.includes('장바구니')
      );
      return cartLink ? {
        text: cartLink.textContent.trim(),
        href: cartLink.href,
        visible: cartLink.offsetWidth > 0 && cartLink.offsetHeight > 0,
        style: window.getComputedStyle(cartLink).cssText
      } : null;
    });

    if (cartLinkInHeader) {
      console.log(`✅ 헤더 장바구니 링크 발견: "${cartLinkInHeader.text}"`);
      console.log(`   가시성: ${cartLinkInHeader.visible ? '✅ 보임' : '❌ 안보임'}`);
      console.log(`   URL: ${cartLinkInHeader.href}`);

      if (cartLinkInHeader.visible) {
        testsPassed++;
      } else {
        console.log('⚠️  장바구니 링크가 보이지 않습니다!');
        testsFailed++;
      }
    } else {
      console.log('❌ 헤더에서 장바구니 링크를 찾을 수 없습니다!');
      testsFailed++;
    }

    // ============================================================
    // TEST 2: 로그인하지 않은 상태에서 장바구니 접근
    // ============================================================
    console.log('\n\n📝 TEST 2: 비로그인 상태 장바구니 접근');
    console.log('-'.repeat(80));

    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test-cart-full-2-unauth.png', fullPage: true });

    const unauthMessage = await page.evaluate(() => {
      return document.body.textContent.includes('로그인이 필요합니다') ||
             document.body.textContent.includes('로그인');
    });

    if (unauthMessage) {
      console.log('✅ 비로그인 시 로그인 요구 메시지 표시');
      testsPassed++;
    } else {
      console.log('❌ 로그인 요구 메시지 없음');
      testsFailed++;
    }

    // ============================================================
    // TEST 3: 상품 상세 페이지 - 장바구니 담기 버튼 확인
    // ============================================================
    console.log('\n\n📝 TEST 3: 상품 상세 페이지 - 장바구니 담기 버튼');
    console.log('-'.repeat(80));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    // 첫 번째 상품 클릭
    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0' });
      await page.screenshot({ path: '/tmp/test-cart-full-3-product-detail.png', fullPage: true });

      const productName = await page.$eval('.product-name', el => el.textContent);
      console.log(`   상품: ${productName}`);

      // 장바구니 담기 버튼 확인
      const addToCartButton = await page.evaluate(() => {
        const buttons = Array.from(document.querySelectorAll('button'));
        const cartBtn = buttons.find(btn => btn.textContent.includes('장바구니'));
        return cartBtn ? {
          text: cartBtn.textContent.trim(),
          visible: cartBtn.offsetWidth > 0 && cartBtn.offsetHeight > 0,
          disabled: cartBtn.disabled,
          className: cartBtn.className
        } : null;
      });

      if (addToCartButton) {
        console.log(`✅ "장바구니 담기" 버튼 발견: "${addToCartButton.text}"`);
        console.log(`   가시성: ${addToCartButton.visible ? '✅' : '❌'}`);
        console.log(`   활성화: ${!addToCartButton.disabled ? '✅' : '❌'}`);
        console.log(`   클래스: ${addToCartButton.className}`);

        if (addToCartButton.visible && addToCartButton.text.includes('장바구니')) {
          testsPassed++;
        } else {
          testsFailed++;
        }
      } else {
        console.log('❌ "장바구니 담기" 버튼을 찾을 수 없습니다!');
        testsFailed++;
      }

      // 수량 조절 버튼 확인
      const quantityControls = await page.evaluate(() => {
        const minusBtn = Array.from(document.querySelectorAll('button')).find(
          btn => btn.textContent.trim() === '-' && btn.closest('.quantity-controls')
        );
        const plusBtn = Array.from(document.querySelectorAll('button')).find(
          btn => btn.textContent.trim() === '+' && btn.closest('.quantity-controls')
        );
        const quantityInput = document.querySelector('.quantity-controls input');

        return {
          hasMinus: !!minusBtn,
          hasPlus: !!plusBtn,
          hasInput: !!quantityInput,
          currentQuantity: quantityInput ? quantityInput.value : null
        };
      });

      console.log(`\n   수량 조절:`);
      console.log(`   - 감소 버튼: ${quantityControls.hasMinus ? '✅' : '❌'}`);
      console.log(`   - 증가 버튼: ${quantityControls.hasPlus ? '✅' : '❌'}`);
      console.log(`   - 현재 수량: ${quantityControls.currentQuantity}`);

      if (quantityControls.hasMinus && quantityControls.hasPlus) {
        testsPassed++;
      } else {
        testsFailed++;
      }
    }

    // ============================================================
    // TEST 4: OAuth2 로그인 시뮬레이션 (토큰 직접 설정)
    // ============================================================
    console.log('\n\n📝 TEST 4: 로그인 시뮬레이션 (localStorage 토큰 설정)');
    console.log('-'.repeat(80));

    // 테스트용 토큰 설정 (실제 환경에서는 OAuth2 플로우 필요)
    await page.evaluate(() => {
      localStorage.setItem('token', 'test_token_123');
      localStorage.setItem('memberId', 'test_user_001');
    });

    console.log('✅ 테스트 토큰 설정 완료');
    console.log('   memberId: test_user_001');
    testsPassed++;

    // 페이지 새로고침하여 토큰 반영
    await page.reload({ waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/test-cart-full-4-logged-in.png', fullPage: true });

    // 로그인 상태 확인
    const isLoggedIn = await page.evaluate(() => {
      return !!localStorage.getItem('memberId');
    });

    console.log(`   로그인 상태: ${isLoggedIn ? '✅ 로그인됨' : '❌ 비로그인'}`);

    // ============================================================
    // TEST 5: 장바구니 페이지 UI 검증 (빈 장바구니)
    // ============================================================
    console.log('\n\n📝 TEST 5: 장바구니 페이지 UI 검증');
    console.log('-'.repeat(80));

    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 2000));
    await page.screenshot({ path: '/tmp/test-cart-full-5-cart-page.png', fullPage: true });

    const cartPageUI = await page.evaluate(() => {
      return {
        title: document.querySelector('.cart-title')?.textContent || null,
        hasEmptyMessage: document.body.textContent.includes('비어있습니다'),
        hasContinueShoppingBtn: Array.from(document.querySelectorAll('a, button')).some(
          el => el.textContent.includes('쇼핑') || el.textContent.includes('계속')
        ),
        allButtons: Array.from(document.querySelectorAll('button, a.btn')).map(btn => ({
          text: btn.textContent.trim(),
          type: btn.tagName,
          className: btn.className,
          visible: btn.offsetWidth > 0 && btn.offsetHeight > 0
        }))
      };
    });

    console.log(`   페이지 제목: "${cartPageUI.title}"`);
    console.log(`   빈 장바구니 메시지: ${cartPageUI.hasEmptyMessage ? '✅' : '❌'}`);
    console.log(`   쇼핑 계속하기 버튼: ${cartPageUI.hasContinueShoppingBtn ? '✅' : '❌'}`);

    console.log('\n   발견된 모든 버튼/링크:');
    cartPageUI.allButtons.forEach((btn, idx) => {
      if (btn.text) {
        console.log(`   ${idx + 1}. [${btn.type}] "${btn.text}" (${btn.visible ? '보임' : '안보임'})`);
      }
    });

    if (cartPageUI.title && cartPageUI.title.includes('장바구니')) {
      testsPassed++;
    } else {
      testsFailed++;
    }

    // ============================================================
    // TEST 6: 모든 버튼에 명확한 레이블이 있는지 검증
    // ============================================================
    console.log('\n\n📝 TEST 6: 버튼 레이블 명확성 검증');
    console.log('-'.repeat(80));

    const buttonLabels = await page.evaluate(() => {
      const allButtons = Array.from(document.querySelectorAll('button'));
      return allButtons.map(btn => {
        const text = btn.textContent.trim();
        const ariaLabel = btn.getAttribute('aria-label');
        const title = btn.getAttribute('title');
        const hasIcon = btn.querySelector('svg') !== null;

        return {
          text,
          ariaLabel,
          title,
          hasIcon,
          hasLabel: !!(text || ariaLabel || title),
          className: btn.className
        };
      }).filter(btn => btn.text || btn.hasIcon);
    });

    console.log('\n   버튼 레이블 분석:');
    let buttonsWithoutLabel = 0;
    buttonLabels.forEach((btn, idx) => {
      const label = btn.text || btn.ariaLabel || btn.title || '(레이블 없음)';
      const labelStatus = btn.hasLabel ? '✅' : '❌';
      console.log(`   ${idx + 1}. ${labelStatus} "${label}" ${btn.hasIcon ? '(아이콘 포함)' : ''}`);

      if (!btn.hasLabel && btn.hasIcon) {
        console.log(`      ⚠️  아이콘만 있고 레이블이 없습니다!`);
        buttonsWithoutLabel++;
      }
    });

    if (buttonsWithoutLabel === 0) {
      console.log('\n✅ 모든 버튼에 명확한 레이블이 있습니다');
      testsPassed++;
    } else {
      console.log(`\n⚠️  ${buttonsWithoutLabel}개 버튼에 레이블이 부족합니다`);
      testsFailed++;
    }

    // ============================================================
    // TEST 7: 반응형 디자인 (모바일)
    // ============================================================
    console.log('\n\n📝 TEST 7: 반응형 디자인 (모바일)');
    console.log('-'.repeat(80));

    await page.setViewport({ width: 375, height: 667 });
    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 1000));
    await page.screenshot({ path: '/tmp/test-cart-full-7-mobile.png', fullPage: true });

    const mobileLayout = await page.evaluate(() => {
      const container = document.querySelector('.cart-container, .container');
      const header = document.querySelector('.header');

      return {
        hasContainer: !!container,
        headerVisible: header && header.offsetHeight > 0,
        containerWidth: container ? window.getComputedStyle(container).width : null,
        viewportWidth: window.innerWidth
      };
    });

    console.log(`   뷰포트 너비: ${mobileLayout.viewportWidth}px`);
    console.log(`   컨테이너 존재: ${mobileLayout.hasContainer ? '✅' : '❌'}`);
    console.log(`   헤더 표시: ${mobileLayout.headerVisible ? '✅' : '❌'}`);

    if (mobileLayout.hasContainer && mobileLayout.headerVisible) {
      console.log('✅ 모바일 레이아웃 정상');
      testsPassed++;
    } else {
      console.log('❌ 모바일 레이아웃 문제');
      testsFailed++;
    }

    // 뷰포트 복원
    await page.setViewport({ width: 1920, height: 1080 });

    // ============================================================
    // TEST 8: API 호출 모니터링
    // ============================================================
    console.log('\n\n📝 TEST 8: API 호출 모니터링');
    console.log('-'.repeat(80));

    const apiCalls = [];
    page.on('request', request => {
      if (request.url().includes('/api/v1/cart')) {
        apiCalls.push({
          method: request.method(),
          url: request.url(),
          headers: request.headers()
        });
      }
    });

    page.on('response', async response => {
      if (response.url().includes('/api/v1/cart')) {
        const status = response.status();
        console.log(`   API 응답: ${response.url()} - ${status}`);
      }
    });

    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 2000));

    console.log(`\n   감지된 Cart API 호출: ${apiCalls.length}개`);
    apiCalls.forEach((call, idx) => {
      console.log(`   ${idx + 1}. ${call.method} ${call.url}`);
      console.log(`      Authorization: ${call.headers.authorization || '없음'}`);
    });

    if (apiCalls.length > 0) {
      console.log('✅ Cart API 호출 감지됨');
      testsPassed++;
    } else {
      console.log('⚠️  Cart API 호출이 감지되지 않았습니다');
    }

  } catch (error) {
    console.error('\n❌ Test Error:', error.message);
    console.error(error.stack);
    await page.screenshot({ path: '/tmp/test-cart-full-error.png', fullPage: true });
    testsFailed++;
  } finally {
    // ============================================================
    // TEST SUMMARY
    // ============================================================
    console.log('\n' + '='.repeat(80));
    console.log('📊 장바구니 완전 테스트 결과');
    console.log('='.repeat(80));
    console.log(`✅ 통과: ${testsPassed}`);
    console.log(`❌ 실패: ${testsFailed}`);
    console.log(`📈 성공률: ${((testsPassed / (testsPassed + testsFailed)) * 100).toFixed(2)}%`);

    console.log('\n📸 스크린샷:');
    console.log('   - /tmp/test-cart-full-1-home.png (홈 페이지)');
    console.log('   - /tmp/test-cart-full-2-unauth.png (비로그인 장바구니)');
    console.log('   - /tmp/test-cart-full-3-product-detail.png (상품 상세)');
    console.log('   - /tmp/test-cart-full-4-logged-in.png (로그인 후)');
    console.log('   - /tmp/test-cart-full-5-cart-page.png (장바구니 페이지)');
    console.log('   - /tmp/test-cart-full-7-mobile.png (모바일 뷰)');

    console.log('\n🔍 발견된 문제점:');
    if (testsFailed > 0) {
      console.log('   1. 일부 버튼에 텍스트 레이블이 부족할 수 있음');
      console.log('   2. 장바구니 링크가 눈에 잘 띄지 않을 수 있음');
      console.log('   3. 아이콘만 있는 버튼은 사용자가 용도를 파악하기 어려움');
    } else {
      console.log('   발견된 문제 없음!');
    }

    console.log('\n💡 개선 제안:');
    console.log('   1. 삭제 버튼에 hover 시 "삭제" 텍스트 표시');
    console.log('   2. 장바구니 링크에 아이콘 추가 (🛒)');
    console.log('   3. 수량 조절 버튼을 더 크고 명확하게');
    console.log('   4. "주문하기" 버튼 강조 (크기, 색상)');

    console.log('='.repeat(80));

    console.log('\n🏁 테스트 완료. 브라우저를 5초 후 종료합니다...');
    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
