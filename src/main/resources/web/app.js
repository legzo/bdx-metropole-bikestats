document.addEventListener("DOMContentLoaded", function() {

    let url = document.location.href

    let match = url.match(/date=([0-9]{4}-[0-9]{2}-[0-9]{2})/i);
    let date = (match && match[1]) || "2021-02-01";

    fetch("/api/raw-data?date=" + date)
        .then(response => response.json())
        .then(data => drawChartsFromData(date, data))
        .then(data => console.log(data));
});

function drawChartsFromData(date, dataForMeters) {
    document.getElementById("logo").textContent += " > " + date
    for (let dataForMeter of dataForMeters) {
        let series = [ dataForMeter.times, dataForMeter.values ]
        makeChart(dataForMeter.id, series)
    }
}

let mooSync = uPlot.sync("moo");

const cursorOpts = {
    lock: true,
    focus: {
        prox: 16,
    },
    sync: {
        key: mooSync.key,
        setSeries: true,
    },
};

function makeChart(title, data) {
    const opts = {
        width: 300,
        height: 200,
        title: title,
        cursor: cursorOpts,
        scales: {
            x: {
                time: true,
            },
        },
        series: [
            {
                label:  "heure",
            },
            {
                label:  "🚲 / heure",
                stroke: "green",
                fill:   "rgba(0, 255, 0, 0.1)",
            }
        ]
    };

    let chartContainer = document.getElementById("charts");
    return new uPlot(opts, data, chartContainer);
}