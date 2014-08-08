cube([64,64,2], true);
difference(){
	translate([0,0,3]) {
		cube([60,60,4], true);
	}
	translate([0,0,3]) {
		cube([56,56,5], true);
	}
}

withmouseears();

// MOUSE EARS TO HELP AVOID WARPING
module withmouseears() {
	mouseear(33,33,-1);
	mouseear(33,-33,-1);
	mouseear(-33,-33,-1);
	mouseear(-33,33,-1);
}



module mouseear(x,y,z) {
	translate([x,y,z]) {
		cylinder(1,6,6);
	}
}