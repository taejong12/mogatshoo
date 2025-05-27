document.addEventListener('DOMContentLoaded', function() {
	let memberName = document.getElementById('memberName');
	let memberTel = document.getElementById('memberTel');
	let memberBirth = document.getElementById('memberBirth');
	
	if(memberName){
		memberName.addEventListener('input', function() {
			let nameWarn = document.querySelector('.warn-div.name');
			if(nameWarn){
				nameWarn.closest('.input-wrap').remove();
			}
		});
	}
	
	if(memberTel){
		memberTel.addEventListener('input', function() {
			let telWarn = document.querySelector('.warn-div.tel');
			if(telWarn){
				telWarn.closest('.input-wrap').remove();
			}
		});
	}
	
	if(memberBirth){
		memberBirth.addEventListener('input', function() {
			let birthWarn = document.querySelector('.warn-div.birth');
			if(birthWarn){
				birthWarn.closest('.input-wrap').remove();
			}
		});
	}
});