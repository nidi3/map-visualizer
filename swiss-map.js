/*
 * ------------------------------------------------------------------------------------------------
 * Copyright 2014 by Swiss Post, Information Technology Services
 * ------------------------------------------------------------------------------------------------
 * $Id$
 * ------------------------------------------------------------------------------------------------
 */


// Create a background layer
var lyr1 = ga.layer.create('ch.swisstopo.pixelkarte-farbe');

// Add the background layer in the map
// map.addLayer(lyr1);

// Create an overlay layer
// var lyr2 = ga.layer.create('ch.swisstopo.fixpunkte-agnes');

// Add the overlay layer in the map
// map.addLayer(lyr2);
var index = 42;
var map;
var geo;
var allData;
var max;

fetch('components/data.json').then(res=>res.json()).then(data=> {
    allData = data;
    max = [];
    for (var prop in data) {
        for (var i = 0; i < data[prop].length; i++) {
            if (max[i]===undefined || data[prop][i] > max[i]) {
                max[i] = data[prop][i];
            }
        }
    }
    fetch('components/gemeinde.json').then(res=>res.json()).then(shapes=> {
        for (var i = 0; i < shapes.features.length; i++) {
            if (shapes.features[i].geometry) {
                var cs = shapes.features[i].geometry.coordinates[0];
                for (var j = 0; j < cs.length; j++) {
                    cs[j].splice(2, 1);
                }
            }
        }

        geo = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: new ol.format.GeoJSON().readFeatures(shapes)
            }),
        });
        setStyle(data);

        map = new ga.Map({
            layers: [lyr1, geo],
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
    });
});

function setStyle(data) {
    geo.setStyle((feature)=> {
        var id = feature.getProperties().BFS_NUMMER;
        var datum = data[id];
        var value = null;
        if (data[id]) {
            value = data[id][index]; //2 16
        } else {
            console.log('gemeinde ' + id + ' not found in data')
        }
        var norm = value / max[index];
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

function toggle() {
    index = 85 - index;
    setStyle(allData);
}
