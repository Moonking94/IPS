function triangulation(p1, p2, p3)
{
	// var sr1 = (-(100+p1.r)) / ((-(100+p1.r)) + (-(100+p2.r)) + (-(100+p3.r)));
    // var sr2 = (-(100+p2.r)) / ((-(100+p1.r)) + (-(100+p2.r)) + (-(100+p3.r)));
    // var sr3 = (-(100+p3.r)) / ((-(100+p1.r)) + (-(100+p2.r)) + (-(100+p3.r)));
	
	var sr1 = p1.r / (p1.r + p2.r + p3.r);
    var sr2 = p2.r / (p1.r + p2.r + p3.r);
    var sr3 = p3.r / (p1.r + p2.r + p3.r);
	
	var latitude = ((p1.x * sr1) + (p2.x * sr2) + (p3.x * sr3));
    var longitude = ((p1.y * sr1) + (p2.y * sr2) + (p3.y * sr3));
	
	return {
		x: longitude, 
		y: latitude, 
		z: 0
	};
}