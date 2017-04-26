<?php
$host = "localhost";
$username = "root";
$password = "root";
$database = "db_ips";

if(isset($_POST["location"])) {

    $location = $_POST["location"];
    $status = "A";

    $ap_info = array();

    $conn = mysqli_connect($hostname, $username, $password, $database);

    $query = "SELECT rp.router_id, rp.ssid, rp.xaxis, rp.yaxis, rp.zaxis FROM router_profile rp WHERE rp.location_id = (SELECT lp.location_id FROM location_profile lp WHERE lp.name = ?) AND rp.status = ? ";

    if($stmt = mysqli_prepare($conn, $query)) {
        /* bind parameters for markers */
        mysqli_stmt_bind_param($stmt, "ss", $location, $status);

        /* execute query */
		$resultFind = mysqli_stmt_execute($stmt);

        /* bind variables to prepared statement */
		mysqli_stmt_bind_result($stmt, $router_id, $ssid, $xaxis, $yaxis, $zaxis);

        /* fetch values */
		while(mysqli_stmt_fetch($stmt)) {
			array_push($ap_info, array(
				"router_id" => $router_id, 
				"router_name" => $ssid . ("(X: ") . $xaxis . (", Y: ") . $yaxis . (")"), 
				"xaxis" => $xaxis, 
				"yaxis" => $yaxis, 
				"zaxis" => $zaxis));
		}
		
		/* close statement */
		mysqli_stmt_close($stmt);
    }
    $response['data'] = $ap_info;
	echo json_encode($response);
} else {
    $response['error_message'] = "Missing required parameters";
	echo json_encode($response);
}
?>