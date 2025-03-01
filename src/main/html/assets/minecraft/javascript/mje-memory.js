let id = "global;"

function initMemory() {
    const urlParams = new URLSearchParams(location.search);
    if(urlParams.has("id")) {
        id = urlParams.get("id");
    }
    document.getElementById("confirm-button").onclick = function () {
        connector('java.settings.'+id+'.memory.'+document.getElementById('slider').value);
    }
    if(urlParams.get("min")!=null&&urlParams.get("max")!=null&&urlParams.get("value")!=null) {
        const min = parseInt(urlParams.get("min"));
        const max = parseInt(urlParams.get("max"));
        const value = parseInt(urlParams.get("value"));
        const slider = document.getElementById("slider");
        const input = document.getElementById("value");

        slider.min = min;
        slider.max = max;
        slider.value = value;

        document.getElementById("min").innerText = min+"mb";
        document.getElementById("max").innerText = max+"mb";
        document.getElementById("value").value = value;
        input.style.width = input.value.toString().length * 9 + 'px';
        input.min = min; input.max = max;

        input.addEventListener("input", () => {
            input.style.width = input.value.toString().length * 9 + 'px';
            slider.value = input.value;
            if(input.value>max) {
                input.value = max;
            } else if(input.value<min) {
                input.value = min;
            }
        });

        slider.addEventListener('input', () => {
            input.value = slider.value;
            input.style.width = input.value.toString().length * 9 + 'px';
        });
    }
}