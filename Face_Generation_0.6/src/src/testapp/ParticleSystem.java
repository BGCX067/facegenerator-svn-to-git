/*		Useful tutorials and examples
 * 		http://www.leolol.com/tutorials/graphics_tutorials.php
 * 		http://www.java-tips.org/other-api-tips/jogl/texture-mapping-nehe-tutorial-jogl-port.html
 * 		http://www.repeatwhiletrue.com/p5/P5JOGL.html
 * 		http://benhem.com/games/GLP5align/
 */

package src.testapp;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class ParticleSystem  
{
	ArrayList particles;
	float destinationScale;
	PApplet parent;
	PGraphicsOpenGL pgl;
	GL gl;
	GLU glu;
	int particleShape;
	float aspect, camZ;
	ParticleSystem( PApplet p, float destScale)
	{
		parent = p;
		destinationScale = destScale;
	    particles = new ArrayList();
	    aspect=(float)parent.width/(float)parent.height;
	    camZ=(float) ((parent.height/2.0) / PApplet.tan((float) (PConstants.PI*60.0/360.0)));
	    initGL();
	}
	
	void addParticle(Particle p)
	{
		Particle temp = p;
	    particles.add(temp);
	}
	
	void initTexture()
	{
		
	}
	
	void initGL()
	{
		 glu = new GLU();
         pgl = (PGraphicsOpenGL) parent.g;  // g may change
         gl = pgl.beginGL();
         buildList();
         gl.glEnable(GL.GL_BLEND);
	     gl.glDisable(GL.GL_DEPTH_TEST);
	     gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);	
	     gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
	}
	
	void buildList()
	{
		particleShape = gl.glGenLists(1);
		gl.glNewList(particleShape, GL.GL_COMPILE);
		prepareParticleShape();
		gl.glEndList();
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		PApplet.println("display list created");

	}
	void prepareParticleShape()
	{
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

		   gl.glColor4f( 0.5f, 0.0f, 0.5f,  0.5f);					// Set colour to purple
		   gl.glVertex3f( 0.0f, 1.0f, 1.0f);						// Top vertex
		   gl.glVertex3f( 1.0f,-1.0f, 0.0f);						// Bottom left vertex
		   gl.glVertex3f( 1.0f,-1.0f, 2.0f);						// Bottom right vertex

		 gl.glEnd();												// Finish drawing triangle sides

		 gl.glBegin(GL.GL_QUADS);									// Begin drawing square bottom

		   gl.glColor4f( 1.0f, 1.0f, 0.0f,  0.5f);					// Set colour to yellow
		   gl.glVertex3f(-1.0f,-1.0f, 0.0f);						// Bottom left vertex
		   gl.glVertex3f(-1.0f,-1.0f, 2.0f);						// Top left vertex
		   gl.glVertex3f( 1.0f,-1.0f, 2.0f);						// Bottom right vertex
		   gl.glVertex3f( 1.0f,-1.0f, 0.0f);						// Top right vertex

		 gl.glEnd();							
		 
		 // Finish drawing triangles
		 PApplet.println("Shape prepared");
	}

	
	void update()
	{
		for(int i=particles.size()-1;i>1; i--)
	    {			
	        Particle p = (Particle)particles.get(i);
	        p.update();
	        PVector position = p.location;
	        PVector prePosition = p.previousLocation;
	       // PApplet.println("drawing at"+position);
	        //gl.glPushMatrix();
	        
	        gl.glMatrixMode(GL.GL_PROJECTION);  // tell GL that the camera is being set up now:
	        gl.glLoadIdentity();
            glu.gluPerspective(180/3.0, aspect, camZ/10.0, camZ*10.0); // these commands are analogous to the p5
	        glu.gluLookAt(0, 0, camZ, 0, 0, 0, 0, 1, 0);              // "perspective()" and "camera()," -- see reference
	        
	        
	        gl.glMatrixMode(GL.GL_MODELVIEW);
	        gl.glLoadIdentity();
	        
	        gl.glRotatef((float)parent.mouseY*360/(float)parent.height,-1,0,0); // perform the same rotations in the same order
	        gl.glRotatef((float)parent.mouseX*360/(float)parent.width,0,1,0);   // the Y-axis is "backwards" in GL
	        gl.glRotatef((float)parent.mouseX*360/(float)parent.width,0,0,-1);
	        
	  		gl.glTranslatef(position.x, position.y, position.z);
	        gl.glCallList(particleShape);
	        
	        gl.glPopMatrix(); 
	        
	        //PApplet.println("drawing");
	        //gl.glFlush();
	  		
	    }
		//gl.glFlush();
	}
}
