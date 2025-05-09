function syncMode(bool) {
    if(bool!=null) {
        if(bool.toString().toLowerCase()==="true") {
            app = true;
            return;
        } else if(bool.toString().toLowerCase()==="false") {
            app = false;
            return;
        }
    }
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.get("in_app")!=null) {
        if (urlParams.get("in_app") === "1") {
            app = true;
        }
    }
}