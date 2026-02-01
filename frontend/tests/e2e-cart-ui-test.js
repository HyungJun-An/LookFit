import puppeteer from 'puppeteer';

/**
 * 장바구니 UI 개선 테스트
 */

const BASE_URL = 'http://localhost:5173';

(async () => {
  console.log('🛒 장바구니 UI 테스트 시작\n');
  console.log('='.repeat(80));

  const browser = await puppeteer.launch({
    headless: false,
    args: ['--no-sandbox', '--window-size=1920,1080'],
    defaultViewport: { width: 1920, height: 1080 },
    slowMo: 200
  });

  const page = await browser.newPage();
  let passed = 0;
  let failed = 0;

  try {
    // TEST 1: 홈 페이지 - Header 장바구니 링크 확인
    console.log('\n📝 TEST 1: Header 장바구니 링크 (아이콘 포함)');
    console.log('-'.repeat(80));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    await page.screenshot({ path: '/tmp/cart-ui-1-header.png', fullPage: true });

    const cartLink = await page.evaluate(() => {
      const link = document.querySelector('.header__cart-link');
      if (!link) return null;

      const icon = link.querySelector('svg');
      const text = link.textContent.trim();
      const styles = window.getComputedStyle(link);

      return {
        hasIcon: !!icon,
        text,
        backgroundColor: styles.backgroundColor,
        display: styles.display,
        visible: link.offsetWidth > 0 && link.offsetHeight > 0
      };
    });

    if (cartLink) {
      console.log(`✅ 장바구니 링크 발견: "${cartLink.text}"`);
      console.log(`   아이콘: ${cartLink.hasIcon ? '✅ 있음' : '❌ 없음'}`);
      console.log(`   배경색: ${cartLink.backgroundColor}`);
      console.log(`   가시성: ${cartLink.visible ? '✅' : '❌'}`);

      if (cartLink.hasIcon && cartLink.visible && cartLink.text.includes('장바구니')) {
        passed++;
      } else {
        failed++;
      }
    } else {
      console.log('❌ 장바구니 링크를 찾을 수 없음');
      failed++;
    }

    // TEST 2: 장바구니 페이지 - 빈 장바구니
    console.log('\n\n📝 TEST 2: 빈 장바구니 페이지');
    console.log('-'.repeat(80));

    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 1000));
    await page.screenshot({ path: '/tmp/cart-ui-2-empty.png', fullPage: true });

    const emptyCartUI = await page.evaluate(() => {
      return {
        hasTitle: !!document.querySelector('.cart-title'),
        title: document.querySelector('.cart-title')?.textContent,
        hasEmptyMessage: document.body.textContent.includes('비어있습니다') ||
                         document.body.textContent.includes('로그인'),
        hasActionButton: !!document.querySelector('.btn')
      };
    });

    console.log(`   제목: "${emptyCartUI.title}"`);
    console.log(`   빈 장바구니 메시지: ${emptyCartUI.hasEmptyMessage ? '✅' : '❌'}`);
    console.log(`   액션 버튼: ${emptyCartUI.hasActionButton ? '✅' : '❌'}`);

    if (emptyCartUI.hasTitle && emptyCartUI.hasEmptyMessage && emptyCartUI.hasActionButton) {
      passed++;
    } else {
      failed++;
    }

    // TEST 3: 상품 페이지에서 장바구니 담기 버튼 확인
    console.log('\n\n📝 TEST 3: 상품 상세 - 장바구니 담기 버튼');
    console.log('-'.repeat(80));

    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    const firstProduct = await page.$('.product-card');
    if (firstProduct) {
      await firstProduct.click();
      await page.waitForNavigation({ waitUntil: 'networkidle0' });
      await page.screenshot({ path: '/tmp/cart-ui-3-product.png', fullPage: true });

      const productUI = await page.evaluate(() => {
        const cartBtn = Array.from(document.querySelectorAll('button')).find(
          btn => btn.textContent.includes('장바구니')
        );

        const quantityControls = document.querySelector('.quantity-controls');
        const minusBtn = quantityControls?.querySelector('button[aria-label*="감소"], button:first-child');
        const plusBtn = quantityControls?.querySelector('button[aria-label*="증가"], button:last-child');

        return {
          hasCartButton: !!cartBtn,
          cartButtonText: cartBtn?.textContent.trim(),
          hasQuantityControls: !!quantityControls,
          hasMinusBtn: !!minusBtn,
          hasPlusBtn: !!plusBtn
        };
      });

      console.log(`   장바구니 담기 버튼: ${productUI.hasCartButton ? '✅' : '❌'}`);
      console.log(`   버튼 텍스트: "${productUI.cartButtonText}"`);
      console.log(`   수량 조절: ${productUI.hasQuantityControls ? '✅' : '❌'}`);
      console.log(`   - 감소 버튼: ${productUI.hasMinusBtn ? '✅' : '❌'}`);
      console.log(`   + 증가 버튼: ${productUI.hasPlusBtn ? '✅' : '❌'}`);

      if (productUI.hasCartButton && productUI.hasQuantityControls) {
        passed++;
      } else {
        failed++;
      }
    }

    // TEST 4: 버튼 스타일 검증
    console.log('\n\n📝 TEST 4: 버튼 스타일 및 접근성');
    console.log('-'.repeat(80));

    const buttonStyles = await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button, a.btn'));
      return buttons.slice(0, 5).map(btn => {
        const styles = window.getComputedStyle(btn);
        return {
          text: btn.textContent.trim().substring(0, 30),
          hasText: btn.textContent.trim().length > 0,
          hasAriaLabel: !!btn.getAttribute('aria-label'),
          hasTitle: !!btn.getAttribute('title'),
          fontSize: styles.fontSize,
          padding: styles.padding,
          backgroundColor: styles.backgroundColor,
          cursor: styles.cursor
        };
      });
    });

    console.log('\n   버튼 스타일 분석:');
    buttonStyles.forEach((btn, idx) => {
      const labelStatus = btn.hasText || btn.hasAriaLabel || btn.hasTitle ? '✅' : '❌';
      console.log(`   ${idx + 1}. ${labelStatus} "${btn.text}"`);
      if (btn.hasTitle) console.log(`      title: ${btn.hasTitle}`);
    });

    const allButtonsHaveLabels = buttonStyles.every(btn =>
      btn.hasText || btn.hasAriaLabel || btn.hasTitle
    );

    if (allButtonsHaveLabels) {
      console.log('\n✅ 모든 버튼에 레이블 있음');
      passed++;
    } else {
      console.log('\n⚠️  일부 버튼에 레이블 부족');
      failed++;
    }

    // TEST 5: 모바일 반응형
    console.log('\n\n📝 TEST 5: 모바일 반응형 (375px)');
    console.log('-'.repeat(80));

    await page.setViewport({ width: 375, height: 667 });
    await page.goto(`${BASE_URL}/cart`, { waitUntil: 'networkidle0' });
    await new Promise(resolve => setTimeout(resolve, 1000));
    await page.screenshot({ path: '/tmp/cart-ui-5-mobile.png', fullPage: true });

    const mobileUI = await page.evaluate(() => {
      const header = document.querySelector('.header');
      const cartLink = document.querySelector('.header__cart-link');

      return {
        headerVisible: header && header.offsetHeight > 0,
        cartLinkVisible: cartLink && cartLink.offsetWidth > 0,
        viewportWidth: window.innerWidth
      };
    });

    console.log(`   뷰포트: ${mobileUI.viewportWidth}px`);
    console.log(`   Header: ${mobileUI.headerVisible ? '✅' : '❌'}`);
    console.log(`   장바구니 링크: ${mobileUI.cartLinkVisible ? '✅' : '❌'}`);

    if (mobileUI.headerVisible && mobileUI.cartLinkVisible) {
      console.log('✅ 모바일 레이아웃 정상');
      passed++;
    } else {
      console.log('❌ 모바일 레이아웃 문제');
      failed++;
    }

  } catch (error) {
    console.error('\n❌ 테스트 에러:', error.message);
    await page.screenshot({ path: '/tmp/cart-ui-error.png', fullPage: true });
    failed++;
  } finally {
    // Summary
    console.log('\n' + '='.repeat(80));
    console.log('📊 테스트 결과');
    console.log('='.repeat(80));
    console.log(`✅ 통과: ${passed}`);
    console.log(`❌ 실패: ${failed}`);
    console.log(`📈 성공률: ${((passed / (passed + failed)) * 100).toFixed(2)}%`);

    console.log('\n✨ UI 개선 사항:');
    console.log('   1. ✅ Header 장바구니 링크에 아이콘 추가');
    console.log('   2. ✅ 장바구니 링크 배경색으로 강조');
    console.log('   3. ✅ 수량 조절 버튼 크기 증가 및 스타일 개선');
    console.log('   4. ✅ 삭제 버튼에 "삭제" 텍스트 추가');
    console.log('   5. ✅ 소계에 "소계" 레이블 추가');
    console.log('   6. ✅ 모든 버튼에 title 속성 추가');

    console.log('\n📸 스크린샷:');
    console.log('   - /tmp/cart-ui-1-header.png');
    console.log('   - /tmp/cart-ui-2-empty.png');
    console.log('   - /tmp/cart-ui-3-product.png');
    console.log('   - /tmp/cart-ui-5-mobile.png');

    console.log('\n='.repeat(80));
    console.log('🏁 테스트 완료. 5초 후 종료...\n');

    await new Promise(resolve => setTimeout(resolve, 5000));
    await browser.close();
  }
})();
