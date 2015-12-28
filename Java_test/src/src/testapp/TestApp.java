package src.testapp;

import java.util.ArrayList;

import processing.core.*;

import peasy.*;
import processing.core.PFont;
import saito.objloader.*;
import controlP5.*;
import fullscreen.FullScreen;

//we need distance values to change the growth of the face
//face needs to be slightly more detailed
//growing process needs to be slightly different
//we need something around the faces 


public class TestApp extends PApplet
{
	
	public static void main(String _args[]) 
	{
		PApplet.main(new String[] { src.testapp.TestApp.class.getName() });
	}
	
	OBJModel model, model_2;
	float rotX, rotY;
	boolean clickFlag;
	float scaleValue, detailValue, scaleValue_2, detailValue_2;

	ArrayList vertices, vertices2;

	ParticleSystem ps, ps_2; 
	PFont mFont;

	PeasyCam cam;

	ControlP5 control;
	ControlWindow controlWindow;
	FullScreen fs;
	
	public void setup()
	{

		  
	  cam = new PeasyCam(this,200);
	  cam.setMinimumDistance(100);
	  cam.setMaximumDistance(1500);

	 
	  mFont = createFont("ArialRoundedMTBold-36",48);
	  size(1440,900, OPENGL);
	  background(0);
	  frameRate(120);
	//  model   = new OBJModel(this, "alyson_laugh.obj", "absolute", TRIANGLES);
	  model   = new OBJModel(this, "alyson_scared.obj", "absolute", TRIANGLES);
	//  model   = new OBJModel(this, "alyson_crying.obj", "absolute", TRIANGLES);
	  model_2 = new OBJModel(this,"ryan_scared.obj","absolute", TRIANGLES );
	  
	  //model.enableDebug();
	  
	  smooth();
	  colorMode(HSB);  
	  strokeWeight(4);
	  
	  model.scale(800);
	  model.translateToCenter();
	  model_2.scale(900);
	  model_2.translateToCenter();
	  
	  detailValue   = 3;
	  detailValue_2 = 6;
	  scaleValue    = 10;
	  scaleValue_2  = 10;
	  vertices 	    = new ArrayList();
	  vertices2     = new ArrayList();
	  ps 		    = new ParticleSystem(this, scaleValue);
	  ps_2 		    = new ParticleSystem(this, scaleValue_2);
	  
	  PVector averagePosition  = new PVector(0,0,0);
	  PVector averagePosition2 = new PVector(0,0,0);
	  
	  for( int i = 0; i < model.getVertexCount(); i += detailValue)
	  {
		  PVector destinationPoint  = model.getVertex(i);
		  
		  vertices.add( destinationPoint );
		  
		  averagePosition.add( destinationPoint );
	  }
	  
	  averagePosition.div(vertices.size());
	  
	  
	  cam.lookAt( averagePosition.x, averagePosition.y, averagePosition.z );
	  
	 for(int i = 0; i < vertices.size(); i++)
	  {
	    PVector destination   = (PVector) vertices.get(i);
	    Particle p  = new Particle( this, averagePosition, destination );
	    ps.addParticle( p );
	  }

	  fs = new FullScreen(this);
	  fs.enter();
	}
	
	public void mouseDragged()
	{
	    rotX += (mouseX - pmouseX) * 0.01;
	    rotY -= (mouseY - pmouseY) * 0.01;
	}

	public void draw()
	{
		background(0);
		frame.setTitle(str((int)frameRate));
		ps.update();
		ps_2.update();
	}
}