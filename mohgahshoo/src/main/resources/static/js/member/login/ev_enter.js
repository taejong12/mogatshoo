document.addEventListener('DOMContentLoaded', function () {
	let form = document.loginForm;

    form.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            fu_memberLogin();
        }
    });
});