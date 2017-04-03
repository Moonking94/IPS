	var user_info = [[], [], []];
	var current_location = {};
	var ap_info = {};
	
	function init() {
		getCurLocation();
		getAPInfo();
		
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
					console.log("get_canvas_func: " + user_info);
				}
			}
			
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("userId=" + userId);
		}
	}
	
	function getCurLocation() {
		var userId = 1;
		var request = new XMLHttpRequest();
			request.overrideMimeType("application/json");
			request.open("POST", "../db_getposition.php", true);
			request.onreadystatechange = function() {
				if (request.readyState == 4 && request.status == 200) {
					current_location = JSON.parse(this.responseText);
					console.log("get_cur_loc_func" + current_location);
				}
			}
			
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("userId=" + userId);
	}
	
	function getAPInfo() {
		var userId = 1;
/*		var request = new XMLHttpRequest();
			request.overrideMimeType("application/json");
			request.open("POST", "../db_getAPInfo.php", true);
			request.onreadystatechange = function() {
				if (request.readyState == 4 && request.status == 200) {
					ap_info = JSON.parse(this.responseText);
					console.log(ap_info);
				}
			}
			
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("userId=" + userId);*/
	}
	
	function draw() {
		var canvas = document.getElementById('canvas');
		
		if(canvas.getContext) {
			var ctx = canvas.getContext('2d');
			
			getCanvas();
			
			p1 = { x:   user_info[0]['xaxis'], y:   user_info[0]['yaxis'], z:  user_info[0]['zaxis'], r:  user_info[0]['distance']}; <!-- Red -->
			console.log(user_info);
			p2 = { x:   user_info[1]['xaxis'], y:   user_info[1]['yaxis'], z:  user_info[1]['zaxis'], r:  user_info[1]['distance']}; <!-- Green -->
			p3 = { x:   user_info[2]['xaxis'], y:   user_info[2]['yaxis'], z:  user_info[2]['zaxis'], r:  user_info[2]['distance']}; <!-- Blue -->
			
			p4 = trilaterate(p1, p2, p3, true);
			
			if (p4 !== null)
			{
				ctx.fillStyle = "#111";
			}
			else
			{
				ctx.fillStyle = "#800";
			}

			ctx.fillRect(0, 0, canvas.width, canvas.height);
			
			ctx.fillStyle = "#f00";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(20 + p1.x - 2, 20 + p1.y - 2, 5, 5);
			ctx.beginPath();
			ctx.arc(20 + p1.x, 20 + p1.y, p1.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			ctx.stroke();

			ctx.fillStyle = "#0f0";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(20 + p2.x - 2, 20 + p2.y - 2, 5, 5);
			ctx.beginPath();
			ctx.arc(20 + p2.x, 20 + p2.y, p2.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			ctx.stroke();

			ctx.fillStyle = "#00f";
			ctx.strokeStyle = ctx.fillStyle;
			ctx.fillRect(20 + p3.x - 2, 20 + p3.y - 2, 5, 5);
			ctx.beginPath();
			ctx.arc(20 + p3.x, 20 + p3.y, p3.r, 0, 2 * Math.PI); // (x, y, radius, start angle, end angle);
			ctx.stroke();

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
					ctx.fillRect(20 + p4.x - 2, 20 + p4.y - 2, 5, 5);
				}
			}
		}
		window.requestAnimationFrame(draw);
	}
