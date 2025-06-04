function fu_pointItemBuy() {
	let pointItemBuyForm = document.pointItemBuyForm;

	let pointItemId = document.getElementById('pointItemId');
	// 구매하기 버튼 클릭 시 
	// 1.보유하고 있는 포인트보다 높을 시 보유 포인트가 부족합니다.
	// 2.재고가 없는 경우 -> 현재 재고가 없습니다.
	// 필요한 것 1.유저아이디(포인트조회), 2.상품아이디(재고조회)

	fetch("/point/shop/buyCheck", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({ pointItemId: pointItemId })
	})
	.then(response => {
		if (!response.ok) {
			throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
		}
		return response.json();
	})
	.then(data => {
		console.log(data);
		if (data.buyCheck) {
			pointItemBuyForm.method = 'post';
			pointItemBuyForm.action = '/point/shop/buy';
			pointItemBuyForm.submit();
		} else{
			alert(data.msg);
		}
	})
	.catch(error => {
		console.error("포인트구매확인 중 오류 발생:", error);
	});

}