	var user_info = [[], [], []];
	var current_location = {};
	var ap_info = {};
	
	function init() {		
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
	
	function draw() {
		var canvas = document.getElementById('canvas');
		
		if(canvas.getContext) {
			var ctx = canvas.getContext('2d');
			
			getCanvas();

			p1 = { x:   user_info[0]['xaxis'], y:   user_info[0]['yaxis'], z:  user_info[0]['zaxis'], r:  user_info[0]['distance'], n:  user_info[0]['ssid'], s:  user_info[0]['signal_str']};
			p2 = { x:   user_info[1]['xaxis'], y:   user_info[1]['yaxis'], z:  user_info[1]['zaxis'], r:  user_info[1]['distance'], n:  user_info[1]['ssid'], s:  user_info[1]['signal_str']};
			p3 = { x:   user_info[2]['xaxis'], y:   user_info[2]['yaxis'], z:  user_info[2]['zaxis'], r:  user_info[2]['distance'], n:  user_info[2]['ssid'], s:  user_info[2]['signal_str']};
			
			//p4 = trilaterate(p1, p2, p3, true);
			//p4 = triangulation(p1, p2, p3);
			p4 = trilateratev2(p1, p2, p3);
			
			if (p4 != null)
			{
				ctx.fillStyle = "#111";
			}
			else
			{
				ctx.fillStyle = "#800";
			}

			/*
			#111 = black
			#800 = dark red
			#f00 = red
			#0f0 = green
			#00f = blue

			*/
			ctx.fillRect(0, 0, canvas.width, canvas.height);
			ctx.font="20px Arial";
			ctx.lineWidth = 5;

			ctx.fillStyle = "#0f0";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(200 + p1.x, 200 + p1.y, 5, 5);
			ctx.beginPath();
			ctx.fillText(p1.n, 200 + p1.x, 200 + p1.y);
			// ctx.arc(500 + p1.x, 500 + p1.y, p1.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			// ctx.stroke();

			ctx.fillStyle = "#0f0";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(200 + p2.x, 200 + p2.y, 5, 5);
			ctx.beginPath();
			ctx.fillText(p2.n, 200 + p2.x, 200 + p2.y);
			// ctx.arc(500 + p2.x, 500 + p2.y, p2.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			// ctx.stroke();

			ctx.fillStyle = "#0f0";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(200 + p3.x, 200 + p3.y, 5, 5);
			ctx.beginPath();
			ctx.fillText(p3.n, 200 + p3.x, 200 + p3.y);
			// ctx.arc(500 + p3.x, 500 + p3.y, p3.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			// ctx.stroke();

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
					ctx.fillStyle = "#fff";
					ctx.fillRect(200 + p4.x, 200 + p4.y, 5, 5);
					document.getElementById("point4").innerText = "X: " + p4.x + " Y: " + p4.y + " Z: " + p4.z;
				}
			}
			document.getElementById("point1").innerText = "Router " + p1.n + " X: " + p1.x + " Y: " + p1.y + " R: " + p1.r + " Signal str: " + p1.s;
			document.getElementById("point2").innerText = "Router " + p2.n + " X: " + p2.x + " Y: " + p2.y + " R: " + p2.r + " Signal str: " + p2.s;
			document.getElementById("point3").innerText = "Router " + p3.n + " X: " + p3.x + " Y: " + p3.y + " R: " + p3.r + " Signal str: " + p3.s;
		}
		window.requestAnimationFrame(draw);
	}
