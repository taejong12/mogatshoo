// 모델 로드 함수
async function loadModel() {
	const modelURL = MODEL_PATH + "model.json";
	const metadataURL = MODEL_PATH + "metadata.json";

	try {
		document.getElementById('model-status').textContent = '모델 로드 중...';
		console.log("모델 로드 시도:", modelURL);

		model = await tmImage.load(modelURL, metadataURL);
		maxPredictions = model.getTotalClasses();
		modelLoaded = true;

		document.getElementById('model-status').textContent = '모델 로드 완료!';
		console.log("모델 로드 완료!");

		// 라벨 컨테이너 초기화
		const labelContainer = document.getElementById("label-container");
		labelContainer.innerHTML = '';
		for (let i = 0; i < maxPredictions; i++) {
			labelContainer.appendChild(document.createElement("div"));
		}

		return true;
	} catch (error) {
		document.getElementById('model-status').textContent = '모델 로드 실패: ' + error.message;
		console.error("모델 로드 실패:", error);
		alert("모델 파일을 찾을 수 없습니다. 다음 경로를 확인해주세요: " + modelURL);

		return false;
	}
}

// 이미지 분석 함수
async function analyzeImage() {
	const preview = document.getElementById('image-preview');
	const predictionContainer = document.getElementById('prediction-container');
	const labelContainer = document.getElementById('label-container');
	const predictionJsonInput = document.getElementById('predictionJson');

	// 로딩 표시
	document.getElementById('model-status').textContent = '이미지 분석 중...';
	predictionContainer.style.display = 'block';
	labelContainer.innerHTML = '<div class="text-center"><div class="spinner-border text-dark" role="status"><span class="visually-hidden">로딩중...</span></div></div>';

	// 모델이 로드되지 않았다면 로드 시도
	if (!modelLoaded) {
		// 여러 경로 시도
		const pathsToTry = [
			MODEL_PATH,
			"/my_model/",
			"./my_model/",
			"../my_model/",
			"/static/my_model/",
			"/resources/my_model/",
			window.location.origin + "/my_model/"
		];

		// 각 경로 시도
		let success = false;
		for (const path of pathsToTry) {
			try {
				document.getElementById('model-status').textContent = `모델 로드 시도 중: ${path}...`;
				console.log(`경로 시도: ${path}`);

				const modelURL = path + "model.json";
				const metadataURL = path + "metadata.json";

				model = await tmImage.load(modelURL, metadataURL);
				maxPredictions = model.getTotalClasses();
				modelLoaded = true;

				console.log(`성공한 경로: ${path}`);
				document.getElementById('model-status').textContent = `모델 로드 성공: ${path}`;

				success = true;
				break;
			} catch (err) {
				console.error(`${path} 경로 시도 실패:`, err);
			}
		}

		if (!success) {
			document.getElementById('model-status').textContent = '모든 경로 시도 실패';
			labelContainer.innerHTML = '<div style="color: #EE5A5A; font-weight: bold;">모델 파일을 찾을 수 없습니다. 관리자에게 문의하세요.</div>';
			return;
		}
	}

	try {
		// 이미지 분석 실행
		const predictions = await model.predict(preview);

		// 디버깅: 예측 결과 로깅
		console.log("원본 예측 결과:", JSON.stringify(predictions));

		// 결과 표시
		document.getElementById('model-status').textContent = '이미지 분석 완료';

		// 라벨 컨테이너 업데이트
		labelContainer.innerHTML = ''; // 이전 결과 지우기

		// 예측 결과가 있는지 확인
		if (!predictions || predictions.length === 0) {
			labelContainer.innerHTML = '<div>예측 결과가 없습니다.</div>';
			return;
		}

		// 예측 결과를 확률 순으로 정렬 (복사본 사용)
		const sortedPredictions = [...predictions].sort((a, b) => b.probability - a.probability);

		// 디버깅: 정렬된 예측 결과 로깅
		console.log("정렬된 예측 결과:", JSON.stringify(sortedPredictions));

		// 커스텀 결과 매핑 - 여기서 원하는 결과로 변경 가능
		const customResults = {
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
			},
			"기타사진": {
				displayName: "정수리 또는 탈모 이미지가 아님",
				description: "이 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
				color: "#9C27B0", // 보라색
				recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
			},
			"얼굴금지": {
				displayName: "정수리 또는 탈모 이미지가 아님",
				description: "얼굴이 나온 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
				color: "#9C27B0", // 보라색
				recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
			},
			"'손모양'기타": {
				displayName: "정수리 또는 탈모 이미지가 아님",
				description: "머리외의 다른 신체가 나온 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
				color: "#9C27B0", // 보라색
				recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
			},
			"'손올리지마세요'기타": {
				displayName: "정수리 또는 탈모 이미지가 아님",
				description: "머리외의 다른 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
				color: "#9C27B0", // 보라색
				recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
			}
		};

		// 이미지 분석 함수의 끝부분에 추가 (약 라인 482 근처, 분석 결과 저장 직후)
		predictionJsonInput.value = JSON.stringify(sortedPredictions);

		// 결과 저장 버튼 표시 - 이 부분을 반드시 추가하세요
		document.getElementById('submitButton').style.display = 'inline-block';

		// 가장 높은 확률의 결과를 실제 탈모가 아닌 일부러 다르게 표시하기
		// 여기서 테스트를 위해 원래 결과를 다른 결과로 변경할 수 있습니다
		// 가장 높은 확률의 결과만 사용
		let topPrediction = sortedPredictions[0];
		let className = topPrediction.className;
		let percentage = (topPrediction.probability * 100).toFixed(2);

		// 픽셀 스타일의 결과 컨테이너 생성
		const resultBox = document.createElement("div");
		resultBox.style.backgroundColor = "rgba(255, 255, 255, 0.8)";
		resultBox.style.padding = "20px";
		resultBox.style.borderRadius = "5px";
		resultBox.style.marginTop = "20px";
		resultBox.style.border = "3px solid var(--accent-color)";

		// 커스텀 결과가 있는지 확인
		if (customResults[className]) {
			const customResult = customResults[className];
			// displayName 속성이 있으면 그것을 사용, 없으면 원래 className 사용
			const displayName = customResult.displayName || className;

			resultBox.innerHTML = `
					        <div style="border-bottom: 2px solid var(--accent-color); padding-bottom: 15px; margin-bottom: 15px;">
					            <h3 style="color: ${customResult.color}; font-size: 24px; text-align: center;">
					                ${displayName} (${percentage}%)
					            </h3>
					        </div>
					        <p style="font-size: 18px; margin-bottom: 20px; color: var(--text-dark);">
					            ${customResult.description}
					        </p>
					        <div style="background-color: rgba(43, 43, 216, 0.1); padding: 15px; border-radius: 5px; border: 2px solid ${customResult.color};">
					            <strong style="color: var(--primary-color);">추천:</strong> 
					            <span style="color: var(--text-dark);">${customResult.recommendation}</span>
					        </div>`;
		} else {
			// 커스텀 결과가 없는 경우 기본 형식 사용
			resultBox.innerHTML = `
								<div style="border-bottom: 2px solid var(--accent-color); padding-bottom: 15px; margin-bottom: 15px;">
									<h3 style="color: var(--primary-color); font-size: 24px; text-align: center;">
										${className} (${percentage}%)
									</h3>
								</div>
								<p style="font-size: 18px; margin-bottom: 20px; color: var(--text-dark);">
									정수리만 나온 사진을 다시 찍어 주세요
								</p>
								<div style="background-color: rgba(43, 43, 216, 0.1); padding: 15px; border-radius: 5px; border: 2px solid var(--accent-color);">
									<strong style="color: var(--primary-color);">추천:</strong> 
									<span style="color: var(--text-dark);">사진 재등록 후 사용 바랍니다.</span>
								</div>
							`;
		}

		// analyzeImage 함수의 마지막 부분(약 라인 482 근처)에서 다음과 같이 수정

		labelContainer.appendChild(resultBox);

		// 분석 결과 JSON으로 저장
		predictionJsonInput.value = JSON.stringify(sortedPredictions);

		// 탈모 단계 클래스인 경우에만 결과 저장 버튼 표시
		const validHairLossStages = ["0단계", "1단계", "2단계", "3단계", "4단계"];
		if (validHairLossStages.includes(className)) {
			// 유효한 탈모 단계인 경우 결과 저장 버튼 표시
			document.getElementById('submitButton').style.display = 'inline-block';
			console.log("유효한 탈모 단계 감지됨, 저장 버튼 표시");
		} else {
			// 유효하지 않은 이미지의 경우 버튼 숨김
			document.getElementById('submitButton').style.display = 'none';
			console.log("유효하지 않은 이미지 감지됨, 저장 버튼 숨김");
		}

	} catch (error) {
		console.error("이미지 분석 중 오류 발생:", error);
		document.getElementById('model-status').textContent = '이미지 분석 오류: ' + error.message;
		labelContainer.innerHTML = '<div style="color: #EE5A5A; font-weight: bold;">이미지 분석 중 오류가 발생했습니다.</div>';
	}
}