function getDateParam(paramName) {
    let url = document.location.href

    let match = url.match(new RegExp(paramName + "=([0-9]{4}-[0-9]{2}-[0-9]{2})"));
    return (match && match[1]) || undefined;
}

document.addEventListener("DOMContentLoaded", function() {

    let date = getDateParam("date");
    let startDate = getDateParam("startDate");
    let endDate = getDateParam("endDate");

    let queryString = date ? `?date=${date}` : `startDate=${startDate}&endDate=${endDate}`
    let title = date ? ` ${date}` : ` ${startDate} -> ${endDate}`

    document.getElementById("logo").textContent += title

    fetch(`/api/raw/data?${queryString}`)
        .then(response => response.json())
        .then(data => drawChartsFromData(date, data))
        .then(data => console.log(data));
});

function drawChartsFromData(date, dataForMeters) {
    for (let dataForMeter of dataForMeters) {
        let series = [ dataForMeter.times, dataForMeter.values, dataForMeter.weatherValues ]
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

const { stepped } = uPlot.paths;
const _stepAfter  = stepped({align:  1});

function makeChart(title, data) {
    const opts = {
        width: 300,
        height: 200,
        title: title,
        cursor: cursorOpts,
        scales: {
            x: {
                time: true,
            }
        },
        series: [
            {
                label:  "heure",
            },
            {
                label:  "🚲 nb/h",
                stroke: "green",
                fill:   "rgba(0, 255, 0, 0.25)",
                scale: "bikes"
            },
            {
                label:  "☔️ mm/h",
                stroke: "blue",
                fill:   "rgba(0, 0, 255, 0.15)",
                paths:  _stepAfter,
                scale: "rain",
                spanGaps: true
            }
        ],
        axes: [
            {},
            {
                scale: "bikes"
            },
            {
                side: 1,
                scale: "rain",
                size: 60,
                grid: {show: false},
            },
        ]
    };

    let chartContainer = document.getElementById("charts");
    return new uPlot(opts, data, chartContainer);
}