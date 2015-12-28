/*
 * 		http://www.leolol.com/tutorials/graphics_tutorials.php
 * 		http://www.java-tips.org/other-api-tips/jogl/texture-mapping-nehe-tutorial-jogl-port.html
 * 		http://www.repeatwhiletrue.com/p5/P5JOGL.html
 */

package src.testapp;

import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

import java.util.ArrayList;

import javax.media.opengl.GL;

public class ParticleSystem  
{
	ArrayList particles;
	float destinationScale;
	PApplet parent;
	
	ParticleSystem( PApplet p, float destScale)
	{
		parent = p;
		destinationScale = destScale;
	    particles = new ArrayList();
	}
	
	void addParticle(Particle p)
	{
		Particle temp = p;
	    particles.add(temp);
	}
	
	void initTexture()
	{
		
	}
	
	void update()
	{
		for(int i=particles.size()-1;i>1; i--)
	    {			
	        Particle p = (Particle)particles.get(i);
	        p.update();
	        PVector position = p.location;
	        PVector prePosition = p.previousLocation;
	        PGraphicsOpenGL pgl = (PGraphicsOpenGL) parent.g;  // g may change
	        GL gl = pgl.beginGL();
	        gl.glPushMatrix();
	  		gl.glTranslatef(position.x, position.y, position.z);                 // Move left 1.5 units, up 1.5 units, and back 8 units
	        gl.glEnable(GL.GL_BLEND);
	        gl.glDisable(GL.GL_DEPTH_TEST);
	        gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE);						

	  		gl.glBegin(GL.GL_TRIANGLES);							// Begin drawing triangle sides

	   gl.glColor4f( 1.0f, 0.0f, 0.0f, 0.5f);					// Set colour to red
	   gl.glVertex3f( 0.0f, 1.0f, 1.0f);						// Top vertex
	   gl.glVertex3f(-1.0f,-1.0f, 0.0f);						// Bottom left vertex
	   gl.glVertex3f( 1.0f,-1.0f, 0.0f);						// Bottom right vertex

	   gl.glColor4f( 0.0f, 1.0f, 0.0f,  0.5f);						// Set colour to green
	   gl.glVertex3f( 0.0f, 1.0f, 1.0f);						// Top vertex
	   gl.glVertex3f(-1.0f,-1.0f, 2.0f);						// Bottom left vertex
	   gl.glVertex3f( -1.0f,-1.0f, 0.0f);						// Bottom right vertex

	   gl.glColor4f( 0.0f, 0.0f, 1.0f,  0.5f);						// Set colour to blue
	   gl.glVertex3f( 0.0f, 1.0f, 1.0f);						// Top vertex
	   gl.glVertex3f(-1.0f,-1.0f, 2.0f);						// Bottom left vertex
	   gl.glVertex3f( 1.0f,-1.0f, 2.0f);						// Bottom right vertex

	   gl.glColor4f( 0.5f, 0.0f, 0.5f,  0.5f);						// Set colour to purple
	   gl.glVertex3f( 0.0f, 1.0f, 1.0f);						// Top vertex
	   gl.glVertex3f( 1.0f,-1.0f, 0.0f);						// Bottom left vertex
	   gl.glVertex3f( 1.0f,-1.0f, 2.0f);						// Bottom right vertex

	 gl.glEnd();									// Finish drawing triangle sides

	 gl.glBegin(GL.GL_QUADS);							// Begin drawing square bottom

	   gl.glColor4f( 1.0f, 1.0f, 0.0f,  0.5f);		// Set colour to yellow
	   gl.glVertex3f(-1.0f,-1.0f, 0.0f);						// Bottom left vertex
	   gl.glVertex3f(-1.0f,-1.0f, 2.0f);						// Top left vertex
	   gl.glVertex3f( 1.0f,-1.0f, 2.0f);						// Bottom right vertex
	   gl.glVertex3f( 1.0f,-1.0f, 0.0f);						// Top right vertex

	 gl.glEnd();							

	  // Finish drawing triangles
	    gl.glPopMatrix(); 
	      
	    }
	}
}
