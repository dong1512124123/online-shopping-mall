// FASHION STORE - Main JavaScript

document.addEventListener('DOMContentLoaded', function () {

    // 현재 카테고리 활성화 표시
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-category').forEach(function (link) {
        if (currentPath.startsWith(link.getAttribute('href'))) {
            link.classList.add('fw-bold');
            link.style.color = '#111';
        }
    });

});
