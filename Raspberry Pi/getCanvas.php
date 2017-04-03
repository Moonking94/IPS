<?php
$host = "localhost";
$username = "root";
$password = "root";
$database = "db_ips";

$user_id = $_POST["userId"];

if(isset($_POST["userId"])) {
	
	$user_id = $_POST["userId"];
	
	$pointInfo = array();
	
	$conn = mysqli_connect($hostname, $username, $password, $database);
	
	$query = "SELECT rp.router_id, rp.xaxis, rp.yaxis, rp.zaxis, rp.frequency, up.signal_str, up.pointNum FROM router_profile rp inner join user_position up where rp.router_id = up.router_id AND up.user_id = ? ORDER BY up.pointNum";
	
	if($stmt = mysqli_prepare($conn, $query)) {
		/* bind parameters for markers */
		mysqli_stmt_bind_param($stmt, "i", $user_id);
		
		/* execute query */
		$resultFind = mysqli_stmt_execute($stmt);
		
		/*$router_id = null;
		$xaxis = null;
		$yaxis = null;
		$zaxis = null;
		$frequency = null;
		$signal_str = null;
		$pointNum = null;*/
		
		/* bind variables to prepared statement */
		mysqli_stmt_bind_result($stmt, $router_id, $xaxis, $yaxis, $zaxis, $frequency, $signal_str, $pointNum);
		
		/* fetch values */
		while(mysqli_stmt_fetch($stmt)) {
			$distance = (pow(10, (27.55 - (20 * log10($frequency)) + abs($signal_str)) / 20.0))*10;
			array_push($pointInfo, array(
				"router_id" => $router_id, 
				"xaxis" => $xaxis, 
				"yaxis" => $yaxis, 
				"zaxis" => $zaxis, 
				"frequency" => $frequency, 
				"signal_str" => $signal_str, 
				"distance" => $distance, 
				"pointNum" => $pointNum));
		}
		
		/* close statement */
		mysqli_stmt_close($stmt);
	}
	$response['data'] = $pointInfo;
	echo json_encode($response);
}
?>