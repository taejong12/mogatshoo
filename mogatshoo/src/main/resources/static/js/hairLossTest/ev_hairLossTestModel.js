
// 전역 변수들
const MODEL_PATH = "/my_model/";
let model, maxPredictions;
let modelLoaded = false;
let analysisStartTime;
let analysisInterval;


function updateSystemStatus(status, color = '#000') {
	const statusElement = document.getElementById('system-status');
	if (statusElement) {
		statusElement.textContent = status;
		statusElement.style.color = color;
	}
	console.log(`📡 시스템 상태: ${status}`);
}

function updateProgress(percentage) {
	const progressFills = document.querySelectorAll('.progress-fill, #progress-fill, #loading-progress, #analysis-progress');
	progressFills.forEach(fill => {
		if (fill) {
			fill.style.width = percentage + '%';
		}
	});
}

function showLoadingDialog(show) {
	const dialog = document.getElementById('loadingDialog');
	if (dialog) {
		dialog.style.display = show ? 'block' : 'none';
	}
}

function formatTime(seconds) {
	const mins = Math.floor(seconds / 60);
	const secs = seconds % 60;
	return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

function startAnalysisTimer() {
	analysisStartTime = Date.now();
	const timeElement = document.getElementById('analysis-time');

	if (timeElement) {
		analysisInterval = setInterval(() => {
			const elapsed = Math.floor((Date.now() - analysisStartTime) / 1000);
			timeElement.textContent = formatTime(elapsed);
		}, 1000);
	}
}

function stopAnalysisTimer() {
	if (analysisInterval) {
		clearInterval(analysisInterval);
		analysisInterval = null;
	}
}

function showWin95Alert(message, type = 'info') {
	try {
		const modal = document.getElementById('resultModal');
		const modalMessage = document.getElementById('modalMessage');
		const iconElement = modal ? modal.querySelector('.modal-body span') : null;

		if (modalMessage) {
			modalMessage.innerHTML = message.replace(/\n/g, '<br>');
		}

		if (iconElement) {
			switch (type) {
				case 'success': iconElement.textContent = '✅'; break;
				case 'error': iconElement.textContent = '❌'; break;
				case 'warning': iconElement.textContent = '⚠️'; break;
				default: iconElement.textContent = 'ℹ️';
			}
		}

		if (modal && typeof bootstrap !== 'undefined') {
			const bootstrapModal = new bootstrap.Modal(modal);
			bootstrapModal.show();
		} else {
			alert(message.replace(/<br>/g, '\n'));
		}
	} catch (error) {
		console.error('모달 표시 오류:', error);
		alert(message.replace(/<br>/g, '\n'));
	}
}


function previewImage() {
	console.log('🖼️ 이미지 미리보기 시작');

	const input = document.getElementById('imageInput');
	const preview = document.getElementById('image-preview');
	const placeholder = document.getElementById('preview-placeholder');
	const analyzeButton = document.getElementById('analyzeButton');

	if (input.files && input.files[0]) {
		const file = input.files[0];

		console.log('📁 파일 정보:', {
			name: file.name,
			size: file.size,
			type: file.type
		});

		// 파일 크기 체크 (5MB)
		if (file.size > 5 * 1024 * 1024) {
			showWin95Alert('파일 크기가 5MB를 초과합니다.\n더 작은 파일을 선택해주세요.', 'warning');
			input.value = '';
			return;
		}

		// 파일 형식 체크
		const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
		if (!allowedTypes.includes(file.type)) {
			showWin95Alert('지원하지 않는 파일 형식입니다.\nJPG, PNG 파일만 업로드 가능합니다.', 'warning');
			input.value = '';
			return;
		}

		const reader = new FileReader();

		reader.onload = function(e) {
			preview.src = e.target.result;
			preview.style.display = 'block';

			if (placeholder) placeholder.style.display = 'none';
			if (analyzeButton) analyzeButton.style.display = 'inline-block';

			updateSystemStatus('이미지 로드 완료 - 분석 준비됨', '#008000');
			console.log('✅ 이미지 미리보기 완료');
		};

		reader.onerror = function() {
			showWin95Alert('이미지 파일을 읽는 중 오류가 발생했습니다.', 'error');
			updateSystemStatus('이미지 로드 실패', '#ff0000');
		};

		reader.readAsDataURL(file);
	}
}

// 메인 분석 함수

async function analyzeImage() {
	console.log('🔍 이미지 분석 시작');

	const preview = document.getElementById('image-preview');
	const predictionContainer = document.getElementById('prediction-container');
	const labelContainer = document.getElementById('label-container');
	const predictionJsonInput = document.getElementById('predictionJson');
	const analyzeButton = document.getElementById('analyzeButton');

	// 이미지 유효성 검사
	if (!preview || !preview.src || preview.src.includes('about:blank') || preview.src === '') {
		console.warn('⚠️ 유효하지 않은 이미지');
		showWin95Alert('분석할 이미지를 먼저 선택해주세요.', 'warning');
		return;
	}

	try {
		console.log('📊 분석 프로세스 시작');

		// UI 상태 업데이트
		if (analyzeButton) {
			analyzeButton.disabled = true;
			analyzeButton.textContent = '⏳ 분석 중...';
		}

		updateSystemStatus('AI 분석 진행 중...', '#ff0000');
		showLoadingDialog(true);
		startAnalysisTimer();

		const progressBar = document.getElementById('progress-bar');
		if (progressBar) progressBar.style.display = 'block';

		const modelStatusElement = document.getElementById('model-status');
		if (modelStatusElement) modelStatusElement.textContent = '분석 중...';

		if (predictionContainer) predictionContainer.style.display = 'block';

		// 로딩 UI 표시
		if (labelContainer) {
			labelContainer.innerHTML = `
                <div style="text-align: center; padding: 20px; background: #c0c0c0; border: 2px inset #c0c0c0;">
                    <div style="font-weight: bold; margin-bottom: 10px;">⏳ AI 분석 진행 중...</div>
                    <div class="progress-bar" style="width: 200px; margin: 10px auto; background: #ffffff; border: 2px inset #c0c0c0; height: 20px;">
                        <div class="progress-fill" id="analysis-progress" style="width: 0%; height: 100%; background: repeating-linear-gradient(45deg, #000080, #000080 4px, #0080ff 4px, #0080ff 8px); transition: width 0.3s ease;"></div>
                    </div>
                    <div style="font-size: 14px; color: #808080; margin-top: 5px;">
                        잠시만 기다려주세요...
                    </div>
                </div>
            `;
		}

		// 진행률 애니메이션
		for (let i = 0; i <= 80; i += 20) {
			updateProgress(i);
			await new Promise(resolve => setTimeout(resolve, 200));
		}

		// 모델 로드 체크 및 로드
		if (!modelLoaded || !model) {
			console.log('🤖 AI 모델 로드 필요');
			await loadModelWithRetry();
		}

		// 최종 진행률
		updateProgress(100);

		console.log('🎯 AI 예측 실행');

		// AI 예측
		const predictions = await model.predict(preview);
		console.log("📊 예측 결과:", predictions);

		if (!predictions || predictions.length === 0) {
			throw new Error('예측 결과가 없습니다.');
		}

		// 결과 처리 및 표시
		await displayResults(predictions, predictionJsonInput, labelContainer);

		// 완료 상태
		updateSystemStatus('분석 완료', '#008000');
		if (modelStatusElement) modelStatusElement.textContent = '분석 완료';

		showWin95Alert('이미지 분석이 완료되었습니다!', 'success');
		console.log('🎉 분석 완료!');

	} catch (error) {
		console.error("❌ 분석 오류:", error);
		handleAnalysisError(error, labelContainer);
	} finally {
		// 정리 작업
		if (analyzeButton) {
			analyzeButton.disabled = false;
			analyzeButton.textContent = '🔍 분석 시작';
		}
		showLoadingDialog(false);
		stopAnalysisTimer();

		const progressBar = document.getElementById('progress-bar');
		if (progressBar) progressBar.style.display = 'none';

		console.log('🔚 분석 프로세스 종료');
	}
}

// 모델 로드 함수

async function loadModelWithRetry() {
	const pathsToTry = [
		MODEL_PATH,
		"/my_model/",
		"./my_model/",
		"../my_model/",
		"/static/my_model/",
		"/resources/my_model/",
		window.location.origin + "/my_model/"
	];

	// tmImage 라이브러리 확인
	if (typeof tmImage === 'undefined') {
		throw new Error('Teachable Machine 라이브러리가 로드되지 않았습니다.');
	}

	for (const path of pathsToTry) {
		try {
			console.log(`🔄 모델 로드 시도: ${path}`);
			updateSystemStatus(`모델 로드 시도: ${path}`, '#0000ff');

			const modelURL = path + "model.json";
			const metadataURL = path + "metadata.json";

			model = await tmImage.load(modelURL, metadataURL);
			maxPredictions = model.getTotalClasses();
			modelLoaded = true;

			console.log(`✅ 모델 로드 성공: ${path}`);
			updateSystemStatus('모델 로드 성공', '#008000');
			return;
		} catch (err) {
			console.error(`❌ ${path} 실패:`, err.message);
		}
	}

	throw new Error('모든 경로에서 AI 모델 로드 실패');
}

// 결과 표시 함수

async function displayResults(predictions, predictionJsonInput, labelContainer) {
	const sortedPredictions = [...predictions].sort((a, b) => b.probability - a.probability);
	const topPrediction = sortedPredictions[0];
	const className = topPrediction.className;
	const percentage = (topPrediction.probability * 100).toFixed(2);

	console.log(`🏆 최고 예측: ${className} (${percentage}%)`);

	// JSON 저장
	if (predictionJsonInput) {
		predictionJsonInput.value = JSON.stringify(sortedPredictions);
	}

	// 결과 매핑
	const customResults = {
		"1단계": {
			displayName: "탈모 1단계", 
			description: "초기 탈모 단계입니다. 모근이 약간 얇아지고 있지만 아직 심각하지 않습니다.",
			color: "#2B2BD8",
			icon: "🟦",
			recommendation: "규칙적인 생활과 스트레스 관리가 중요합니다. 두피 마사지를 시작해보세요."
		},
		"2단계": {
			displayName: "탈모 2단계",
			description: "진행성 탈모 단계입니다. 모근이 점점 더 얇아지고 있으며 관리가 필요합니다.",
			color: "#FFC107",  
			icon: "🟨",
			recommendation: "두피 전용 샴푸 사용과 영양 섭취에 신경 쓰고, 두피 관리 제품을 사용해보세요."
		},
		"3단계": {
			displayName: "탈모 3단계",
			description: "두피가 들어나는 심각한 탈모 단계입니다. 모근이 상당히 얇아져 있으며 전문적인 케어가 필요합니다.",
			color: "#FF9800", 
			icon: "🟧",
			recommendation: "병원을 방문하여 의사와 상담하고 전문 탈모 치료를 시작하는 것이 좋습니다."
		},
		"4단계": {
			displayName: "대머리화가 진행중인 단계",
			description: "심각한 탈모 단계입니다. 이미 완성형 탈모이며 확연한 모근의 손상이 진행되었습니다.",
			color: "#EE5A5A", // 
			icon: "🟥",
			recommendation: "즉시 전문 병원에서 집중 치료가 필요합니다. 약물 치료나 시술을 고려해보세요."
		},
		"0단계": {
			displayName: "건강한 두피상태",
			description: "현재 두피 상태가 건강합니다. 특별한 탈모 증상이 없는 정상 상태입니다.",
			color: "#4CAF50", // 초록색
			icon: "🟩",
			recommendation: "현재의 두피 관리 방식을 유지하고, 건강한 식습관과 생활 습관을 지속하세요."
		},
		"기타사진": {
			displayName: "정수리 또는 탈모 이미지가 아님",
			description: "이 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
			color: "#9C27B0", // 보라색
			icon: "❌",
			recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
		},
		"얼굴금지": {
			displayName: "정수리 또는 탈모 이미지가 아님",
			description: "얼굴이 나온 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
			color: "#9C27B0", // 보라색
			icon: "❌",
			recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
		},
		"'손모양'기타": {
			displayName: "정수리 또는 탈모 이미지가 아님",
			description: "머리외의 다른 신체가 나온 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
			color: "#9C27B0", // 
			icon: "❌",
			recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
		},
		"'손올리지마세요'기타": {
			displayName: "정수리 또는 탈모 이미지가 아님",
			description: "머리외의 다른 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
			color: "#9C27B0", // 
			icon: "❌",
			recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
		},
		"'문서'기타": {
			displayName: "정수리 또는 탈모 이미지가 아님",
			description: "머리외의 다른 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
			color: "#9C27B0", // 
			icon: "❌",
			recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
		}
	};

	const customResult = customResults[className] || {
		displayName: className, color: "#800080", bgColor: "#f0ccff", icon: "❌",
		description: "이 이미지는 탈모 분석을 위한 유효한 사진이 아닙니다.",
		recommendation: "정수리나 탈모가 의심되는 부분의 명확한 사진을 업로드해 주세요."
	};

	// 결과 HTML 생성
	const resultHTML = createResultHTML(customResult, className, percentage);

	if (labelContainer) {
		labelContainer.innerHTML = resultHTML;

		// 버튼 이벤트 리스너 추가
		setTimeout(() => {
			const saveBtn = document.querySelector('.integrated-save-btn');
			const resetBtn = document.querySelector('.integrated-reset-btn');

			if (saveBtn) {
				saveBtn.onclick = () => saveResult();
			}

			if (resetBtn) {
				resetBtn.onclick = () => {
					if (confirm('새로운 분석을 시작하시겠습니까?\n현재 결과가 초기화됩니다.')) {
						resetAnalysis();
					}
				};
			}
		}, 100);
	}

	const validHairLossStages = ["0단계", "1단계", "2단계", "3단계", "4단계"];
	const saveButton = document.querySelector('.integrated-save-btn');

	if (validHairLossStages.includes(className)) {
		if (saveButton) {
			saveButton.style.display = 'inline-block';
		}
		console.log("유효한 탈모 단계 감지됨, 저장 버튼 표시");
	} else {
		if (saveButton) {
			saveButton.style.display = 'none';
		}
		console.log("유효하지 않은 이미지 감지됨, 저장 버튼 숨김");
	}
}

function createResultHTML(customResult, className, percentage) {
	return `
        <div style="background-color: #c0c0c0; border: 2px outset #c0c0c0; padding: 15px; margin: 10px 0; box-shadow: 2px 2px 4px rgba(0,0,0,0.3);">
            <div style="background: linear-gradient(90deg, #000080 0%, #0080ff 100%); color: white; padding: 4px 8px; margin: -15px -15px 10px -15px; font-weight: bold; font-size: 11px;">
                📊 AI 분석 결과
            </div>
            <div style="background: ${customResult.bgColor}; border: 2px inset #c0c0c0; padding: 15px; margin-bottom: 10px; text-align: center;">
                <div style="font-size: 24px; margin-bottom: 5px;">${customResult.icon}</div>
                <div style="color: ${customResult.color}; font-weight: bold; font-size: 14px;">${customResult.displayName}</div>
                <div style="color: #000000; font-size: 12px; margin-top: 5px;">신뢰도: ${percentage}%</div>
                <div style="margin: 10px 0;">
                    <div style="background: #ffffff; border: 2px inset #c0c0c0; height: 20px; position: relative;">
                        <div style="background: ${customResult.color}; height: 100%; width: ${percentage}%; transition: width 0.5s ease;"></div>
                        <div style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: bold; color: #000000;">
                            ${percentage}%
                        </div>
                    </div>
                </div>
            </div>
            <div style="background: #ffffff; border: 2px inset #c0c0c0; padding: 10px; margin-bottom: 10px;">
                <div style="font-weight: bold; color: #000080; margin-bottom: 5px; font-size: 11px;">📋 상세 분석</div>
                <div style="font-size: 11px; color: #000000; line-height: 1.4;">${customResult.description}</div>
            </div>
            <div style="background: #ffffcc; border: 2px inset #c0c0c0; padding: 10px; margin-bottom: 15px;">
                <div style="font-weight: bold; color: #808000; margin-bottom: 5px; font-size: 11px;">💡 권장사항</div>
                <div style="font-size: 11px; color: #000000; line-height: 1.4;">${customResult.recommendation}</div>
            </div>
            <div style="background: #c0c0c0; border: 2px inset #c0c0c0; padding: 10px; text-align: center;">
                <div style="font-weight: bold; color: #000080; margin-bottom: 10px; font-size: 11px;">🔧 다음 작업</div>
                <button type="button" class="integrated-save-btn retro-button" style="margin: 3px; min-width: 90px; font-size: 10px; padding: 6px 12px;">💾 결과 저장</button>
                <button type="button" class="integrated-reset-btn retro-button" style="margin: 3px; min-width: 90px; font-size: 10px; padding: 6px 12px; background-color: #808080;">🔄 다시 분석</button>
            </div>
        </div>
    `;
}


function handleAnalysisError(error, labelContainer) {
	updateSystemStatus('분석 오류 발생', '#ff0000');

	const modelStatusElement = document.getElementById('model-status');
	if (modelStatusElement) {
		modelStatusElement.textContent = '분석 오류';
	}

	if (labelContainer) {
		labelContainer.innerHTML = `
            <div style="background: #c0c0c0; border: 2px inset #c0c0c0; padding: 15px; text-align: center;">
                <div style="color: #ff0000; font-weight: bold; margin-bottom: 10px;">❌ 시스템 오류</div>
                <div style="font-size: 11px; color: #000000;">
                    ${error.message}<br>
                    다시 시도해주세요.
                </div>
            </div>
        `;
	}

	showWin95Alert(`분석 중 오류가 발생했습니다.\n\n${error.message}\n\n다시 시도해 주세요.`, 'error');
}


async function saveResult() {
	console.log(' 결과 저장 시작');

	const predictionData = document.getElementById('predictionJson').value;

	if (!predictionData) {
		showWin95Alert('저장할 결과 데이터가 없습니다.\n먼저 이미지 분석을 수행해주세요.', 'warning');
		return;
	}

	try {
		updateSystemStatus('데이터베이스 저장 중...', '#ff0000');
		showLoadingDialog(true);

		const formData = new FormData();
		const imageFile = document.getElementById('imageInput').files[0];

		if (imageFile) {
			formData.append('files', imageFile);
		}

		formData.append('predictionData', predictionData);

		const userIdInput = document.querySelector('input[name="id"]');
		if (userIdInput) {
			formData.append('id', userIdInput.value);
		}

		const response = await fetch('/hairLossTest/api', {
			method: 'POST',
			body: formData
		});

		if (response.ok) {
			const result = await response.json();
			updateSystemStatus('저장 완료', '#008000');
			showWin95Alert('분석 결과가 성공적으로 저장되었습니다!\n\n저장 완료: ' + new Date().toLocaleString(), 'success');

			setTimeout(() => {
				window.location.href = '/';  // 메인 페이지로 리다이렉트
			}, 3000);

		} else {
			throw new Error(`서버 오류: ${response.status}`);
		}

	} catch (error) {
		console.error('저장 오류:', error);
		updateSystemStatus('저장 실패', '#ff0000');
		showWin95Alert('저장 중 오류가 발생했습니다.\n\n다시 시도해주세요.', 'error');
	} finally {
		showLoadingDialog(false);
	}
}

function resetAnalysis() {
	console.log('🔄 분석 초기화');

	const elements = {
		imageInput: document.getElementById('imageInput'),
		imagePreview: document.getElementById('image-preview'),
		placeholder: document.getElementById('preview-placeholder'),
		analyzeButton: document.getElementById('analyzeButton'),
		submitButton: document.getElementById('submitButton'),
		predictionContainer: document.getElementById('prediction-container'),
		predictionJson: document.getElementById('predictionJson'),
		labelContainer: document.getElementById('label-container')
	};

	if (elements.imageInput) elements.imageInput.value = '';
	if (elements.imagePreview) {
		elements.imagePreview.style.display = 'none';
		elements.imagePreview.src = '';
	}
	if (elements.placeholder) elements.placeholder.style.display = 'block';
	if (elements.analyzeButton) elements.analyzeButton.style.display = 'none';
	if (elements.submitButton) elements.submitButton.style.display = 'none';
	if (elements.predictionContainer) elements.predictionContainer.style.display = 'none';
	if (elements.predictionJson) elements.predictionJson.value = '';
	if (elements.labelContainer) elements.labelContainer.innerHTML = '';

	updateSystemStatus('새로운 분석 준비 완료', '#008000');
}


document.addEventListener('DOMContentLoaded', function() {

	updateSystemStatus('시스템 초기화 중...', '#0000ff');

	// 2초 후 준비 완료 상태로 변경
	setTimeout(() => {
		updateSystemStatus('AI 모델 준비 완료', '#008000');
		console.log('✅ 시스템 준비 완료');
	}, 2000);

});

console.log(' 스크립트 로드 완료');
