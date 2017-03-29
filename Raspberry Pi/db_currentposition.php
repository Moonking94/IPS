<?php
// array for JSON response
$response = array("error" => FALSE);

$hostname = 'localhost';
$username = 'root';
$password = 'root';
$database = 'db_ips';

if(isset($_POST['xaxis']) && isset($_POST['yaxis']) && isset($_POST['zaxis']) && isset($_POST['userId'])) {
	
	$xaxis = $_POST['xaxis'];
    $yaxis = $_POST['yaxis'];
    $zaxis = $_POST['zaxis'];
	$user_id = $_POST['userId'];
	
	$conn = mysqli_connect($hostname, $username, $password, $database);
	
	$queryAdd = "INSERT INTO user_position (xaxis, yaxis, zaxis, user_id) VALUES ($xaxis, $yaxis, $zaxis, $user_id)";
	$queryFind = "SELECT * FROM user_position WHERE user_id = '$user_id'";
	$queryUpdate = "UPDATE user_position SET xaxis='$xaxis', yaxis='$yaxis', zaxis='$zaxis' WHERE user_id='$user_id'";
	
	$resultFind = mysqli_query($conn, $queryFind);
	
	if(mysqli_num_rows($resultFind) > 0) {
		$resultUpdate = mysqli_query($conn, $queryUpdate);
	} else {
		$resultAdd = mysqli_query($conn, $queryAdd);
	}
	
	if($resultUpdate or $resultAdd) {
		if($resultUpdate) {
			$response['responseError'] = FALSE;
			$response['responseMsg'] = "User current position updated";
		} else {
			$response['responseError'] = FALSE;
			$response['responseMsg'] = "User current position added";
		}
		echo json_encode($response);
	} else {
		$response['responseError'] = TRUE;
		$response['responseMsg'] = "User current position failed to be updated";
		echo json_encode($response);
	}
} else {
	$response['responseError'] = TRUE;
	$response['responseMsg'] = "Missing required parameters";
	echo json_encode($response);
}

?>