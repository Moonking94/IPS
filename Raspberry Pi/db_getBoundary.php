<?php
$host = "localhost";
$username = "root";
$password = "root";
$database = "db_ips";

if(isset($_POST["locationName"]) && isset($_POST["userId"])) {

    $user_id = $_POST["userId"];
	$location = $_POST["locationName"];

    $boundary_info = array();

    $conn = mysqli_connect($hostname, $username, $password, $database);

    $query = "SELECT szd.xaxis, szd.yaxis, szd.zaxis, szd.pointNum FROM safezone_profile_detail szd INNER JOIN safezone_profile szp WHERE szd.safezone_id = szp.safezone_id AND szp.location_id = (SELECT lp.location_id FROM location_profile lp WHERE lp.name = ?) AND szp.user_id = ? ORDER BY szd.pointNum";

    if($stmt = mysqli_prepare($conn, $query)) {
        /* bind parameters for markers */
        mysqli_stmt_bind_param($stmt, "si", $location, $user_id);

        /* execute query */
		$resultFind = mysqli_stmt_execute($stmt);

        /* bind variables to prepared statement */
		mysqli_stmt_bind_result($stmt, $xaxis, $yaxis, $zaxis, $pointNum);

        /* fetch values */
		while(mysqli_stmt_fetch($stmt)) {
			array_push($boundary_info, array(
				"xaxis" => $xaxis, 
				"yaxis" => $yaxis, 
				"zaxis" => $zaxis, 
                "pointNum" => $pointNum));
		}
		
		/* close statement */
		mysqli_stmt_close($stmt);
    }

    $response['responseError'] = FALSE;
    $response['responseMsg'] = "Get boundary successful !";
    $response['responseData'] = $boundary_info;
	echo json_encode($response);

} else {
	$response['responseError'] = TRUE;
    $response['responseMsg'] = "Missing required parameters";
	echo json_encode($response);
}
?>