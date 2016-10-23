var lyr1 = ga.layer.create('ch.swisstopo.pixelkarte-farbe');

// Add the background layer in the map
// map.addLayer(lyr1);

// Create an overlay layer
// var lyr2 = ga.layer.create('ch.swisstopo.fixpunkte-agnes');

// Add the overlay layer in the map
// map.addLayer(lyr2);
var map = new ga.Map({
    layers: [lyr1],
    // Define the div where the map is placed
    target: 'map',

    // Create a view
    view: new ol.View({

        // Define the default resolution
        // 10 means that one pixel is 10m width and height
        // List of resolution of the WMTS layers:
        // 650, 500, 250, 100, 50, 20, 10, 5, 2.5, 2, 1, 0.5, 0.25, 0.1
        resolution: 650,

        // Define a coordinate CH1903 (EPSG:21781) for the center of the view
        center: [660000, 190000]
    })
});

var geo;
var max;
var serie = 0;
var values;

fetch('swiss-community-shape.json').then(res=>res.json()).then(shapes=> {
    fetch('swiss-community-data.json').then(res=>res.json()).then(data=> {
        var select = document.getElementById("serie");
        while (select.children.length > 0) {
            select.removeChild(select.children[0]);
        }
        for (var i = 0; i < data.series.length; i++) {
            var option = document.createElement('option');
            option.value = i;
            option.appendChild(document.createTextNode(data.series[i]));
            select.appendChild(option);

        }
        max = [];
        values = data.data[0].values;
        for (var prop in values) {
            for (var i = 0; i < values[prop].length; i++) {
                if (max[i] === undefined || values[prop][i] > max[i]) {
                    max[i] = values[prop][i];
                }
            }
        }
        geo = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: new ol.format.GeoJSON().readFeatures(shapes)
            }),
        });
        setStyle(values);
        map.addLayer(geo);
    });
});

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
        var norm = value / max[serie];
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

function serieChanged(select) {
    serie = parseInt(select.selectedOptions[0].value);
    setStyle(values);
}