function enableMicrosoftLogin() {
    const button = document.getElementById("mje-ms-login-button");
    if(!button.classList.contains("active")) {
        button.classList.add("active");
    }
    button.onclick = function () {
        connector("async.java.auth.login");
    };
    if(!document.getElementById("mje-back-to-select").classList.contains("active")) {
        document.getElementById("mje-back-to-select").classList.add("active");
    }
}

function disableMicrosoftLogin() {
    const button = document.getElementById("mje-ms-login-button");
    if(button.classList.contains("active")) {
        button.classList.remove("active");
    }
    button.onclick = function () {};
    if(!document.getElementById("mje-back-to-select").classList.contains("active")) {
        document.getElementById("mje-back-to-select").classList.add("active");
    }
}

function initLogin() {
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.get("enable")) {
        if(urlParams.get("enable")==="true") {
            enableMicrosoftLogin();
            return;
        }
    }
    disableMicrosoftLogin();
    connector("async.java.init.auth.login");
    setInterval(refresh, 5000);
}

function refresh() {
    connector("async.java.init.auth.login");
}

function backToSelect() {
    if(document.getElementById("mje-back-to-select").classList.contains("active")) {
        connector("async.java.init.library.select");
    }
}