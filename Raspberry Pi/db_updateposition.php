<?php
// array for JSON response
$response = array("error" => FALSE);

$hostname = 'localhost';
$username = 'root';
$password = 'root';
$database = 'db_ips';

if(isset($_POST['p1']) && isset($_POST['p2']) && isset($_POST['p3']) && isset($_POST['userId'])) {
	
	$p1 = $_POST['p1'];
	$p2 = $_POST['p2'];
	$p3 = $_POST['p3'];
	$user_id = $_POST['userId'];
	
	$pointInfo = array(
	array($p1[0], $p1[1], $p1[2]), 
	array($p2[0], $p2[1], $p2[2]), 
	array($p3[0], $p3[1], $p3[2]));
	
	$conn = mysqli_connect($hostname, $username, $password, $database);
	
	/* Add query */
	$queryAdd = "INSERT INTO user_position (router_id, signal_str, pointNum, user_id) VALUES ((SELECT router_id FROM router_profile WHERE bssid = ?), ?, ?, ?)";
	/* Query to find user exist or not */
	$queryFind = "SELECT * FROM user_position WHERE user_id = $user_id";
	/* Update query */
	$queryUpdate = "UPDATE user_position SET router_id = (SELECT router_id FROM router_profile WHERE bssid = ?), signal_str = ? WHERE user_id = ? AND pointNum = ?";
	
	$resultFind = mysqli_query($conn, $queryFind);
	
	$num = 1;
	
	if(mysqli_num_rows($resultFind) > 0) {
		if($stmt = mysqli_prepare($conn, $queryUpdate)) {
			for($i = 0;$i<3;$i++) {
				/* bind parameters for markers */
				mysqli_stmt_bind_param($stmt, "sdii", $pointInfo[$i][1], $pointInfo[$i][2], $user_id, $num);
				
				/* execute query */
				$resultUpdate = mysqli_stmt_execute($stmt);	
				
				/* bind result variables */
				mysqli_stmt_bind_result($stmt);
				
				/* fetch result boolean */
				mysqli_stmt_fetch($stmt);		
				
				$num += 1;
			}
			
			/* close statement */
			mysqli_stmt_close($stmt);
		}
	} else {
		if($stmt = mysqli_prepare($conn, $queryAdd)) {
			for($i = 0;$i<3;$i++) {
				/* bind parameters for markers */
				mysqli_stmt_bind_param($stmt, "sdii", $pointInfo[$i][1], $pointInfo[$i][2], $num, $user_id);
				
				/* execute query */
				$resultAdd = mysqli_stmt_execute($stmt);
				
				/* bind result variables */
				mysqli_stmt_bind_result($stmt);
				
				/* fetch result boolean */
				mysqli_stmt_fetch($stmt);
				
				$num += 1;
			}
			
			/* close statement */
			mysqli_stmt_close($stmt);
		}
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
		$response['responseMsg'] = "Failed to add or update position";
		echo json_encode($response);
	}
} else {
	$response['responseError'] = TRUE;
	$response['responseMsg'] = "Missing required parameters";
	echo json_encode($response);
}
?>