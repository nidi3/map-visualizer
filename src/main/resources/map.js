var map = new ol.Map({
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    ],
    target: 'map',
    view: new ol.View({

        // Define the default resolution
        // 10 means that one pixel is 10m width and height
        // List of resolution of the WMTS layers:
        // 650, 500, 250, 100, 50, 20, 10, 5, 2.5, 2, 1, 0.5, 0.25, 0.1
        resolution: 650,
        center: [0, 0]
    })
});

var geo;
var mins, maxs;
var serie = 0;
var values;

fetch('swiss-community-shape-15.json').then(res=>res.json()).then(shapes=> {
    var bbox = shapes.bbox;
    map.getView().setCenter([(bbox[0] + bbox[2]) / 2, (bbox[1] + bbox[3]) / 2]);
    fetch('swiss-community-data.json').then(res=>res.json()).then(data=> {
        var select = document.getElementById('serie');
        while (select.children.length > 0) {
            select.removeChild(select.children[0]);
        }
        for (var i = 0; i < data.series.length; i++) {
            var option = document.createElement('option');
            option.value = i;
            option.appendChild(document.createTextNode(data.series[i]));
            select.appendChild(option);
        }
        document.getElementById('title').textContent = data.title;
        values = data.data[0].values;
        mins = min(values);
        maxs = max(values);

        geo = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: new ol.format.GeoJSON().readFeatures(shapes)
            }),
        });
        setSerie(0);
        map.addLayer(geo);
    });
});

function max(data) {
    var max = [];
    for (var prop in data) {
        for (var i = 0; i < data[prop].length; i++) {
            if (data[prop][i] != null && (max[i] === undefined || data[prop][i] > max[i])) {
                max[i] = data[prop][i];
            }
        }
    }
    return max;
}

function min(data) {
    var min = [];
    for (var prop in data) {
        for (var i = 0; i < data[prop].length; i++) {
            if (data[prop][i] != null && (min[i] === undefined || data[prop][i] < min[i])) {
                min[i] = data[prop][i];
            }
        }
    }
    return min;
}

function setStyle(data) {
    geo.setStyle((feature)=> {
        var id = feature.getProperties().GMDNR;
        var datum = data[id];
        var value = null;
        if (data[id]) {
            value = data[id][serie]; //2 16
        } else {
            console.log('gemeinde ' + id + ' not found in data')
        }
        var norm = (value - mins[serie]) / (maxs[serie] - mins[serie]);
        var red = norm < .333 ? 0 : norm < .666 ? Math.round(768 * (norm - .333)) : 255;
        var green = norm < .666 ? 0 : Math.round(768 * (norm - .666));
        var blue = norm < .333 ? Math.round(768 * norm) : norm < .666 ? Math.round(768 * (.666 - norm)) : 0;
        return [new ol.style.Style({
            fill: new ol.style.Fill({
                color: 'rgba(' + red + ',' + green + ',' + blue + ',' + (value == null ? '0' : '0.8') + ')'
            })
        })]
    });
}

function sigFigs(n, sig) {
    return n < 0 ? -posSigFigs(-n) : posSigFigs(n);

    function posSigFigs(n) {
        var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
        return Math.round(n * mult) / mult;
    }
}


function serieChanged(select) {
    setSerie(parseInt(select.selectedOptions[0].value));
}

function setSerie(s) {
    serie = s;
    document.getElementById('min').textContent = sigFigs(mins[serie], 4);
    document.getElementById('max').textContent = sigFigs(maxs[serie], 4);
    setStyle(values);
}