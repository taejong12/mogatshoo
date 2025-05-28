document.addEventListener('DOMContentLoaded', function() {
	
	let totalAgreeCheck = document.getElementById('totalAgreeCheck');
	let agreeIntegration = document.getElementById('agreeIntegration');
	let agreeInfo = document.getElementById('agreeInfo');
	let agreeChecks = [agreeIntegration, agreeInfo];
	
	totalAgreeCheck.addEventListener('change', function(){
		let isChecked = totalAgreeCheck.checked;
		agreeChecks.forEach(chk => {
			chk.checked = isChecked;
			chk.value = chk.checked ? 'Y' : 'N';
		});
	});
	
	agreeChecks.forEach(chk => {
		chk.addEventListener('change', function(){
			let allChecked = agreeChecks.every(item => item.checked);
			totalAgreeCheck.checked = allChecked;
			chk.value = chk.checked ? 'Y' : 'N';
		});
	})
})