<?php
$host = "localhost";
$username = "root";
$password = "root";
$database = "db_ips";

if(isset($_POST["userId"])) {
	
	$user_id = $_POST["userId"];
	
	$pointInfo = array();
	
	$conn = mysqli_connect($hostname, $username, $password, $database);
	
	$query = "SELECT rp.router_id, rp.ssid, rp.xaxis, rp.yaxis, rp.zaxis, rp.frequency, up.signal_str, up.signal_lvl, up.pointNum FROM router_profile rp inner join user_position up where rp.router_id = up.router_id AND up.user_id = ? ORDER BY up.pointNum";
	
	if($stmt = mysqli_prepare($conn, $query)) {
		/* bind parameters for markers */
		mysqli_stmt_bind_param($stmt, "i", $user_id);
		
		/* execute query */
		$resultFind = mysqli_stmt_execute($stmt);
		
		/*$router_id = null;
		$ssid = null;
		$xaxis = null;
		$yaxis = null;
		$zaxis = null;
		$frequency = null;
		$signal_str = null;
		$signal_lvl = null;
		$pointNum = null;*/
		
		/* bind variables to prepared statement */
		mysqli_stmt_bind_result($stmt, $router_id, $ssid, $xaxis, $yaxis, $zaxis, $frequency, $signal_str, $signal_lvl, $pointNum);
		
		/* fetch values */
		while(mysqli_stmt_fetch($stmt)) {
			 //$distance = (pow(10, (27.55 - (20 * log10($frequency)) + abs($signal_str)) / 20.0))*100; // Will return in meter
			 $distance = -1 * $signal_str * 4;
			// $distance = 100 * $distance;
			// $distance = abs($signal_str) * $signal_lvl ;
			array_push($pointInfo, array(
				"router_id" => $router_id, 
				"ssid" => $ssid, 
				"xaxis" => $xaxis, 
				"yaxis" => $yaxis, 
				"zaxis" => $zaxis, 
				"frequency" => $frequency, 
				"signal_str" => $signal_str, 
				"signal_lvl" => $signal_lvl,
				"distance" => $distance, 
				"pointNum" => $pointNum));
		}
		
		/* close statement */
		mysqli_stmt_close($stmt);
	}
	$response['data'] = $pointInfo;
	echo json_encode($response);
} else {
    $response['error_message'] = "Missing required parameters";
	echo json_encode($response);
}
?>
