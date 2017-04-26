	var user_info = {};
	var ap_info = {};
	
	function init() {
		getAP();
		getCanvas();
		
		draw();
	}
	
	function getCanvas() {
		var userId = 1;
		if(userId > 0 || userId != null) {
			var request = new XMLHttpRequest();
			request.overrideMimeType("application/json");
			request.open("POST", "../getCanvas.php", true);
			request.onreadystatechange = function() {
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
		if(location != "" || location != null) {
			var request = new XMLHttpRequest();
			request.overrideMimeType("application/json");
			request.open("POST", "../db_getAP.php", true);
			request.onreadystatechange = function() {
				if (request.readyState == 4 && request.status == 200) {
					ap_info = JSON.parse(this.responseText).data;
					console.log(ap_info);
				}
			}
			
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("location=" + location);
		}
	}

	function drawAP() {
		var canvas = document.getElementById('canvas');

		if(canvas.getContext) {
			var ctx = canvas.getContext('2d');

			ctx.fillRect(0, 0, canvas.width, canvas.height);
			ctx.font="17px Arial";
			ctx.lineWidth = 5;

			for(var i = 0;i<ap_info.length;i++) {
				ctx.globalAlpha = 1.0;
				ctx.fillStyle = "#f00";
				ctx.strokeStyle = ctx.fillStyle;
				ctx.fillRect(100 + ap_info[i].xaxis, 100 + ap_info[i].yaxis, 5, 5);
				ctx.beginPath();
				ctx.fillText(ap_info[i].router_name, 100 + ap_info[i].xaxis, 100 + ap_info[i].yaxis);
			}
		}
	}
	
	function checkPoint(){
		return true;
	}
	
	function draw() {
		var canvas = document.getElementById('canvas');
						
		var point1 ={x:"250",y: "200"};
		var point2 ={x:"400",y: "200"};
		var point3 ={x:"400",y: "300"};
		var point4 ={x:"250",y: "300"};
		
		if(canvas.getContext) {
			var ctx = canvas.getContext('2d');
			var gps = document.getElementById("gps");
			
			floor=new Image();
				floor.onload=function(){
				ctx.globalAlpha = 0.1;			
				ctx.drawImage(floor,0,0,canvas.width,canvas.height);	
				}
			floor.src="bg_images/3A4.jpg";
			

			p4 = null;

			getAP();
			getCanvas();

			if(user_info.length != null) {
				p1 = { x:   user_info[0].xaxis, y:   user_info[0].yaxis, z:  user_info[0].zaxis, r:  user_info[0].distance, n:  user_info[0].ssid, s:  user_info[0].signal_str};
				p2 = { x:   user_info[1].xaxis, y:   user_info[1].yaxis, z:  user_info[1].zaxis, r:  user_info[1].distance, n:  user_info[1].ssid, s:  user_info[1].signal_str};
				p3 = { x:   user_info[2].xaxis, y:   user_info[2].yaxis, z:  user_info[2].zaxis, r:  user_info[2].distance, n:  user_info[2].ssid, s:  user_info[2].signal_str};

				p4 = trilaterate(p1, p2, p3, true);

				document.getElementById("point1").innerText = "Router " + p1.n + " X: " + p1.x + " Y: " + p1.y + " R: " + p1.r + " Signal str: " + p1.s;
				document.getElementById("point2").innerText = "Router " + p2.n + " X: " + p2.x + " Y: " + p2.y + " R: " + p2.r + " Signal str: " + p2.s;
				document.getElementById("point3").innerText = "Router " + p3.n + " X: " + p3.x + " Y: " + p3.y + " R: " + p3.r + " Signal str: " + p3.s;
				
			}
			
			//p4 = triangulation(p1, p2, p3);
			//p4 = trilateratev2(p1, p2, p3);
			
			if (p4 != null)
			{
				ctx.fillStyle = "#fff";
			}
			else
			{
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

			if (p4 !== null)
			{
				if (p4 instanceof Array)
				{
					ctx.fillStyle = "#0ff";
					ctx.fillRect(20 + p4[0].x - 2, 20 + p4[0].y - 2, 5, 5);

					ctx.fillStyle = "#ff0";
					ctx.fillRect(20 + p4[1].x - 2, 20 + p4[1].y - 2, 5, 5);
				}
				else
				{

					document.getElementById("point4").innerText = "X: " + p4.x + " Y: " + p4.y + " Z: " + p4.z;
					
					 ctx.moveTo(point1.x,point1.y);
					 ctx.lineTo(point2.x,point2.y);
					 ctx.lineTo(point3.x,point3.y);
					 ctx.lineTo(point4.x,point4.y);
					 ctx.lineTo(point1.x,point1.y);
					 ctx.stroke();
					
					
					if(checkPoint()){
						gps=new Image();
							gps.onload=function(){
							ctx.globalAlpha = 0.1;
							ctx.drawImage(gps,100+ p4.x, 100+ p4.y,30,30);	
							}		
						gps.src="bg_images/gps.png";		
		
					for(var p = 0, len = gps.length; p < gps; p+=4) {
						gps[p + 0] = 100
						gps[p + 1] = 100
						gps[p + 2] = 200
						gps[p + 3] = 300
					}


						
					}else{
						gps=new Image();
						gps.onload=function(){
						ctx.globalAlpha = 0.1;			
						ctx.drawImage(gps,100+ p4.x, 100+ p4.y,30,30);	
						}
						gps.src="bg_images/gps.png";
					}
					
					
				}

			}
		}
			
		window.requestAnimationFrame(draw);

	}
