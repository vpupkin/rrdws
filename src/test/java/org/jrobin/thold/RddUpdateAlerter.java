package org.jrobin.thold;

import java.io.IOException;

import org.jrobin.core.ConsolFuns;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;

/**
 * <b>Description:TODO</b>
 * 
 * @author vipup<br>
 * <br>
 *         <b>Copyright:</b> Copyright (c) 2006-2008 Monster AG <br>
 *         <b>Company:</b> Monster AG <br>
 * 
 *         Creation: 01.09.2011::16:48:15<br>
 */
public abstract class RddUpdateAlerter extends AbstractAlerter {
	private Sample sample;

	private RrdDb rrdDb;

	private void init(String rrdPar) throws RrdException, IOException {
		RrdDef rrdDef = new RrdDef(rrdPar);
		long startTime = 920800000L; // 920800000L == [Sun Mar 07 10:46:40 CET
										// 1999]

		rrdDef.setStartTime(startTime);
		rrdDef.setStep(1);
		rrdDef.addDatasource("speed", "GAUGE", 600, Double.NaN, Double.NaN);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 288, 775);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 288, 775);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 288, 775);
		rrdDb = new RrdDb(rrdDef);
		sample = rrdDb.createSample();

	}

	public RddUpdateAlerter(String rrdName, double baseLine,
			long activationTimeoutInSeconds) {
		this.rrdName = rrdName;
		this.baseLine = baseLine;
		this.activationTimeoutInSeconds = activationTimeoutInSeconds;
		try {
			init(this.rrdName + ".stat.rrd");
		} catch (RrdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void performAction(long timestamp) {
		try {
			long activatingTimepoint = this.inIncidentTime()
					+ this.getSpanLength();
			boolean isActivated = timestamp > activatingTimepoint;
			int lowLevel = isActivated ? 44 : 111;
			String valTmp = ""
					+ (this.IncidentTime == -1 ? lowLevel : this.baseLine);
			this.sample.setAndUpdate("" + (timestamp) + ":" + valTmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RrdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void performSleep(long timestamp) {
		try {
			long activatingTimepoint = this.inIncidentTime()
					+ this.getSpanLength();
			boolean isActivated = timestamp > activatingTimepoint;
			int lowLevel = isActivated ? 0 : -111;
			String valTmp = ""
					+ (this.IncidentTime == -1 ? lowLevel : lowLevel);
			this.sample.setAndUpdate("" + (timestamp) + ":" + valTmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RrdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
