<?php
$host = "localhost";
$user = "root";
$pass = "root";
$database = "SafeHome";
?>
<?php
echo "
<html>
	<head>
		<title>trilateration.js</title>
		<script type=\"text/javascript\" src=\"trilateration.js\"></script>
	</head>
	<body>
		<h1>Trilateration in 3D</h1>

		<p>
			<em>
			\"In geometry, trilateration is the process of determining absolute
			or relative locations of points by measurement of distances, using
			the geometry of circles, spheres or triangles.<br/>
			<br/>
			In addition to its interest as a geometric problem, trilateration
			does have practical applications in surveying and navigation,
			including global positioning systems (GPS). In contrast to
			triangulation, it does not involve the measurement of angles.\"</em>

			&mdash; <a href=\"https://en.wikipedia.org/wiki/Trilateration\">Wikipedia</a>
		</p>

		<p>
			The trilateration can result zero, one or two solutions, in these
			cases trilaterate() will return <em>null</em>, an Object with
			{ x, y, z } coordinates or an Array with two Objects with
			{ x, y, z } coordinates, respectively.
		</p>

		<p>
			There is an optional fourth parameter after the three points for
			trilaterate() for the case of two solutions to return the middle
			of them as one point.
		</p>

		<p>
			In this example...
			<ul>
				<li>the canvas will turn red if no solution can be found,</li>
				<li>a white dot will show the solution if one can be found,</li>
				<li>a yellow and a cyan dot will show the two solutions if there
				are two of them.</li>
			</ul>
		</p>

		<p>
			Note: the display of points and distances are projected to the X-Y
			plane so the Z coordinates cannot be seen, but they are taken into
			consideration by trilaterate()!
		</p>

		<canvas id=\"canvas1\" width=\"800\" height=\"800\">
		</canvas>
		";
?>
<?php
	echo "
		<script type=\"text/javascript\">
			var canvas, ctx;

			function initialize()
			{
				canvas = document.getElementById(\"canvas1\");
				ctx = canvas.getContext(\"2d\");";

				$p1 = $_POST['p1'];
				$p2 = $_POST['p2'];
				$p3 = $_POST['p3'];

				$pointInfo = array(
				array($p1[0], $p1[1], $p1[2], $p1[3]), 
				array($p2[0], $p2[1], $p2[2], $p2[3]), 
				array($p3[0], $p3[1], $p3[2], $p3[3]));

				$conn = mysqli_connect($hostname, $username, $password, $database);
				
				$pCo = array();
	
				for($i = 0;$i<count($pointInfo);$i++) {
					$query = "SELECT xaxis, yaxis, zaxis FROM router_profile where bssid = '" . $pointInfo[$i][1] . "'";
					$resultSelect = mysqli_query($conn, $query);
					
					if(mysqli_num_rows($resultSelect) > 0) {
						while($row = mysqli_fetch_array($resultSelect, MYSQLI_BOTH)) {
							array_push($pCo, array($row["xaxis"], $row["yaxis"], $row["zaxis"]));
						}
					}
				}

				echo "
				p1 = { x:   " . $pCo[0][0] . ", y:   " . $pCo[0][1] . ", z:  " . $pCo[0][2] . ", r: 325 }; <!-- Red -->
				p2 = { x:   " . $pCo[1][0] . ", y:   " . $pCo[1][1] . ", z:  " . $pCo[1][2] . ", r: 325 }; <!-- Green -->
				p3 = { x:   " . $pCo[2][0] . ", y:   " . $pCo[2][1] . ", z:  " . $pCo[2][2] . ", r: 325 }; <!-- Blue -->

				p4 = trilaterate(p1, p2, p3, true);

				if (p4 !== null)
				{
					ctx.fillStyle = \"#111\";
				}
				else
				{
					ctx.fillStyle = \"#800\";
				}
				ctx.fillRect(0, 0, canvas.width, canvas.height);

				ctx.fillStyle = \"#f00\";
				ctx.strokeStyle = ctx.fillStyle;
				ctx.fillRect(20 + p1.x - 2, 20 + p1.y - 2, 5, 5);
				ctx.beginPath();
				ctx.arc(20 + p1.x, 20 + p1.y, p1.r, 0, 2 * Math.PI);
				ctx.stroke();

				ctx.fillStyle = \"#0f0\";
				ctx.strokeStyle = ctx.fillStyle;
				ctx.fillRect(20 + p2.x - 2, 20 + p2.y - 2, 5, 5);
				ctx.beginPath();
				ctx.arc(20 + p2.x, 20 + p2.y, p2.r, 0, 2 * Math.PI);
				ctx.stroke();

				ctx.fillStyle = \"#00f\";
				ctx.strokeStyle = ctx.fillStyle;
				ctx.fillRect(20 + p3.x - 2, 20 + p3.y - 2, 5, 5);
				ctx.beginPath();
				ctx.arc(20 + p3.x, 20 + p3.y, p3.r, 0, 2 * Math.PI);
				ctx.stroke();

				if (p4 !== null)
				{
					if (p4 instanceof Array)
					{
						ctx.fillStyle = \"#0ff\";
						ctx.fillRect(20 + p4[0].x - 2, 20 + p4[0].y - 2, 5, 5);

						ctx.fillStyle = \"#ff0\";
						ctx.fillRect(20 + p4[1].x - 2, 20 + p4[1].y - 2, 5, 5);
					}
					else
					{
						ctx.fillStyle = \"#fff\";
						ctx.fillRect(20 + p4.x - 2, 20 + p4.y - 2, 5, 5);
					}
				}
			}

			window.onload = initialize;
		</script>
	</body>
</html>";
?>