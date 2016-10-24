var overlay = new ol.Overlay({
    element: document.getElementById('popup')
});

var map = new ol.Map({
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    ],
    overlays: [overlay],
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


var cvs = document.createElement('canvas');
cvs.height = 1;
cvs.width = 1;
var ctx = cvs.getContext('2d');

function colorToRgba(color) {
    ctx.fillStyle = color;
    ctx.fillRect(0, 0, 1, 1);
    return ctx.getImageData(0, 0, 1, 1).data;
}

var geo;
var mins, maxs;
var serie = 0;
var values;
var colorFunc = createColorFunc('blue, cyan 25%, green 50%, yellow 75%, red');

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
            })
        });
        setSerie(0);
        map.addLayer(geo);
        var selectClick = new ol.interaction.Select({
            condition: ol.events.condition.click
        });
        map.addInteraction(selectClick);
        selectClick.on('select', function (e) {
            var props = e.selected[0].getProperties();
            var name = props.GMDNAME;
            var id = props.GMDNR;
            var datum = values[id];
            var value;
            if (values[id]) {
                value = values[id][serie];
            }
            value = (value == null) ? '*' : sigFigs(value, 4);
            var popup = document.getElementById('popup');
            popup.textContent = name + ': ' + value;

            overlay.setPosition(e.mapBrowserEvent.coordinate);
            popup.style.display = 'block';
        })
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
            value = data[id][serie];
        } else {
            console.log('gemeinde ' + id + ' not found in data')
        }
        var norm = (value - mins[serie]) / (maxs[serie] - mins[serie]);
        var color = colorFunc(norm);
        return [new ol.style.Style({
            fill: new ol.style.Fill({
                color: color
            })
        })]
    });
}

function sigFigs(n, sig) {
    return n === 0 ? '0' : n < 0 ? -posSigFigs(-n) : posSigFigs(n);

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


function createColorFunc(gradient) {
    document.getElementById('scale').style.background = 'linear-gradient(0deg, ' + gradient + ')';
    var values = [];
    var parts = gradient.split(',');
    values.push({value: 0, color: colorToRgba(parts[0].trim())});
    for (var i = 1; i < parts.length - 1; i++) {
        var nameAndValue = parts[i].trim().split(/\s+/);
        var value = nameAndValue[1];
        value = (value.substring(value.length - 1) == '%')
            ? parseFloat(value.substring(0, value.length - 1)) / 100
            : parseFloat(value);
        values.push({value: value, color: colorToRgba(nameAndValue[0])});
    }
    values.push({value: 1, color: colorToRgba(parts[parts.length - 1].trim())});
    return function (v) {
        if (v >= 1) {
            return values[values.length - 1].color;
        }
        if (v <= 0) {
            return values[0].color;
        }
        var i = 0;
        while (values[i].value < v) {
            i++;
        }
        var before = values[i - 1].color;
        var after = values[i].color;
        var pos = (v - values[i - 1].value) / ( values[i].value - values[i - 1].value);
        var red = Math.round(before[0] * (1 - pos) + after[0] * pos);
        var green = Math.round(before[1] * (1 - pos) + after[1] * pos);
        var blue = Math.round(before[2] * (1 - pos) + after[2] * pos);
        var alpha = (before[3] * (1 - pos) + after[3] * pos) / 256;
        return 'rgba(' + red + ',' + green + ',' + blue + ',' + alpha + ')';
    }
}

