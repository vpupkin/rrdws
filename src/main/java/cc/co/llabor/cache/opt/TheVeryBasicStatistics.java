package cc.co.llabor.cache.opt;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.CacheStatistics;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.11.2012::12:20:55<br> 
 */
public class TheVeryBasicStatistics implements  CacheStatistics{

	final static Map<String, CacheStatistics> statRepo = new HashMap<String, CacheStatistics>();
	
	private TheVeryBasicStatistics (String namePar){
		CacheStatistics oldOne = statRepo.put(namePar, this);
		if (oldOne !=null){
			System.out.println("TheVeryBasicStatistics["+namePar+"] was recreated!");
		}
	}

	public static TheVeryBasicStatistics getInstance(String namePar) { 
		return new TheVeryBasicStatistics (namePar) ;
	}

	private int hits = 0;
	private int miss = 0;
	private int oCount = 0;

	public int getStatisticsAccuracy() { 
		return CacheStatistics.STATISTICS_ACCURACY_NONE;
	}

	
	public void hit(){
		hits++;
	}
	
	public void miss(){
		miss++;
	}
	
	public void postAdd(){
		oCount++;
	}
	
	public void postDel(){
		oCount--;
	}
	
	public int getObjectCount() { 
		return oCount ;  
	}

	public int getCacheHits() { 
		return this.hits ; 
	}

	public int getCacheMisses() {
		return this.miss  ; 
	}

	public void clearStatistics() {  
		hits = 0;
		miss = 0;
	}

}


 