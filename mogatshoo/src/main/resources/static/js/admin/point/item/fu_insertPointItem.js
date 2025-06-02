function fu_insertPointItem() {
	let insertPointItemForm = document.insertPointItemForm;

	let name = document.getElementById('pointItemName');
	let description = document.getElementById('pointItemDescription');
	let price = document.getElementById('pointItemPrice');
	let stock = document.getElementById('pointItemStock');
	let nameWarnMsg = document.getElementById('nameWarnMsg');
	let descriptionWarnMsg = document.getElementById('descriptionWarnMsg');
	let stockWarnMsg = document.getElementById('stockWarnMsg');
	let priceWarnMsg = document.getElementById('priceWarnMsg');

	let nameVal = name.value.trim();
	let descriptionVal = description.value.trim();
	let priceVal = price.value.trim();
	let stockVal = stock.value.trim();

	nameWarnMsg.textContent = '';
	descriptionWarnMsg.textContent = '';
	priceWarnMsg.textContent = '';
	stockWarnMsg.textContent = '';
	
	nameWarnMsg.style.display = 'none';
	descriptionWarnMsg.style.display = 'none';
	priceWarnMsg.style.display = 'none';
	stockWarnMsg.style.display = 'none';

	let nameCheck = false;
	let descriptionCheck = false;
	let priceCheck = false;
	let stockCheck = false;

	if (nameVal == '' || nameVal.length == 0) {
		name.focus();
		showWarning(nameWarnMsg, '이름을 입력해주세요.');
		nameCheck = false;
	} else {
		nameCheck = true;
	}

	if (descriptionVal == '' || descriptionVal.length == 0) {
		description.focus();
		showWarning(descriptionWarnMsg, '유의사항을 입력해주세요.');
		descriptionCheck = false;
	} else {
		descriptionCheck = true;
	}

	if (priceVal === '' || isNaN(priceVal) || Number(priceVal) <= 0) {
		price.focus();
		showWarning(priceWarnMsg, '가격은 0보다 큰 숫자여야 합니다.');
		priceCheck = false;
	} else {
		priceCheck = true;
	}

	if (stockVal === '' || isNaN(stockVal) || Number(stockVal) <= 0) {
		stock.focus();
		showWarning(stockWarnMsg, '재고는 0보다 큰 숫자여야 합니다.');
		stockCheck = false;
	} else {
		stockCheck = true;
	}


	if (nameCheck && descriptionCheck && priceCheck && stockCheck) {
		insertPointItemForm.method = "post";
		insertPointItemForm.action = "/admin/point/item/insert";
		insertPointItemForm.submit();
	}
}

// 경고메시지 함수
function showWarning(warnElement, message) {
	warnElement.textContent = message;
	warnElement.style.color = 'rgb(255, 107, 107)';
	warnElement.style.fontSize = '0.8em';
	warnElement.style.padding = '.375rem .75rem';
	warnElement.style.display = 'inline';
}