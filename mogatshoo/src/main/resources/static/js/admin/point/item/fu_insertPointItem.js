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
		nameWarnMsg.textContent = '이름을 입력해주세요.';
		nameWarnMsg.style.color = 'rgb(255, 107, 107)';
		nameWarnMsg.style.fontSize = '0.8em';
		nameWarnMsg.style.padding = '.375rem .75rem';
		nameWarnMsg.style.display = 'inline';
		nameCheck = false;
	} else {
		nameCheck = true;
	}

	if (descriptionVal == '' || descriptionVal.length == 0) {
		description.focus();
		descriptionWarnMsg.textContent = '유의사항을 입력해주세요.';
		descriptionWarnMsg.style.color = 'rgb(255, 107, 107)';
		descriptionWarnMsg.style.fontSize = '0.8em';
		descriptionWarnMsg.style.padding = '.375rem .75rem';
		descriptionWarnMsg.style.display = 'inline';
		descriptionCheck = false;
	} else {
		descriptionCheck = true;
	}

	if (priceVal === '' || isNaN(priceVal) || Number(priceVal) <= 0) {
		price.focus();
		priceWarnMsg.textContent = '가격은 0보다 큰 숫자여야 합니다.';
		priceWarnMsg.style.color = 'rgb(255, 107, 107)';
		priceWarnMsg.style.fontSize = '0.8em';
		priceWarnMsg.style.padding = '.375rem .75rem';
		priceWarnMsg.style.display = 'inline';
		priceCheck = false;
	} else {
		priceCheck = true;
	}

	if (stockVal === '' || isNaN(stockVal) || Number(stockVal) <= 0) {
		stock.focus();
		stockWarnMsg.textContent = '재고는 0보다 큰 숫자여야 합니다.';
		stockWarnMsg.style.color = 'rgb(255, 107, 107)';
		stockWarnMsg.style.fontSize = '0.8em';
		stockWarnMsg.style.padding = '.375rem .75rem';
		stockWarnMsg.style.display = 'inline';
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
