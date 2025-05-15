function fu_addr2Vali(){
	
	let addr2Input = document.getElementById('memberAddress2');
	let addr2WarnMsg = document.getElementById('addr2WarnMsg');
	
	addr2WarnMsg.textContent = '';
	
	if(addr2Input.value == "" || addr2Input.value.length == 0){
		addr2Input.focus();
		addr2WarnMsg.textContent = '기본주소를 입력해주세요.';
		return false;
	} else {
		return true;
	}
}