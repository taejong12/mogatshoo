function fu_updateCategory() {
	let updateCategoryForm = document.updateCategoryForm;

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

	if (sortOrderVal == '' || sortOrderVal.length == 0) {
		sortOrder.focus();
		fu_showWarning(sortWarnMsg, '정렬순서를 입력해주세요.');
		sortOrderCheck = false;
	} else {
		sortOrderCheck = true;
	}

	if (nameVal == '' || nameVal.length == 0) {
		name.focus();
		fu_showWarning(nameWarnMsg, '이름을 입력해주세요.');
		nameCheck = false;
	} else {
		nameCheck = true;
	}

	if (nameCheck && sortOrderCheck) {
		updateCategoryForm.method = "post";
		updateCategoryForm.action = "/admin/point/category/update";
		updateCategoryForm.submit();
	}
}