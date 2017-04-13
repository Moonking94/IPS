/**
 * Notes
 * 
 * Solution: x0 = (1/delta) * (2B(x1-x2)-2A(x1-x3)), y0 = (1/delta) * (2A(y1-y3)-2B(y1-y2))
 * delta = 4*((x1-x2)(y1-y3)-(x1-x3)(y1-y2))
 * A = r2²-r1²-x2²+x1²-y2²+y1²
 * B = r3²-r1²-x3²+x1²-y3²+y1²
 * 
 * @param {Object} p1 Point and distance: { x, y, z, r }
 * @param {Object} p2 Point and distance: { x, y, z, r }
 * @param {Object} p3 Point and distance: { x, y, z, r }
 * @return {Object} { x, y, z }
 */
function trilateratev2(p1, p2, p3) 
{
    function getDelta(a, b, c)
    {
        return 4 * ((a.x - b.x) * (a.y - c.y) - (a.x - c.x) * (a.y - b.y));
    }

    function getA(a, b, c)
    {
        return Math.pow(b.r, 2) - Math.pow(a.r, 2) - Math.pow(b.x, 2) + Math.pow(a.x, 2) - Math.pow(b.y, 2) + Math.pow(a.y, 2);
    }
    
    function getB(a, b, c)
    {
        return Math.pow(c.r, 2) - Math.pow(a.r, 2) - Math.pow(c.x, 2) + Math.pow(a.x, 2) - Math.pow(c.y, 2) + Math.pow(a.y, 2);
    }

    function getPoint()
    {
        return {x: (1/getDelta(p1, p2, p3)) * (2 * getA(p1, p2, p3) * (p1.y - p3.y) - 2 * getB(p1, p2, p3) * (p1.y - p2.y)),
				y: (1/getDelta(p1, p2, p3)) * (2 * getB(p1, p2, p3) * (p1.x - p2.x) - 2 * getA(p1, p2, p3) * (p1.x - p3.x))};
    }

    a = getPoint();

    return a;
}