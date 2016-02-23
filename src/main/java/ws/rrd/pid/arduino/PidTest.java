package ws.rrd.pid.arduino;


import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  09.02.2012::20:47:34<br> 
 */
public class PidTest extends TestCase {

	private double analog = Math.random()*255.0;//;
	double koefficient = 21.0/33.0;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	
	public void testBasic() {
		/********************************************************
		 * PID Basic Example Reading analog input 0 to control analog PWM output
		 * 3
		 ********************************************************/

		// #include <PID_v1.h>

		// Define Variables we'll be connecting to
		double Setpoint = 100;
		double Input = 1;
		double Output = -1;

		// Specify the links and initial tuning parameters
		// PID myPID(&Input, &Output, &Setpoint,2,5,1, DIRECT);
		Pid myPID = new Pid(Input, Output, Setpoint, .123, .135, .012, Pid.DIRECT);

		// void setup()
		{
			// initialize the variables we're linked to
			Input = analogRead(0);
			Setpoint = 100.0;

			// turn the PID on
			myPID.setMode(Pid.AUTOMATIC);
		}

		// void loop()
		for (int i = 0; i < 100; i++) {
			Input = analogRead(0);
			// myPID.myInput = Input;
			Output = myPID.Compute(Input);
			// Output = myPID.myOutput;
			System.out.println("I:" + Input + "   O:" + Output
					+ "   Setpoint-=" + (Setpoint - Input));
			analogWrite(3, Output);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.  printStackTrace();
			}
		}
		assertTrue("" + (Setpoint - Input), Math.abs(Setpoint - Input) < 6);

	}

	private void analogWrite(int ignoredPar, double output) { 
		analog += output * koefficient;
	}

	private double analogRead(int ignoredPar) {
		return analog;
	}
	
	
}


 