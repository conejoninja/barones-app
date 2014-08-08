difference(){
	difference(){
		difference(){
			difference(){
				// MAIN BOX
					difference() {
						union(){
							cube([64,64,26], true);
							// MOUNTH TUBE
							translate([-42,-16,2]){
								rotate([00,90,0]) {
									cylinder(25,5,7);
								}
							}
						}
						translate([0,0,2]){cube([60,60,24], true);}
					}
				// MQ303A
				translate([-48,-16,2]){
					rotate([0,90,0]) {
						cylinder(30,4,4);
					}
				}
			}
			//POWER SWITCH
			translate([25,20,0]){
				rotate([0,90,0]) {
					cylinder(10,3,3);
				}
				}
		}
		//MINI USB BATTERY CHARGER
		translate([28,-11,-2]){
			cube([11,10,8.5],true);
		}
	}
	// LEDS
	union() {
		// POWER LED
		translate([-20,23,-15]) {
			cylinder(10,1.6,1.6);
		}			
		// BLUETOOTH LED
		translate([-10,23,-15]) {
			cylinder(10,1.6,1.6);
		}			
	}
}

//ADDITIONAL FEATURES TO HELP PRINTING AND AVOID WARPING
withsupport();
withmouseears();




// SUPPORTS FOR PRINTING
module withsupport() {
	translate([-42,-14,-13]) {
		cube([10,1,11]);
	}
	translate([-42,-19,-13]) {
		cube([10,1,11]);
	}
}



// MOUSE EARS TO HELP AVOID WARPING
module withmouseears() {
	mouseear(33,33,-13);
	mouseear(33,-33,-13);
	mouseear(-33,-33,-13);
	mouseear(-33,33,-13);
}



module mouseear(x,y,z) {
	translate([x,y,z]) {
		cylinder(2,6,6);
	}
}