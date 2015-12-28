package src.testapp;
import processing.core.PApplet;
import processing.core.PConstants;
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
	PVector distance;
	
	
	float mass;
	float aValue; //Alpha
	float bValue; //Brightness
	float hValue; //Hue 
	float sValue; //Saturation
	float magnitude; //Magnitude
	float noiseTime;
	float angle;
	float phi_angle;
	PApplet parent;

	
	Particle(PApplet p, PVector start, PVector dest)
	{
		parent   	 = p;
		angle 	     = 5;
		phi_angle 	 = 5;
		velocity	 = new PVector(parent.random((float)0.2,(float)4.0),parent.random((float)0.2,(float)4.0),parent.random((float)0.2,(float)4.0));
	    acceleration = new PVector(0,0,0);
	    location 	 = start.get();
	    destination  = dest.get();
	    magnitude    = 5;
	    h 			 = (float) 0.2;
	    mass 		 = 1;
	    forceScale	 = (float)0.3;
	    noiseTime 	 = 0.0f;   
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
	    velocity.mult((float) 0.59);
	    //velocity.normalize();
	    
	    previousLocation = location;
	    location.add(PVector.mult(velocity, h));
	    acceleration.mult(0);
	} 
	
	public void setDestination(float destScale)
	{
		//destination.mult(destScale);
	}
	
	public void addPos()
	{
		location.x += magnitude * parent.cos(angle);
	    location.y += magnitude * parent.sin(angle);
	    
	    //location.x += magnitude * parent.sin(phi_angle) * parent.cos(angle);
	    //location.y += magnitude * parent.sin(phi_angle) * parent.sin(angle);
	    //location.z += magnitude * parent.cos(phi_angle);
	}
	    
	
	void gravitate(Particle Z)
	{
		//finding rho x^2 y ^2 z^2= rho then* cos(degrees);
		//3D vector calc
		//x = RHO * sin(PHI)*COS(theta);
		//y = RHO * sin(PHI)*sin(theta);
		//z = RHO * cos(phi)
		
	     
		float F, THETA;;
	    F = 5 * 1;
	   
	    PVector zVector = Z.destination;
	    zVector.sub(location);
	    
	    THETA = findTheta( zVector );
	    //RHO = findRho( zVector );
	    //PHI = findPhi( zVector );
	    
	    //regular coordinates
	    
	    zVector.x = F * parent.cos(THETA);
	    zVector.y = F * parent.sin(THETA);
	    
	    /*zVector.x = F * parent.cos(THETA);
	    zVector.y = F * parent.sin(THETA);
	    zVector.z = F * parent.cos(PHI);*/
	    
	    //regular formula
	    
	    zVector.x += magnitude * parent.cos(angle);
	    zVector.y += magnitude * parent.sin(angle);
	    
	    /*zVector.x += magnitude * parent.cos(angle);
	    zVector.y += magnitude * parent.sin(angle);
	    zVector.z += magnitude * parent.cos(phi_angle);*/
	   
	    //M = zVector.mag();
	    
	    //Regular formula for magnitude
	    magnitude = parent.sqrt( parent.sq(zVector.x) + parent.sq(zVector.y));
	    
	    //magnitude = parent.sqrt( parent.sq(zVector.x) + parent.sq(zVector.y) );
	    angle  	  = findTheta( zVector );
	   // phi_angle = findPhi( zVector );
				
	}
	
	float findTheta(PVector nLocation)
	{
		float theta;
	    theta = parent.atan( nLocation.y / nLocation.x );
	    if( nLocation.x < 0  &&  nLocation.y >= 0 ){theta += parent.PI;}
	    if( nLocation.x < 0  &&  nLocation.y < 0 ){theta -= parent.PI;}
	    return theta;
	}
	
	/*float findPhi(PVector nLocation)
	{
		float phi;
	    phi = parent.atan( nLocation.z / nLocation.y );
	    if( nLocation.x < 0  &&  nLocation.y >= 0 ){phi += parent.PI;}
	    if( nLocation.x < 0  &&  nLocation.y < 0 ){phi -= parent.PI;}
	    return phi;
	}
	
	float findRho(PVector nLocation)
	{
		float RHO;
		RHO = parent.sq(nLocation.x) + parent.sq(nLocation.y) + parent.sq(nLocation.z);
		return RHO;
	}*/
	
	void deteriorate()
	{
		magnitude *= 0.925;
	}
}
