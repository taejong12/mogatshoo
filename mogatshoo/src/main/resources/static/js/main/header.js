// 헤더 관련 JavaScript 기능
document.addEventListener('DOMContentLoaded', function() {
    // 모바일 메뉴 토글 기능 개선
    const navbarToggler = document.querySelector('.navbar-toggler');
    const navbarContent = document.querySelector('#navbarContent');
    
    if (navbarToggler) {
        // 기본 이벤트 방지 및 클래스 토글
        navbarToggler.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // show 클래스 토글
            if (navbarContent.classList.contains('show')) {
                navbarContent.classList.remove('show');
                navbarToggler.setAttribute('aria-expanded', 'false');
            } else {
                navbarContent.classList.add('show');
                navbarToggler.setAttribute('aria-expanded', 'true');
            }
        });
        
        // 메뉴 영역 외 클릭 시 메뉴 닫기
        document.addEventListener('click', function(e) {
            if (navbarContent.classList.contains('show') && 
                !navbarContent.contains(e.target) && 
                e.target !== navbarToggler) {
                navbarContent.classList.remove('show');
                navbarToggler.setAttribute('aria-expanded', 'false');
            }
        });
    }
    
	// 로고 클릭 이벤트 확인 (홈페이지로 이동)
	const logoContainer = document.querySelector('.logo-container a');
	if (logoContainer) {
	    logoContainer.addEventListener('click', function(e) {
	        // 명시적으로 메인 페이지로 이동하도록 설정
	        window.location.href = '/';
	        console.log('로고 클릭: 홈으로 이동');
	    });
	}
    
    // 모바일에서 메뉴 항목 클릭 시 메뉴 닫기
    const menuLinks = document.querySelectorAll('#navbarContent .nav-link, #navbarContent .btn-white');
    menuLinks.forEach(link => {
        link.addEventListener('click', function() {
            if (window.innerWidth < 992) { // Bootstrap lg 브레이크포인트
                navbarContent.classList.remove('show');
                navbarToggler.setAttribute('aria-expanded', 'false');
            }
        });
    });
    
    // 화면 크기 변경 시 메뉴 상태 조정
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 992 && navbarContent.classList.contains('show')) {
            navbarContent.classList.remove('show');
            navbarToggler.setAttribute('aria-expanded', 'false');
        }
    });
});