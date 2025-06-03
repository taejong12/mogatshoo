function fu_deleteCategory() {
	if (confirm("정말로 이 카테고리를 삭제하시겠습니까?")) {
		let deletePointCategoryForm = document.deletePointCategoryForm;
		let pointCategoryId = deletePointCategoryForm.pointCategoryId.value;

		fetch("/admin/point/category/deleteCheck", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({ pointCategoryId: pointCategoryId })
		})
		.then(response => {
			if (!response.ok) {
				throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
			}
			return response.json();
		})
		.then(data => {
			if (data.hasItems) {
				alert(data.message);
			} else {
				// 상품이 없으면 삭제 진행
				deletePointCategoryForm.method = "post";
				deletePointCategoryForm.action = "/admin/point/category/delete";
				deletePointCategoryForm.submit();
			}
		})
		.catch(error => {
			console.error("카테고리 삭제 확인 중 오류 발생:", error);
			alert("카테고리 삭제 확인 중 문제가 발생했습니다.");
		});
	}
}