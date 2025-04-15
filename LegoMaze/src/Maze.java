import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Maze {
	
	 Wheel wheel1 = WheeledChassis.modelWheel(Motor.A, 56.0).offset(-65);
	 Wheel wheel2 = WheeledChassis.modelWheel(Motor.B, 56.0).offset(65);
	 Chassis chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
	 MovePilot pilot = new MovePilot(chassis);
	 
	 // This sensor can identify 8 unique colors (NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN). 
	 EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S3);
		
	 //ArrayList<String> modes = cs.getAvailableModes(); // ColorID, RGB, Red, or Ambient
		
	 SensorMode color = colorSensor.getRGBMode();// set to RGB mode
	 float[] RGBSample = new float[color.sampleSize()];
		
	 SensorMode colorID = colorSensor.getColorIDMode();// set to ColorID mode
	 float[] colorIDSample = new float[colorID.sampleSize()];
	 
	 EV3UltrasonicSensor sonicSensor = new EV3UltrasonicSensor(SensorPort.S4);
		
	 // get an instance of this sensor in measurement mode
	 SampleProvider distance= sonicSensor.getMode("Distance");
		  
	 // initialize an array of floats for fetching samples. 
	 // Ask the SampleProvider how long the array should be
	 float[] distanceSample = new float[distance.sampleSize()]; // in meters
     
	public static void main(String[] args) {
		
		LCD.drawString("Press any button", 0, 1);
		LCD.drawString("   to start", 0, 2);
		Button.waitForAnyPress();
		LCD.clear();
		
		Maze myMaze = new Maze();
	    
	    myMaze.goMaze();
	}
	
	public void goMaze() {
		
		colorSensor.setFloodlight(true); // can specify color of light to turn on
		
		pilot.setAngularSpeed(100);
		pilot.setLinearSpeed(60);
		pilot.setAngularAcceleration(100);
		
	    // boolean's not1stWall & not2ndWall will check for 'walls'
	    boolean not1stWall = true;
	    boolean not2ndWall = true;
	    
	    // notAtBlack will check for if at black strip or not
	    boolean notAtBlack = true;
	    
	    // foundG, foundY, & foundB will be a check for the 3 squares in the red box after it traverses the course
	    boolean foundG = false;
	    boolean foundY = false;
	    boolean foundB = false;
	    
	    while (not1stWall) {
	    	// fetches distance sample
	    	distance.fetchSample(distanceSample, 0);
	    	LCD.drawString("Dist: " + distanceSample[0]*100,0,3); // multiply by 100 to convert to cm
	    	if (distanceSample[0]*100 > 25) { // while distance > 25
	    		// fetches color sample
	    		colorID.fetchSample(colorIDSample, 0);
	    		int colorValue = (int)colorIDSample[0];
	    		if (colorValue == Color.BLUE) { // if color == blue
	    			// move robot forwards
	    			pilot.travel(15);
	    		} else {
	    			searchForBlue();
	    		}
	    	} else {
	    		// at the first wall
    			not1stWall = false;
	    		Sound.beep();
    		}
	    }
	    
	    // how far to spin to find blue again
	    pilot.travel(130);
	    pilot.rotate(50);
	    
	    while (not2ndWall) {
	    	// fetches distance sample
	    	distance.fetchSample(distanceSample, 0);
	    	LCD.drawString("Dist: " + distanceSample[0]*100,0,3); // multiply by 100 to convert to cm
	    	colorID.fetchSample(colorIDSample, 0);
    		int colorValue = (int)colorIDSample[0];
	    	if (distanceSample[0]*100 > 25 || colorValue != Color.RED) { // while distance > 25
	    		// fetches color sample
	    		colorID.fetchSample(colorIDSample, 0);
	    		int colorValue2 = (int)colorIDSample[0];
	    		if (colorValue2 == Color.BLUE) { // if color == blue
	    			// move robot forwards
	    			pilot.travel(15);
	    		} else {
	    			searchForBlue();
	    		}
	    	} else {
	    		// at the second wall
    			not2ndWall = false;
	    		Sound.beep();
    		}
	    }
	    
	    pilot.rotate(110);
	    
	    while (notAtBlack) {
	    	// fetches distance sample
	    	distance.fetchSample(distanceSample, 0);
	    	LCD.drawString("Dist: " + distanceSample[0]*100,0,3); // multiply by 100 to convert to cm
	    	colorID.fetchSample(colorIDSample, 0);
    		int colorValue = (int)colorIDSample[0];
	    	if (colorValue != Color.BLACK) { // while distance > 25
	    		// fetches color sample
	    		colorID.fetchSample(colorIDSample, 0);
	    		int colorValue2 = (int)colorIDSample[0];
	    		if (colorValue2 == Color.BLUE) { // if color == blue
	    			// move robot forwards
	    			pilot.travel(15);
	    		} else {
	    			searchForBlueReverse();
	    		}
	    	} else {
	    		// at black strip
    			notAtBlack = false;
	    		Sound.beep();
	    		//pilot.rotate(180);
    		}
	    }
	    
	    if (!notAtBlack) {
	    	
	    	pilot.rotate(-65);
	   
	    	while (!foundB || !foundY || !foundG) {
	    		
	    		pilot.travel(25);
	    		
	    		// will rotate backwards if red is found
		    	colorID.fetchSample(colorIDSample, 0);
	    		int colorValue = (int)colorIDSample[0];
	    		if (colorValue == Color.RED) {
	    			pilot.rotate(65);
	    		}
	    		
	    		// will beep once if blue is found
		    	colorID.fetchSample(colorIDSample, 0);
	    		int colorValue2 = (int)colorIDSample[0];
	    		if (colorValue2 == Color.BLUE) {
	    			if (!foundB) {
	    				Sound.beep();
	    				foundB = true;
	    			}
	    			
	    		}
	    		
	    		// will beep twice if yellow is found
		    	colorID.fetchSample(colorIDSample, 0);
	    		int colorValue3 = (int)colorIDSample[0];
	    		if (colorValue3 == Color.YELLOW) {
	    			if (!foundY) {
	    				Sound.beep();
	    				Sound.beep();
	    				foundY = true;
	    			}
	    		}
	    		
	    		// will beep thrice if green is found
		    	colorID.fetchSample(colorIDSample, 0);
	    		int colorValue4 = (int)colorIDSample[0];
	    		if (colorValue4 == Color.GREEN) {
	    			if (!foundG) {
	    				Sound.beep();
	    				Sound.beep();
	    				Sound.beep();
	    				foundG = true;
	    			}
	    		}
	    	}
	    }
	    
		
		LCD.clear(); 
		pilot.stop();
		colorSensor.close();
	}
	
	public void searchForBlue() {
		
		int deg = 5;
		int angle = 1;
		
		colorID.fetchSample(colorIDSample, 0);
		int colorValue = (int)colorIDSample[0];
		while (colorValue != Color.BLUE) {
			
			pilot.rotate(angle * deg);
		
			deg += 5;
			angle *= -1;
			colorID.fetchSample(colorIDSample, 0);
			colorValue = (int)colorIDSample[0];
		}	
	}
	
	public void searchForBlueReverse() {
		
		
		int deg = 5;
		int angle = -1;
		
		colorID.fetchSample(colorIDSample, 0);
		int colorValue = (int)colorIDSample[0];
		while (colorValue != Color.BLUE) {
			
			pilot.rotate(angle * deg);
		
			deg += 5;
			angle *= -1;
			colorID.fetchSample(colorIDSample, 0);
			colorValue = (int)colorIDSample[0];
		}	
	}
}


