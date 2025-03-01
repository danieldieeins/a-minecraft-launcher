function initHover() {
    const discover = document.getElementById("mje-discover-modpacks");

    discover.addEventListener("mouseenter", () => {
        if(!document.getElementById("mje-discover-modpacks-i").classList.contains("bx-spin")) {
            document.getElementById("mje-discover-modpacks-i").classList.add("bx-spin");
        }
        connector('java.sync.discoverHover.on');
    });

    discover.addEventListener("mouseleave", () => {
        if(document.getElementById("mje-discover-modpacks-i").classList.contains("bx-spin")) {
            document.getElementById("mje-discover-modpacks-i").classList.remove("bx-spin");
        }
        connector('java.sync.discoverHover.off');
    });
}

function initSelects(reloadVersion) {
    const gameType = document.getElementById("mje-settings-game");
    const gameVersion = document.getElementById("mje-settings-game-version");
    const modloaderVersion = document.getElementById("mje-settings-modloader-version");

    if(reloadVersion) {
        gameVersion.innerHTML = "";
        modloaderVersion.innerHTML = "";
        connector("java.init.creator.gameVersion."+gameType.value);
    } else {
        modloaderVersion.innerHTML = "";
        connector("java.init.creator.modloaderVersion."+gameType.value+"."+gameVersion.value);
    }
}

function createInstance() {
    const name = document.getElementById("mje-creator-name").value.replaceAll(".","%dot%");
    let version;
    if(document.getElementById("mje-creator-version").value) {
        version = document.getElementById("mje-creator-version").value.replaceAll(".", "%dot%");
    } else {
        version = "1%dot%0%dot%0";
    }
    let description;
    if(document.getElementById("mje-creator-description").value) {
        description = document.getElementById("mje-creator-description").value.replaceAll(".", "%dot%");
    } else {
        description = "No description%dot%%dot%%dot%";
    }
    const gameType = document.getElementById("mje-settings-game").value;
    const gameVersion = document.getElementById("mje-settings-game-version").value.replaceAll(".","%dot%");
    let modloaderVersion;
    if(document.getElementById("mje-settings-modloader-version").value) {
        modloaderVersion = document.getElementById("mje-settings-modloader-version").value.replaceAll(".","%dot%");
    } else {
        modloaderVersion = "";
    }
    if(name&&version&&description&&gameType&&gameVersion) {
        connector("java.init.creator.create."+name+"."+version+"."+description+"."+gameType+"."+gameVersion+"."+modloaderVersion);
    }
}