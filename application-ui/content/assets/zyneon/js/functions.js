let theme = "default.dark";
let app = false;

document.addEventListener('contextmenu',function(e){
    e.preventDefault();
});

document.addEventListener('dragstart', function(e){
    e.preventDefault();
});

function sendNotification(title,text,actions,id) {
    const template = document.getElementById("message-template");
    let card = template.cloneNode(true);
    let now = new Date();
    const date = now.getHours().toString().padStart(2, '0')+":"+now.getMinutes().toString().padStart(2, '0')+" - "+now.getFullYear()+"/"+(now.getMonth() + 1).toString().padStart(2, '0')+"/"+now.getDate().toString().padStart(2, '0');
    if(id!==undefined) {
        card.id = id;
    } else {
        card.id = date.toLowerCase().replaceAll(":","").replaceAll(" - ","").replaceAll("/","");
    }
    card.querySelector("#notification-title").innerText=title;
    card.querySelector("#notification-text").innerHTML=text;
    card.querySelector(".button").onclick=function () {
        callJavaMethod("button.notification.remove."+id);
        document.getElementById(card.id).remove();
    };
    card.querySelector("#notification-actions").innerHTML=actions;
    card.querySelector(".date").innerText=date;
    template.insertAdjacentElement("afterend",card);
}

function openInBrowser(url) {
    if(app) {
        callJavaMethod("browser."+url);
    } else {
        link_(url);
    }
}

function toggleMenu() {
    let menu = document.querySelector(".menu");
    menu.classList.toggle('active');
    let submenu = document.querySelector("#submenu");
    submenu.classList.toggle('active');
}

function activateMenu() {
    if(document.querySelector(".menu")!==null) {
        let menu = document.querySelector(".menu");
        menu.classList.add('active');
    }
    if(document.querySelector("#submenu")!==null) {
        let submenu = document.querySelector("#submenu");
        submenu.classList.remove('active');
    }
}

function deactivateMenu() {
    if(document.querySelector(".menu")!==null) {
        let menu = document.querySelector(".menu");
        menu.classList.remove('active');
    }
    if(document.querySelector("#submenu")!==null) {
        let submenu = document.querySelector("#submenu");
        submenu.classList.add('active');
    }
}

function syncApplication(tab) {
    if(tab==="start") {
        activateMenu();
    } else {
        deactivateMenu();
    }
    callJavaMethod("sync.login");
}

function login(name) {
    checkForWeb();
    document.getElementById("username").innerText = name;
    document.getElementById("profile-picture").src = "https://cravatar.eu/helmhead/"+name+"/64.png";
    document.getElementById("profile-picture").style.display = "inherit";
    document.getElementById("loginlogout").innerText = "Logout";
    document.getElementById("loading").style.display = "none";
}

function logout() {
    checkForWeb();
    document.getElementById("username").innerText = "Not logged in";
    document.getElementById("profile-picture").src = "assets/zyneon/images/steve.png";
    document.getElementById("profile-picture").style.display = "inherit";
    document.getElementById("loginlogout").innerText = "Login";
    document.getElementById("loading").style.display = "none";
}

function checkForWeb() {
    app = true;
    document.getElementById("profile_").style.display = "inherit";
    document.getElementById("instance-button").style.display = "inherit";
    document.getElementById("resource-button").style.display = "inherit";
    document.getElementById("download-button").style.display = "none";
    const zyndex = document.getElementById("zyndex-button");
    if(zyndex) {
        zyndex.style.display = "inherit";
    }
}

function syncTheme() {
    if(localStorage.getItem("theme")!==null) {
        theme = localStorage.getItem("theme");
        if(theme.toLowerCase()==="dark") {
            theme="default.dark";
        } else if(theme.toLowerCase()==="light") {
            theme="default.light";
        } else if(theme.toLowerCase()==="zyneon") {
            theme="default.zyneon";
        }
    }
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.get("theme")!=null) {
        theme = urlParams.get('theme');
    }
    setTheme(theme);
}

function setTheme(newTheme) {
    let root = document.documentElement;
    if (newTheme === "default.light") {
        theme = "default.light";
        root.style.setProperty("--background", '#c7c7c7');
        root.style.setProperty('--background2', '#e0e0e0');
        root.style.setProperty('--background3', '#d3d3d3');
        root.style.setProperty("--background4", "#f6f6f6");
        root.style.setProperty("--background-accent", "#fff");
        root.style.setProperty("--highlight", "#000");
        root.style.setProperty("--color", "#000");
        root.style.setProperty("--color-dim", "#00000085");
        root.style.setProperty("--color-dim-less", "#00000099");
        root.style.setProperty("--inverted", "#fff");
        localStorage.setItem('theme', theme);
        callJavaMethod('button.theme.' + theme);
    } else if (newTheme === "default.zyneon") {
        setTheme("default.dark");
        theme = "default.zyneon";
        root.style.setProperty('--background', '#140c28');
        root.style.setProperty('--background2', '#0d061c');
        root.style.setProperty('--background3', '#120925');
        root.style.setProperty('--background4', '#060112');
        root.style.setProperty('--background-accent', '#050113');
        localStorage.setItem('theme', theme);
        callJavaMethod('button.theme.' + theme);
    } else if (newTheme === "default.dark") {
        theme = "default.dark";
        root.style.setProperty('--background', '#181818');
        root.style.setProperty('--background2', '#101010');
        root.style.setProperty('--background3', '#1a1a1a');
        root.style.setProperty('--background4', '#080808');
        root.style.setProperty('--background-accent', '#000');
        root.style.setProperty("--highlight", "#fff");
        root.style.setProperty("--color", "#fff");
        root.style.setProperty("--color-dim", "#ffffff60");
        root.style.setProperty("--color-dim-less", "#ffffff90");
        root.style.setProperty("--inverted", "#000");
        localStorage.setItem('theme', theme);
        callJavaMethod('button.theme.' + theme);
    } else if (newTheme === "custom") {
        document.getElementById("shared-css").href = "assets/zyneon/css/themes/custom/shared.css";
        document.getElementById("sub-css").href = "../assets/zyneon/css/themes/custom/shared.css";
        let page = document.getElementById("page-css").href;
        page = page.replace("default","custom");
        document.getElementById("page-css").href = page;
        localStorage.setItem('theme', newTheme);
        callJavaMethod("button.theme.custom");
    } else {
        callJavaMethod("sync.theme."+newTheme);
    }
}

function callJavaMethod(message) {
    console.log("[Launcher-Bridge] "+message);
}

function link(url) {
    window.open(url, "_self");
}

function link_(url) {
    window.open(url, "_blank");
}

function addVersionsToMinecraftSelect(id) {
    callJavaMethod("sync.select.minecraft."+id);
}

function addGameTypesToSelect(id) {
    callJavaMethod("sync.select.gametypes."+id);
}

function addToSelect(selectID,value,name) {
    const select = document.getElementById(selectID);
    const option = document.createElement("option");
    option.text = name;
    option.value = value;
    select.add(option);
}

function message(m) {
    document.getElementById("overlay").style.display = "inherit";
    document.getElementById("message").innerHTML = m;
}

function unmessage() {
    document.getElementById("overlay").style.display = "none";
}