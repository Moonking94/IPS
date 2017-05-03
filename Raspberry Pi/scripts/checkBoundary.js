function checkInsideBoundary(polygon,x,y){
	var result = false;
	var sides = polygon.length;
	
	var vertx = [];
	var verty = [];
	
	if(sides>0){
		for(var i=0;i<polygon.length;i++){
			vertx[i] = polygon[i].xaxis;
			verty[i] = polygon[i].yaxis;
		}
		
		var i,j;
		for(i=0,j = sides-1;i<sides; j=i++){
			if(((verty[i]>y)!=(verty[j]>y)) && (x<(vertx[j]-vertx[i])*(y-verty[i])/(verty[j]-verty[i])+vertx[i]))
				result = !result;
		}
		
		return result;	
	}
}
	
function toRad(Value) {
    /** Converts numeric degrees to radians */
    return Value * Math.PI / 180;
}

function checkCircle(x0,y0,r,x,y){
	var C = 40075.04;
	var A = 360 * (r/1000)/C;
	var B = A /Math.cos(Math.toRad(x0));
	return Math.pow((x - x0) / A, 2) + Math.pow((y - y0) / B, 2) < 1;
}