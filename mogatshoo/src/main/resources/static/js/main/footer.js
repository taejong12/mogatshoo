document.addEventListener("DOMContentLoaded", () => {
    updateClock();
    setInterval(updateClock, 1000);
});

function updateClock() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    document.getElementById("clock").textContent = `${hh}:${mm}`;
}

function openModal(type) {
    const modal = document.getElementById("modal");
    const modalTitle = document.getElementById("modal-title");
    const modalContent = document.getElementById("modal-content");

    if (type === "terms") {
        modalTitle.textContent = "이용약관";
        modalContent.innerHTML = `
            <p>이 사이트를 사용함으로써 다음 조건에 동의하는 것으로 간주됩니다...</p>
            <ul>
                <li>데이터 수집 동의</li>
                <li>개인정보 보호 준수</li>
            </ul>
        `;
    } else if (type === "company") {
        modalTitle.textContent = "회사 소개";
        modalContent.innerHTML = `
            <p>우리는 창의적이고 혁신적인 솔루션을 제공합니다.</p>
            <p>주소: 서울특별시 어딘가</p>
        `;
    }

    modal.style.display = "block";
}

function closeModal() {
    document.getElementById("modal").style.display = "none";
}
