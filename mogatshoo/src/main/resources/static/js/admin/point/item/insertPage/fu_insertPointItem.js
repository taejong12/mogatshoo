function fu_insertPointItem() {
	let insertPointItemForm = document.insertPointItemForm;

	let name = document.getElementById('pointItemName');
	let description = document.getElementById('pointItemDescription');
	let price = document.getElementById('pointItemPrice');
	let stock = document.getElementById('pointItemStock');
	let category = document.getElementById('pointCategoryId');

	let nameWarnMsg = document.getElementById('nameWarnMsg');
	let descriptionWarnMsg = document.getElementById('descriptionWarnMsg');
	let stockWarnMsg = document.getElementById('stockWarnMsg');
	let priceWarnMsg = document.getElementById('priceWarnMsg');
	let categoryWarnMsg = document.getElementById('categoryWarnMsg');

	let nameVal = name.value.trim();
	let descriptionVal = description.value.trim();
	let priceVal = price.value.trim();
	let stockVal = stock.value.trim();
	let categoryVal = category.value.trim();

	nameWarnMsg.textContent = '';
	descriptionWarnMsg.textContent = '';
	priceWarnMsg.textContent = '';
	stockWarnMsg.textContent = '';
	categoryWarnMsg.textContent = '';

	nameWarnMsg.style.display = 'none';
	descriptionWarnMsg.style.display = 'none';
	priceWarnMsg.style.display = 'none';
	stockWarnMsg.style.display = 'none';
	categoryWarnMsg.style.display = 'none';

	let nameCheck = false;
	let descriptionCheck = false;
	let priceCheck = false;
	let stockCheck = false;
	let categoryCheck = false;

	console.log(categoryCount);


	if (categoryCount == 0) {
		category.focus();
		showWarning(categoryWarnMsg, '카테고리가 존재하지 않습니다. 먼저 카테고리를 생성해주세요.');
		categoryCheck = false;
	} else if (categoryVal == '' || categoryVal.length == 0) {
		category.focus();
		showWarning(categoryWarnMsg, '카테고리를 선택해주세요.');
		categoryCheck = false;
	} else {
		categoryCheck = true;
	}


	if (stockVal === '' || isNaN(stockVal) || Number(stockVal) <= 0) {
		stock.focus();
		showWarning(stockWarnMsg, '재고는 0보다 큰 숫자여야 합니다.');
		stockCheck = false;
	} else {
		stockCheck = true;
	}

	if (priceVal === '' || isNaN(priceVal) || Number(priceVal) <= 0) {
		price.focus();
		showWarning(priceWarnMsg, '가격은 0보다 큰 숫자여야 합니다.');
		priceCheck = false;
	} else {
		priceCheck = true;
	}

	if (descriptionVal == '' || descriptionVal.length == 0) {
		description.focus();
		showWarning(descriptionWarnMsg, '유의사항을 입력해주세요.');
		descriptionCheck = false;
	} else {
		descriptionCheck = true;
	}


	if (nameVal == '' || nameVal.length == 0) {
		name.focus();
		showWarning(nameWarnMsg, '이름을 입력해주세요.');
		nameCheck = false;
	} else {
		nameCheck = true;
	}

	if (nameCheck && descriptionCheck && priceCheck
		&& stockCheck && categoryCheck) {
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