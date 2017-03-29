<?php
// array for JSON response
$response = array("error" => FALSE);

$hostname = 'localhost';
$username = 'root';
$password = 'root';
$database = 'db_ips';

if(isset($_POST['p1']) && isset($_POST['p2']) && isset($_POST['p3'])) {
	
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

	$response['responseError'] = FALSE;
	$response['responseMsg'] = $pCo;
	//$response['responseMsg'] = mysqli_num_rows($resultSelect);

    echo json_encode($response);
} else {
    $response['responseError'] = TRUE;
    $response['responseMsg'] = "Missing required parameters";
    echo json_encode($response);
}
?>