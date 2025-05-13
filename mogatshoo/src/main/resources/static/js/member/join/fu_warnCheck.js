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
	let genderWarn = document.querySelector('.warn-div.gender');
	let zipCodeWarn = document.querySelector('.warn-div.zipCode');
	let addr1Warn = document.querySelector('.warn-div.addr1');
	let addr2Warn = document.querySelector('.warn-div.addr2');
	let nickNameWarn = document.querySelector('.warn-div.nickName');
	
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
	
	if(genderWarn){
		genderWarn.closest('.input-wrap').remove();
	}
	
	if(zipCodeWarn){
		zipCodeWarn.closest('.input-wrap').remove();
	}
	
	if(addr1Warn){
		addr1Warn.closest('.input-wrap').remove();
	}
	
	if(addr2Warn){
		addr2Warn.closest('.input-wrap').remove();
	}
	
	if(nickNameWarn){
		nickNameWarn.closest('.input-wrap').remove();
	}
}