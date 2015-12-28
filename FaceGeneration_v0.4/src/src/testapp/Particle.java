package src.testapp;
import processing.core.PApplet;
import processing.core.PVector;

public class Particle 
{
	float h;
	float forceScale;
	PVector location;
	PVector velocity;
	PVector acceleration;
	PVector destination;
	PVector previousLocation;
	
	float mass;
	float aValue; //Alpha
	float bValue; //Brightness
	float hValue; //Hue 
	float sValue; //Saturation
	
	float noiseTime;
	PApplet parent;

	
	Particle(PApplet p, PVector start, PVector dest)
	{
		parent = p;
		
		velocity = new PVector(parent.random((float)0.2,(float)4.0),parent.random((float)0.2,(float)4.0),parent.random((float)0.2,(float)4.0));
	    acceleration = new PVector(0,0,0);
	    location = start.get();
	    destination = dest.get();
	    h = (float) 0.2;
	    mass = 1;
	    forceScale = (float)0.3;
	    noiseTime = 0.0f;
	}
	
	public void addNoise()
	{
		PVector noiseVector = new PVector(parent.noise(noiseTime), parent.noise(noiseTime), parent.noise(noiseTime));
	    acceleration.add(noiseVector);
	    noiseTime += 0.01;
	}
	public void update()
	{
		acceleration = PVector.sub(destination, location);
	    addNoise();
	    acceleration.mult(forceScale);
	    velocity.add(acceleration); 
	    velocity.normalize();
	    previousLocation = location;
	    location.add(PVector.mult(velocity, h));
	    acceleration.mult(0);
	} 
	
	public void setDestination(float destScale)
	{
		destination.mult(destScale);
	}
	
	public PVector getSomething()
	{
		return this.velocity;
	}
}
