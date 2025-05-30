function fu_memberAgree(){
	let agreeForm = document.agreeForm;
	
	let agreeVali = fu_agreeVali();
	
	if(agreeVali){
		agreeForm.method = "post";
		agreeForm.action = "/member/agree";
		agreeForm.submit();
	}
}