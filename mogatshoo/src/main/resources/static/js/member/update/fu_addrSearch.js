function fu_addrSearch(){
    new daum.Postcode({
        oncomplete: function(data) {
        	document.getElementById('memberZipcode').value = data.zonecode;
            document.getElementById("memberAddress1").value = data.roadAddress;
        }
    }).open();
}