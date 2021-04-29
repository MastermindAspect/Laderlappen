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

     domain = [-20, 20];
    
    document.body.appendChild(canvas);
}

function addMowerPos(x, y, collision) {
    if (collision == true) {
        grid.addCircle(x, y, 0.2, { "colour": "red", "fill": true });
    } else {
        grid.addCircle(x, y, 0.2, { "colour": "blue", "fill": true });
    }
    
}



