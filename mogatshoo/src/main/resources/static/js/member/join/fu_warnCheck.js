// 경고 문구 초기화
function fu_warnCheck(){
	let idWarn = document.querySelector('.warn-div.id');
	let pwdWarn = document.querySelector('.warn-div.pwd');
	let pwdCheckWarn = document.querySelector('.warn-div.pwdCheck');
	let nameWarn = document.querySelector('.warn-div.name');
	let telWarn = document.querySelector('.warn-div.tel');
	let birthWarn = document.querySelector('.warn-div.birth');
	let emailWarn = document.querySelector('.warn-div.email');
	let emailCheckWarn = document.querySelector('.warn-div.emailCheck');
	let infoCheckWarn = document.querySelector('.warn-div.infoCheck');
	let authWarn = document.querySelector('.warn-div.auth');
	let emailAuthWarn = document.querySelector('.warn-div.emailAuth');
	
	if(idWarn){
		idWarn.closest('.input-wrap').remove();
	}
	
	if(pwdWarn){
		pwdWarn.closest('.input-wrap').remove();
	}
	
	if(pwdCheckWarn){
		pwdCheckWarn.closest('.input-wrap').remove();
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
	
	if(emailCheckWarn){
		emailCheckWarn.closest('.input-wrap').remove();
	}
	
	if(infoCheckWarn){
		infoCheckWarn.closest('.input-wrap').remove();
	}
	
	if(authWarn){
		authWarn.closest('.input-wrap').remove();
	}
	
	if(emailAuthWarn){
		emailAuthWarn.closest('.input-wrap').remove();
	}
}