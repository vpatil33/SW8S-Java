package org.aquapackrobotics.sw8s.states;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.aquapackrobotics.sw8s.trainingsim.SimWindow;

/**
 * Moves robot 3/4 of a circle with the given radius
 */
public class RotateState extends SimState {
	
	//Initial X position of the robot
	private double initialX;
	//Initial Y position of the robot
	private double initialY;
	//Target X position of the robot
	private double targetX;
	//Target Y position of the robot
	private double targetY;
	//Radius of circle to "draw"
	private double targetRadius;
	//Multiplier for setting velocities
	private final double kP = 1.5;
	//Minimum distance from target position to be considered done
	private final double kError = 5.0;
	
    public RotateState(ScheduledThreadPoolExecutor pool, SimWindow sim, double radius) {
        super(pool, sim);
        targetRadius = radius;
    }

    public void onEnter() {
    	window.setRobotSpeed(0.0, 0.0, 0.0);
    	initialX = window.getXPos();
    	initialY = window.getYPos();
    	
    	targetX = initialX - targetRadius;
    	targetY = initialY + targetRadius;
    	
    }

    /**
     * Robot goes to 3 different points to "draw" the outside of the G
     * The targetRadius field is used to make an imaginary circle on which the 3 points are found
     * P0 is the current position of the robot
     *         oP0o
     *     	o        o
     *     o          o
     *    P1		  P3
     *     o          o
     *      o        o
     *         oP2o   
     */
    public boolean onPeriodic() {
    	//Calculates xVelocity based on current point
    	double xVelocity;
    	if (targetX == initialX - targetRadius) //P1
    		xVelocity = -Math.abs(window.getXPos() - targetX) / targetRadius;
    	else if (targetX == initialX + targetRadius) //P3
    		xVelocity = Math.abs(window.getXPos() - targetX) / targetRadius;
    	else //P2
    		xVelocity = 1 - Math.abs(window.getXPos() - targetX) / targetRadius;
    	
    	//Calculates yVelocity based on current point
    	double yVelocity;
    	//Since targetY = initialY for P1 & P3, the second condition is to distinguish between the two
    	if (targetY == initialY + targetRadius && Math.signum(window.getYPos() - targetY) == -1.0) //P1
    		yVelocity = 1.0 - ( Math.abs(window.getYPos() - targetY) / targetRadius ) + 0.05;
    	else if (targetY == initialY + targetRadius * 2) //P2
    		yVelocity = Math.abs(window.getYPos() - targetY) / targetRadius;
    	else { //P3
    		yVelocity = 1.0 - ( Math.abs(window.getYPos() - targetY) / targetRadius );
    		yVelocity = -yVelocity;
    	}
    	
    	//Calculate targetAngle based on X and Y velocities
    	double targetAngle = Math.toDegrees(Math.atan(yVelocity / xVelocity)) - 90;
    	//If robot is going to P2 or P3, then adjusts angle to account for difference in atan() angles
    	if (Math.signum(xVelocity) == 1.0)
    		targetAngle = targetAngle - 180;
    	double currentAngle = window.getRobotAngle();
    	
    	//Sets robot speed with calculated velocities
    	window.setRobotSpeed(xVelocity * kP, yVelocity * kP, (targetAngle - currentAngle));
    	
    	//If the robot has reached a point, this conditional switches to the next one (or exits if done)
    	if (Math.abs(window.getXPos() - targetX) < kError && Math.abs(window.getYPos() - targetY) < kError) {
    		if (targetX == initialX && targetY == initialY + targetRadius * 2) { //Switches to Point 3
    			//System.out.println("Switching to point 3");
    			targetX = initialX + targetRadius;
    			targetY = initialY + targetRadius;
    		}
    		else if (targetX == initialX + targetRadius) { //Done with drawing part of circle, returns false
    			//System.out.println("Done");
    			return false;
    		}
    		else { //Switches to Point 2
    			//System.out.println("Switching to point 2");
    			targetX = initialX;
    			targetY = initialY + targetRadius * 2;
    		}
    	}
    	
    	
    	
    	/*
    	if (Math.abs(window.getXPos() - targetX) < kError) {
    		if (targetX == initialX)
    			targetX = initialX + targetRadius;
    		else if (targetX == initialX + targetRadius)
    			return false;
    		else
    			targetX = initialX;
    	}
    	
    	if (Math.abs(window.getYPos() - targetY) < kError) {
    		if (targetY == initialY + targetRadius * 2) {
    			System.out.println("orange");
    			targetY = initialY + targetRadius;
    			goingUp = true;
    		}
    		else
    			targetY = initialY + targetRadius * 2;
    	}
    	*/
    	
    	return true;
    }

    public void onExit() {
    	window.setRobotSpeed(0.0, 0.0, 0.0);
    }
    
    /**
     * Returns an instance of RotateLeftState
     */
    public State nextState() {
        return new RotateLeftState(pool, window);
    }
}
