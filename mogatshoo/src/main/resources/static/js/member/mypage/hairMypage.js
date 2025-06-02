let customResults = {
	"1단계": {
		displayName: "탈모 1단계",  // 표시할 이름
		description: "초기 탈모 단계입니다. 모근이 약간 얇아지고 있지만 아직 심각하지 않습니다.",
		color: "#2B2BD8", // 파란색
		recommendation: "규칙적인 생활과 스트레스 관리가 중요합니다. 두피 마사지를 시작해보세요."
	},
	"2단계": {
		displayName: "탈모 2단계",
		description: "진행성 탈모 단계입니다. 모근이 점점 더 얇아지고 있으며 관리가 필요합니다.",
		color: "#FFC107", // 노란색
		recommendation: "두피 전용 샴푸 사용과 영양 섭취에 신경 쓰고, 두피 관리 제품을 사용해보세요."
	},
	"3단계": {
		displayName: "탈모 3단계",
		description: "두피가 들어나는 심각한 탈모 단계입니다. 모근이 상당히 얇아져 있으며 전문적인 케어가 필요합니다.",
		color: "#FF9800", // 주황색
		recommendation: "병원을 방문하여 의사와 상담하고 전문 탈모 치료를 시작하는 것이 좋습니다."
	},
	"4단계": {
		displayName: "대머리화가 진행중인 단계",
		description: "심각한 탈모 단계입니다. 이미 완성형 탈모이며 확연한 모근의 손상이 진행되었습니다.",
		color: "#EE5A5A", // 빨간색
		recommendation: "즉시 전문 병원에서 집중 치료가 필요합니다. 약물 치료나 시술을 고려해보세요."
	},
	"0단계": {
		displayName: "건강한 두피상태",
		description: "현재 두피 상태가 건강합니다. 특별한 탈모 증상이 없는 정상 상태입니다.",
		color: "#4CAF50", // 초록색
		recommendation: "현재의 두피 관리 방식을 유지하고, 건강한 식습관과 생활 습관을 지속하세요."
	}
};

console.log(hairStage);

let customResult = customResults[hairStage];

let hairMsg = document.getElementById("hairMsg");
hairMsg.style.backgroundColor = "#c0c0c0";
hairMsg.style.padding = "20px";
hairMsg.style.borderRadius = "5px";
hairMsg.style.marginTop = "20px";
hairMsg.style.border = "3px solid var(--accent-color)";


let hairMsgDiv = document.createElement('div');

hairMsgDiv.innerHTML = 
	`<div style="border-bottom: 2px solid var(--accent-color); padding-bottom: 15px; margin-bottom: 15px;">
        <h3 style="color: ${customResult.color}; font-size: 24px; text-align: center;">
            ${customResult.displayName}
        </h3>
    </div>
    <p style="font-size: 18px; margin-bottom: 20px; color: var(--text-dark);">
        ${customResult.description}
    </p>
    <div style="background-color: #c0c0c0; padding: 15px; border-radius: 5px; border: 2px solid ${customResult.color};">
        <strong style="color: var(--primary-color);">추천:</strong> 
        <span style="color: var(--text-dark);">${customResult.recommendation}</span>
    </div>`;

hairMsg.appendChild(hairMsgDiv);
