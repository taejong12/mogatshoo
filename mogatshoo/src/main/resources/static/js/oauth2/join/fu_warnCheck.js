// 경고 문구 초기화
function fu_warnCheck(){
	let idWarn = document.querySelector('.warn-div.id');
	let nameWarn = document.querySelector('.warn-div.name');
	let telWarn = document.querySelector('.warn-div.tel');
	let birthWarn = document.querySelector('.warn-div.birth');
	let emailWarn = document.querySelector('.warn-div.email');
	let infoCheckWarn = document.querySelector('.warn-div.infoCheck');
	
	if(idWarn){
		idWarn.closest('.input-wrap').remove();
	}
	
	if(nameWarn){
		nameWarn.closest('.input-wrap').remove();
	}
	
	if(telWarn){
		telWarn.closest('.input-wrap').remove();
	}
	
	if(birthWarn){
		birthWarn.closest('.input-wrap').remove();
	}
	
	if(emailWarn){
		emailWarn.closest('.input-wrap').remove();
	}
	
	if(infoCheckWarn){
		infoCheckWarn.closest('.input-wrap').remove();
	}
}