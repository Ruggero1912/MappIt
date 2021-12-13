- How to convert from .osm json to .geojson:

1. install  osmtogeojson:
    npm install -g osmtogeojson
2.  osmtogeojson -f json -m true ITALY.osm > ITALY.geojson
    -f specifies the input format
    -m stands for minify
3. NOTE: there is an encoding problem, the unique (temporary) solution found at now is to copy paste the content of the response file in another file...