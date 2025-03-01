function initMjeSettings() {
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.get("n")!=null) {
        document.getElementById("mje-name").innerText = urlParams.get("n");
    }
    if(urlParams.get("v")!=null) {
        document.getElementById("mje-version").innerText = urlParams.get("v");
    }
    if(urlParams.get("a")!=null) {
        document.getElementById("mje-authors").innerText = urlParams.get("a");
    }
}