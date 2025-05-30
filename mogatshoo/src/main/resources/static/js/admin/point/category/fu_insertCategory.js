function fu_insertCategory() {
	let categoryForm = document.categoryForm;
	let name = document.getElementById('pointCategoryName');
	let sortOrder = document.getElementById('pointCategorySortOrder');
	let nameWarnMsg = document.getElementById('nameWarnMsg');
	let sortWarnMsg = document.getElementById('sortWarnMsg');
	let nameVal = name.value.trim();
	let sortOrderVal = sortOrder.value.trim();

	nameWarnMsg.textContent = '';
	sortWarnMsg.textContent = '';
	nameWarnMsg.style.display = 'none';
	sortWarnMsg.style.display = 'none';

	let nameCheck = false;
	let sortOrderCheck = false;

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

	if (sortOrderVal == '' || sortOrderVal.length == 0) {
		sortOrder.focus();
		sortWarnMsg.textContent = '정렬순서를 입력해주세요.';
		sortWarnMsg.style.color = 'rgb(255, 107, 107)';
		sortWarnMsg.style.fontSize = '0.8em';
		sortWarnMsg.style.padding = '.375rem .75rem';
		sortWarnMsg.style.display = 'inline';
		sortOrderCheck = false;
	} else {
		sortOrderCheck = true;
	}

	if (nameCheck && sortOrderCheck) {
		categoryForm.method = "post";
		categoryForm.action = "/admin/point/category/insert";
		categoryForm.submit();
	}
}