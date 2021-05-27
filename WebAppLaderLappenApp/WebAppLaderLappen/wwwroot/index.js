var grid;
var domain;
function createCanvas(){
    var width = window.innerWidth
    || document.documentElement.clientWidth
    || document.body.clientWidth;
    
    var height = window.innerHeight
    || document.documentElement.clientHeight
    || document.body.clientHeight;

    var canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;

     grid = new Grid(canvas);

     domain = [-30, 30];
    
    document.body.appendChild(canvas);
}

function addMowerPos(x, y, collision, onLine) {
    if (onLine == true) {
        grid.addCircle(x, y, 0.1, { "colour": "yellow", "fill": true });
    }
    else if (collision == true) {
        grid.addCircle(x, y, 0.1, { "colour": "red", "fill": true });
    } else {
        grid.addCircle(x, y, 0.1, { "colour": "green", "fill": true });
    }
}