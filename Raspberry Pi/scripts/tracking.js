var user_info = {};
var ap_info = {};
var boundary_info = {};

function init() {
	getAP();
	getCanvas();
	getBoundary();

	draw();
}

function getCanvas() {
	var userId = 1;
	if (userId > 0 || userId != null) {
		var request = new XMLHttpRequest();
		request.overrideMimeType("application/json");
		request.open("POST", "../getCanvas.php", true);
		request.onreadystatechange = function () {
			if (request.readyState == 4 && request.status == 200) {
				user_info = JSON.parse(this.responseText).data;
				console.log(user_info);
			}
		}

		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send("userId=" + userId);
	}
}

function getAP() {
	var location = "3A";
	if (location != "" || location != null) {
		var request = new XMLHttpRequest();
		request.overrideMimeType("application/json");
		request.open("POST", "../db_getAP.php", true);
		request.onreadystatechange = function () {
			if (request.readyState == 4 && request.status == 200) {
				ap_info = JSON.parse(this.responseText).data;
				console.log(ap_info);
			}
		}

		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send("locationName=" + location);
	}
}

function getBoundary() {
	var location = "3A";
	var userId = 1;
	if ((location != "" || location != null) && (userId > 0 || userId != null)) {
		var request = new XMLHttpRequest();
		request.overrideMimeType("application/json");
		request.open("POST", "../db_getBoundary.php", true);
		request.onreadystatechange = function () {
			if (request.readyState == 4 && request.status == 200) {
				boundary_info = JSON.parse(this.responseText).data;
				console.log(boundary_info);
			}
		}

		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send("locationName=" + location + "&userId=" + userId);
	}
}

function drawAP() {
	var canvas = document.getElementById('canvas');

	if (canvas.getContext) {
		var ctx = canvas.getContext('2d');

		ctx.fillRect(0, 0, canvas.width, canvas.height);
		ctx.font = "17px Arial";
		ctx.lineWidth = 5;

		for (var i = 0; i < ap_info.length; i++) {
			ctx.globalAlpha = 1.0;
			ctx.fillStyle = "#f00";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(100 + ap_info[i].xaxis, 100 + ap_info[i].yaxis, 5, 5);
			ctx.beginPath();
			ctx.fillText(ap_info[i].router_name, 100 + ap_info[i].xaxis, 100 + ap_info[i].yaxis);
		}
	}
}

function checkPoint(vs, pt) {
	var x = pt.x+100, y = pt.y+100;

    var inside = false;
    for (var i = 0, j = vs.length - 1; i < vs.length; j = i++) {
        var xi = vs[i][0], yi = vs[i][1];
        var xj = vs[j][0], yj = vs[j][1];

        var intersect = ((yi > y) != (yj > y))
            && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
        if (intersect) 
			inside = !inside;
    }
	  return !inside;
}




function draw() {
	var canvas = document.getElementById('canvas');
	
	if (canvas.getContext) {
		var ctx = canvas.getContext('2d');
		var gps = document.getElementById("gps");

		floor = new Image();
		floor.onload = function () {
			ctx.globalAlpha = 0.1;
			ctx.drawImage(floor, 0, 0, canvas.width, canvas.height);
		}
		floor.src = "bg_images/3A4.jpg";


		p4 = null;

		getAP();
		getBoundary();
		getCanvas();

		if (user_info.length != null) {
			p1 = { x: user_info[0].xaxis, y: user_info[0].yaxis, z: user_info[0].zaxis, r: user_info[0].distance, n: user_info[0].ssid, s: user_info[0].signal_str };
			p2 = { x: user_info[1].xaxis, y: user_info[1].yaxis, z: user_info[1].zaxis, r: user_info[1].distance, n: user_info[1].ssid, s: user_info[1].signal_str };
			p3 = { x: user_info[2].xaxis, y: user_info[2].yaxis, z: user_info[2].zaxis, r: user_info[2].distance, n: user_info[2].ssid, s: user_info[2].signal_str };

			p4 = trilaterate(p1, p2, p3, true);

			document.getElementById("point1").innerText = "Router " + p1.n + " X: " + p1.x + " Y: " + p1.y + " R: " + p1.r + " Signal str: " + p1.s;
			document.getElementById("point2").innerText = "Router " + p2.n + " X: " + p2.x + " Y: " + p2.y + " R: " + p2.r + " Signal str: " + p2.s;
			document.getElementById("point3").innerText = "Router " + p3.n + " X: " + p3.x + " Y: " + p3.y + " R: " + p3.r + " Signal str: " + p3.s;

		}

		//p4 = triangulation(p1, p2, p3);
		//p4 = trilateratev2(p1, p2, p3);

		if (p4 != null) {
			ctx.fillStyle = "#fff";
		}
		else {
			ctx.fillStyle = "#800";
		}

		/*
		#fff = white
		#111 = black
		#800 = dark red
		#f00 = red
		#0f0 = green
		#00f = blue
		*/

		drawAP();

		if (p4 !== null) {
			if (p4 instanceof Array) {
				ctx.fillStyle = "#0ff";
				ctx.fillRect(20 + p4[0].x - 2, 20 + p4[0].y - 2, 5, 5);

				ctx.fillStyle = "#ff0";
				ctx.fillRect(20 + p4[1].x - 2, 20 + p4[1].y - 2, 5, 5);
			}
			else {

				document.getElementById("point4").innerText = "X: " + p4.x + " Y: " + p4.y + " Z: " + p4.z;
				
				
				if (boundary_info.length > 0){// && boundary_info != null) {
					ctx.moveTo(boundary_info[0].xaxis, boundary_info[0].yaxis);
					for (var count = 1; count < boundary_info.length; count++) {
						ctx.lineTo(boundary_info[count].xaxis, boundary_info[count].yaxis);
					}
					ctx.lineTo(boundary_info[0].xaxis, boundary_info[0].yaxis);
					ctx.stroke();
				}

				if (checkPoint(boundary_info, p4)) {
					gps = new Image();
					gps.onload = function () {
						ctx.globalAlpha = 0.1;
						ctx.drawImage(gps, 100 + p4.x, 100 + p4.y, 30, 30);
					}
					gps.src = "bg_images/gpsTrue.png";
				} else {
					gps = new Image();
					gps.onload = function () {
						ctx.globalAlpha = 0.1;
						ctx.drawImage(gps, 100 + p4.x, 100 + p4.y, 30, 30);
					}
					gps.src = "bg_images/gpsFalse.png";
				}


			}

		}
	}

	window.requestAnimationFrame(draw);

}
