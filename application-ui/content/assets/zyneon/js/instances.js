let highlighted = document.getElementById("top");

document.getElementById('values').addEventListener('submit', function(event) {
    event.preventDefault();
});

document.getElementById('settings').addEventListener('submit', function(event) {
    event.preventDefault();
});

function highlight(id) {
    const element = document.getElementById(id);
    const newHighlight = element.querySelector("div");
    if(newHighlight) {
        if(highlighted !== null) {
            if(highlighted !== undefined) {
                highlighted.classList.remove("active");
            }
        }
        highlighted = newHighlight;
        newHighlight.classList.add("active");
    }
}

function syncInstanceList() {
    callJavaMethod("sync.instances.list");
}

function loadTab(tab,editable) {
    const urlParams = new URLSearchParams(window.location.search);
    let tab_ = "zyneon::overview";
    if(urlParams.get("tab")!=null) {
        tab_ = urlParams.get("tab");
    } else {
        if(tab!==null) {
            if(tab!==undefined) {
                if(tab!=="null") {
                    tab_ = tab;
                }
            }
        }
    }
    if(document.getElementById(tab)!==null) {
        syncInstance(tab_,editable);
    }
}

function addInstanceToList(id,name,png,editable) {
    const base = document.getElementById("list-template");
    const instance = base.cloneNode(true);
    instance.id = id;
    base.parentNode.insertBefore(instance, base);
    const a = instance.querySelector("a");
    if (a) {
        a.onclick = function () {
            syncInstance(id,editable);
        }
    }
    const i = instance.querySelector("i");
    if(i) {
        if(!id.includes("official/")) {
            i.style.display = "none";
        }
    }
    const span = instance.querySelector("span");
    if (span) {
        span.innerText = name;
    }
    if (png !== undefined) {
        if (png !== "") {
            const img = instance.querySelector("img");
            if (img) {
                img.src = png;
            }
        }
    }
}

function syncInstance(id,editableBool) {
    let editable = false;
    if(editableBool) {
        if (editableBool === true||editableBool === "true") {
            editable = true;
        }
    }

    closeSettings();
    document.getElementById("instance-adder").style.display = "none";
    document.getElementById("instance-view").style.display = "inherit";
    document.getElementById("open-resourcepacks").onclick = function () { callJavaMethod("button.resourcepacks."+id); };
    document.getElementById("open-screenshots").onclick = function () { callJavaMethod("button.screenshots."+id); };
    document.getElementById("open-shaderpacks").onclick = function () { callJavaMethod("button.shaders."+id); };
    document.getElementById("open-worlds").onclick = function () { callJavaMethod("button.worlds."+id); };
    document.getElementById("open-instance").onclick = function () { callJavaMethod("button.folder."+id); };
    document.getElementById("open-mods").onclick = function () { callJavaMethod("button.mods."+id); };

    if(!id.includes("official/")||editable) {
        makeEditable(id);
    } else {
        document.getElementById("check").style.display = "inherit";
        document.getElementById("open-instance").style.display = "none";
        document.getElementById("content").style.display = "none";
        document.getElementById("open-mods").style.display = "none";
        document.getElementById("local-settings").style.display = "none";
        document.getElementById("local-appearance").style.display = "none";
        document.getElementById("check").style.display = "inherit";
        document.getElementById("folder").onclick = function () {};
    }

    document.getElementById("configure-memory").onclick = function () { callJavaMethod("button.settings."+id); };
    document.getElementById("delete-instance").onclick = function () { callJavaMethod("button.delete."+id); };

    highlight(id);
    callJavaMethod("button.instance." + id);
}

function makeEditable(id) {
    document.getElementById("content").style.display = "inherit";
    document.getElementById("open-instance").style.display = "inherit";
    document.getElementById("open-mods").style.display = "inherit";
    document.getElementById("local-settings").style.display = "inherit";
    document.getElementById("local-appearance").style.display = "inherit";
    document.getElementById("folder").onclick = function () { callJavaMethod("button.folder."+id); };
}

function syncTitle(name,png) {
    document.getElementById("title").innerText = name;
    if(png !== undefined) {
        if(png !== "") {
            document.getElementById("icon").src = png;
        } else {
            document.getElementById("icon").src = "assets/zyneon/images/instances/default.png";
        }
    } else {
        document.getElementById("icon").src = "assets/zyneon/images/instances/default.png";
    }
}

function syncLogo(png) {
    if(png !== undefined) {
        if(png !== "") {
            document.getElementById("logo").src = png;
        } else {
            document.getElementById("logo").src = "assets/zyneon/images/instances/default-logo.png";
        }
    } else {
        document.getElementById("logo").src = "assets/zyneon/images/instances/default-logo.png";
    }
}

function syncDescription(description) {
    document.getElementById("description").innerText = description;
}

function syncBackground(png) {
    if(png !== undefined) {
        if(png !== "") {
            document.getElementById('instance-view').style.backgroundImage="url("+png+")";
        } else {
            document.getElementById('instance-view').style.backgroundImage="none";
        }
    } else {
        document.getElementById('instance-view').style.backgroundImage="none";
    }
}

function syncDock(id,version,minecraft,modloader,mlversion) {
    launchDefault();
    document.getElementById("version").innerText = version;
    document.getElementById("minecraft").innerText = minecraft;
    document.getElementById("modloader").innerText = modloader;
    document.getElementById("mlversion").innerText = mlversion;
    document.getElementById("launch").onclick = function () {
        callJavaMethod("button.start."+id);
    };
}

function launchUpdate() {
    document.getElementById("launch").innerHTML = "<i class='bx bx-loader-alt bx-spin bx-rotate-90' ></i> UPDATING";
}

function launchStarted() {
    document.getElementById("launch").innerHTML = "<i class='bx bx-check'></i> STARTED";
}

function launchDefault() {
    document.getElementById("launch").innerHTML = "<i class='bx bxs-rocket'></i> LAUNCH";
}

function syncSettings(id,ram,name,version,description,minecraft,modloader,mlversion,icon,logo,background) {
    document.getElementById("settings-name").value = name;
    document.getElementById("settings-version").value = version;
    document.getElementById("settings-description").value = description;
    document.getElementById("memory-int").innerText = ram;
    document.getElementById("memory-int").style.display = "inline";
    document.getElementById("settings-save").onclick = function () { updateInstance(id); };
    document.getElementById("content").onclick = function () {
        if(modloader==="Quilt") {
            link("resources.html?s=modrinth&t="+modloader.toLowerCase()+"&v="+minecraft+"&d=forge,neoforge&i="+id);
        } else if(modloader==="NeoForge") {
            link("resources.html?s=modrinth&t="+modloader.toLowerCase()+"&v="+minecraft+"&d=fabric,quilt&i="+id);
        } else if(modloader==="Fabric") {
            link("resources.html?s=modrinth&t="+modloader.toLowerCase()+"&v="+minecraft+"&d=forge,quilt,neoforge&i="+id);
        } else if(modloader==="Forge") {
            link("resources.html?s=modrinth&t="+modloader.toLowerCase()+"&v="+minecraft+"&d=fabric,neoforge,quilt&i="+id);
        } else {
            showSettings();
        }
    };
    document.getElementById("configure-icon").onclick = function () { callJavaMethod("button.change.icon."+id); };
    document.getElementById("configure-logo").onclick = function () { callJavaMethod("button.change.logo."+id); };
    document.getElementById("configure-background").onclick = function () { callJavaMethod("button.change.background."+id); };
    if(icon !== undefined) {
        if(icon !== "") {
            document.getElementById("show-icon").style.display = "inline";
            document.getElementById("show-icon").onclick = function () { callJavaMethod("button.icon."+id); };
        }
    }
    if(logo !== undefined) {
        if(logo !== "") {
            document.getElementById("show-logo").style.display = "inline";
            document.getElementById("show-logo").onclick = function () { callJavaMethod("button.logo."+id); };
        }
    }
    if(background !== undefined) {
        if(background !== "") {
            document.getElementById("show-background").style.display = "inline";
            document.getElementById("show-background").onclick = function () { callJavaMethod("button.background."+id); };
        }
    }
}

function showSettings() {
    document.getElementById("sidebyside").style.display = "none";
    document.getElementById("instance-settings").style.display = "inherit";
    document.getElementById("configure-instance").innerText = "Close settings";
    document.getElementById("management").onclick = function () {
        closeSettings();
    };
}

function closeSettings() {
    document.getElementById("instance-settings").style.display = "none";
    document.getElementById("sidebyside").style.display = "inherit";
    document.getElementById("configure-instance").innerText = "Settings";
    document.getElementById("management").onclick = function () {
        showSettings();
    };
}

function toggleSettings() {
    if(document.getElementById("instance-settings").style.display === "none") {
        showSettings();
    } else {
        closeSettings();
    }
}

function addInstance() {
    document.getElementById("instance-view").style.display = "none";
    document.getElementById("instance-adder").style.display = "inherit";
}

function installZyneonPlus() {
    callJavaMethod("button.install.official/zyneonplus/"+document.getElementById("zyneonplus-version").value);
}

function installInstanceID() {
    id = document.getElementById("id").value;
    callJavaMethod('button.install.'+id);
}

function validateInstanceCreator() {
    const instanceCreator = document.getElementById('values');
    if (instanceCreator.checkValidity()) {
        const name = document.getElementById('creator-name');
        const version = document.getElementById('creator-version');
        const minecraft = document.getElementById('creator-minecraft');
        const modloader = document.getElementById('creator-modloader');
        const mlversion = document.getElementById('creator-mlversion');

        const n = name.value;
        const v = version.value;
        const m = minecraft.value;
        const l = modloader.value;
        let k;

            if(mlversion.value) {
                callJavaMethod("test");
                k = mlversion.value;
                callJavaMethod("test: "+k);
            } else {
                k = "";
            }


        const fN = n.replace(/\./g, "%DOT%");
        const fV = v.replace(/\./g, "%DOT%");
        const fM = m.replace(/\./g, "%DOT%");
        const fL = l.replace(/\./g, "");
        const fK = k.replace(/\./g, "%DOT%");

        callJavaMethod('button.creator.create.'+fN+'.'+fV+'.'+fM+'.'+fL+'.'+fK);
    }
    return false;
}

function updateInstance(id) {
    const instanceCreator = document.getElementById('settings');
    if (instanceCreator.checkValidity()) {
        const name = document.getElementById('settings-name');
        const version = document.getElementById('settings-version');
        const minecraft = document.getElementById('settings-minecraft');
        const modloader = document.getElementById('settings-modloader');
        const mlversion = document.getElementById('settings-mlversion');
        const description = document.getElementById('settings-description');

        const n = name.value;
        const v = version.value;
        let m = minecraft.value;
        let l = modloader.value;
        let k;
        const d = description.value;
        if(mlversion.value!=null) {
            k = mlversion.value;
        } else {
            k = "";
        }

        if(l==="nothing") {
            m = document.getElementById("minecraft").innerText;
            l = document.getElementById("modloader").innerText;
            k = document.getElementById("mlversion").innerText;
        }

        const fN = n.replace(/\./g, "%DOT%");
        const fV = v.replace(/\./g, "%DOT%");
        const fM = m.replace(/\./g, "%DOT%");
        const dS = d.replace(/\./g, "%DOT%");
        const fL = l.replace(/\./g, "");
        const fK = k.replace(/\./g, "%DOT%");

        callJavaMethod('button.creator.update.'+id+'.'+fN+'.'+fV+'.'+fM+'.'+fL+'.'+fK+'.'+dS);
    }
    return false;
}

function updateCreator(version) {
    const type = document.getElementById("creator-modloader").value.toLowerCase();
    const versionSelect = document.getElementById("creator-minecraft");
    const mlversionSelect = document.getElementById("creator-mlversion");
    if(version) {
        mlversionSelect.selectedIndex = -1;
        mlversionSelect.innerHTML = "";
        callJavaMethod("sync.creator-version."+type+"."+versionSelect.value);
    } else {
        versionSelect.selectedIndex = -1;
        versionSelect.innerHTML = "";
        mlversionSelect.selectedIndex = -1;
        mlversionSelect.innerHTML = "";
        if (type !== "vanilla" && type !== "snapshots") {
            document.getElementById("creator-modloader-version").style.display = "inherit";
            document.getElementById("creator-modloader-version2").style.display = "none";
        } else {
            document.getElementById("creator-modloader-version").style.display = "none";
            document.getElementById("creator-modloader-version2").style.display = "inherit";
        }
        callJavaMethod("sync.creator." + type);
    }
}

function updateUpdater(version) {
    const type = document.getElementById("settings-modloader").value.toLowerCase();
    const versionSelect = document.getElementById("settings-minecraft");
    const mlversionSelect = document.getElementById("settings-mlversion");
    if(version) {
        mlversionSelect.selectedIndex = -1;
        mlversionSelect.innerHTML = "";
        callJavaMethod("sync.updater-version."+type+"."+versionSelect.value);
    } else {
        versionSelect.selectedIndex = -1;
        versionSelect.innerHTML = "";
        mlversionSelect.selectedIndex = -1;
        mlversionSelect.innerHTML = "";
        callJavaMethod("sync.updater." + type);
    }
}