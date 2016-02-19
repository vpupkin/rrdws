package cc.co.llabor.threshold;
 
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet; 
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import net.sf.jsr107cache.Cache;

import org.jrobin.core.Datasource;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDbPool; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.rrd.csv.RrdKeeper;
import cc.co.llabor.cache.Manager;
import cc.co.llabor.features.Repo;
import cc.co.llabor.threshold.rrd.Threshold;
 

/**
 * <b>Description:TODO</b>
 * 
 * @author vipup<br>
 * <br>
 *         <b>Copyright:</b> Copyright (c) 2006-2008 Monster AG <br>
 *         <b>Company:</b> Monster AG <br>
 * 
 *         Creation: 30.08.2011::15:59:53<br>
 */
public class AlertCaptain implements Runnable, NotificationListener {



	private static final String TO_DO_PROPERTIES = "ToDo.properties";
	static boolean inited = false;

	public final static AlertCaptain getInstance() {
		ThreadGroup groupTmp = Thread.currentThread().getThreadGroup();
		return getInstance(groupTmp );
	}
	public final static AlertCaptain getInstance(ThreadGroup groupTmp) {
		if (!inited) {
			synchronized (Thread.class) {
				if (!inited) {
					if (myself.isAsync && !myself.isAlive) {
						System.out.println("starting ....");
						init(groupTmp);
						System.out.println("...done."); 	
					}
				}
			}
		}
		return myself;
	}
	public static void init(ThreadGroup groupTmp) {
		try{
			Thread th = groupTmp == null?
					new Thread(  myself, "AlertCaptain"):
					new Thread(groupTmp , myself, "AlertCaptain")							
				;
			th.setPriority(Thread.MAX_PRIORITY / 2
					+ Thread.MAX_PRIORITY / 4); // 75%
			myself.isAlive = true;
			th.start();
			inited = true;
		}catch(Throwable e){
			if (groupTmp !=null)init(null); 
			e.printStackTrace();
		}
	}

	private List<Threshold> ToDo = new ArrayList<Threshold>();
	public List<Threshold> getToDo() { 
			return ToDo;
	}

	private static final Logger log = LoggerFactory
			.getLogger(AlertCaptain.class.getName());
	private boolean isAlive = false;

	public boolean isAlive() {
		return isAlive;
	}
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	private java.util.Queue<CheckPoint> queue;
	private int wakeCounter;
	private boolean isAsync = false;
	private Throwable lastExc;
	public Throwable getLastExc() {
		return lastExc;
	}

	private long processStart;
	public long getProcessStart() {
		return processStart;
	}

	private long processEnd;
	private  Collection<Threshold> unmodifiableCollection = new HashSet<Threshold>();
	 

	public long getProcessEnd() {
		return processEnd;
	}
	public boolean isAsync() {
		return isAsync;
	}
	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}
	public int getWakeCounter() {
		return wakeCounter;
	}
	public int getQueueLength() {
		return queue.size();
	}
	public AlertCaptain() {
		this(new ConcurrentLinkedQueue<CheckPoint>());
	}
	AlertCaptain(Queue<CheckPoint> q) {
		isAsync = true;
		this.queue = q;
		log.info(Repo.getBanner("AlertCaptain"));
	}
	public void register(Threshold e) {
		boolean exist  = indexOf(e)>-1;  
// !!!FIXED@register!!!		if (1==1 ){
		if (!exist ){
			ToDo.add(e );
			NotificationListener listener = this;
			NotificationFilter filter = null;
			Object handback = null;
			RrdKeeper.getInstance().addNotificationListener(listener , filter , handback );
			syncUC();
		}
	}
	// ToDo.indexOf(activist)
	private int indexOf(Threshold e){
		int retval = -1;
		int count = -1;
		for (Threshold t: ToDo){
			count++;
			String tStr = t.toProperties().toString();
			String eStr = e.toProperties().toString();			
			if (eStr .equals(tStr)){
				retval = count;
				break;
			}
		}
		
		return retval ;
	}
	private boolean persist(Threshold e) {
		boolean exist  = indexOf(e)>=0;
		if (!exist){
			ToDo.add(e );
			syncUC(); 
			commit();
			
		}			
		return !exist;
	}
	/**
	 * 
	 * @author vipup
	 * @param activist
	 * @return true if element was removed from registered-Activist-List
	 */
	private boolean unpersist(Threshold activist) {
		synchronized (ToDo) { 
			syncUC();
			int index  = indexOf(activist);
			Threshold o = index>-1? ToDo.remove(index):null;
			syncUC();
			commit();
			return o !=null;
		}
		
	}
	
	private String storeWrapped(Threshold clPar ) {
		String nick = "rrw";
		try{
			nick  = toNICK(clPar.getClass().getName());
		}catch(Throwable e){}
		String retval = nick +"@" + clPar.getDatasource();
		Properties props = new Properties();
		String nameTmp = clPar.getDsName();
		nameTmp = nameTmp==null?"NONAME#"+System.currentTimeMillis():nameTmp;
		props.put(Threshold.DS_NAME, nameTmp);
		props.put(Threshold.CLASS , clPar.getClass().getName() );
		String monArgs = clPar.getMonitorArgs() ;
		monArgs =monArgs ==null?"-MARGS-":monArgs ;
		props.put(Threshold.MONITOR_ARGS, monArgs );
		try{
			props.put(Threshold.SPAN_LENGTH, "" + ((MVELActionist)clPar).notificationIntervalInSecs );
		}catch(Exception e){}
		String monAct = clPar.getAction() ;
		monAct=monAct==null?"-ACTION-":monAct ;
		
		props.put(Threshold.ACTION ,  monAct );
		String actionArgs  = clPar.getActionArgs() ;
		actionArgs  = actionArgs  ==null?"-actionArgs-":actionArgs  ;
		props.put(Threshold.ACTION_ARGS , actionArgs  );
		
		String childTmp =retval;
 		try {
 			AlertCaptain.storeToName(childTmp, props);
		} catch (TholdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		return retval;
	}
	
	
	private void commit(){
		int counterTmp = 0;
		for (Threshold theNext: list() ){
			try {
				storeToName(""+counterTmp, theNext);
				 
			} catch (TholdException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
			 
				storeWrapped( theNext);
			} catch ( Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			counterTmp++;
		}
		
	}
	
	public final void init(){
		try {
			load();
		} catch (TholdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private final void load() throws TholdException{
		Cache cTmp = Manager.getCache(cacheNS);
		Object tholdProps = cTmp.get(TO_DO_PROPERTIES); 
		// same as at the StartStopServet.init(...)
		Threshold watchDog  = /*ac.*/toThreshold(tholdProps );
		/*ac.*/register(  watchDog );
		lookInsideThold(tholdProps);
	}
	

	
	/**
	 * use the log4j-similar semantic for tholds-definition:
	 * 
	 * 
	 * #CacheEntry stored at 1317918241876
	 * #Thu Oct 06 18:24:01 CEST 2011
	 * datasource=test.rrd
	 * dsName=speed
	 * actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * spanLength=600
	 * class=cc.co.llabor.threshold.Log4JActionist
	 * action=log4j
	 * BaseLine=0.0
	 * monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * monitorType=mvel
	 * A.datasource=test.rrd
	 * A.dsName=speed
	 * A.actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * A.spanLength=600
	 * A.class=cc.co.llabor.threshold.Log4JActionist
	 * A.action=log4j
	 * A.BaseLine=1.0
	 * A.monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * A.monitorType=mvel
	 * B.datasource=test.rrd
	 * B.dsName=speed
	 * B.actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * B.spanLength=600
	 * B.class=cc.co.llabor.threshold.Log4JActionist
	 * B.action=log4j
	 * B.BaseLine=2.0
	 * B.monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * B.monitorType=mvel
	 */
	void lookInsideThold(Object tholdProps) throws TholdException {
		for(Object  key: ((Properties )tholdProps).keySet()){
			String keyTmp = ""+key;
			if(keyTmp.endsWith("."+Threshold.BASE_LINE)){ // one more "thold-def" inside
				String prefixTmp = (""+key).substring(0, keyTmp.length() - Threshold.BASE_LINE.length());
				System.out.println(prefixTmp);
				Properties pSubset = new Properties();
				for(Object  keyWithPrefix: ((Properties )tholdProps).keySet()){
					if ((""+keyWithPrefix).indexOf( prefixTmp) == 0){
						String keyToStore = (""+keyWithPrefix).substring(prefixTmp.length());
						String valToStore = ((Properties )tholdProps).getProperty(""+keyWithPrefix);
						pSubset.setProperty(keyToStore, valToStore);
						
					}
				}
				AlertCaptain acTmp = AlertCaptain.getInstance();
				Threshold watchDogTmp  = acTmp.toThreshold(pSubset );
				
				acTmp.register(  watchDogTmp );
				
			} 
		}
	}
	
	
	public void tick() {
		long timestamp = System.currentTimeMillis();
		this.tick(timestamp / 1000);
	}

	public void tick(long timestampInSeconds) {
		wakeUp(timestampInSeconds);
	}

	/**
	 * put the time-related-job into queue OR process it synchronously
	 * 
	 * @author vipup
	 * @param timestamp
	 */
	private void wakeUp(long timestampInSeconds) {
		wakeCounter++;
		String rrdDef = null;
		RrdDbPool instance = null;
		for (Threshold activist : unmodifiableCollection ) {
			try{
				
				if (this.isAsync) {
					CheckPoint e = new CheckPoint(timestampInSeconds, activist);
					queue.add(e);
				} else {
					//processData(e);
					rrdDef = activist.getDatasource();
					instance = RrdDbPool.getInstance();
					RrdDb rrd = instance.requestRrdDb(rrdDef);										
  					String dsName = activist.getDsName();//"speed";
 					Datasource dsTmp = rrd.getDatasource(dsName);
 					double val = dsTmp.getLastValue();
					processData(val, timestampInSeconds, activist);
				}
			}catch(Exception e){
				
				// TODO ???????
				if (1==2)e.printStackTrace();
				unregister(activist);
			}
		}
	}


	public void kill() {
		isAlive = false;
	}
	public void run() {
		log.info(Repo.getBanner("AlertCaptain"));
		while (isAlive) { // show must go on..
			try {
				if (queue.isEmpty()) {
					wait100();
				} else if (queue.size()>100000){
					log.error("AlertingQueue reached MAX_ALLOWED_CAPACITY["+100000+"]:"+queue.size()+"!!!");
					queue.clear();
					log.warn ("AlertingQueue resetet!");
				}else{
					CheckPoint charlieTmp = null;
					try {
						charlieTmp = queue.peek();
						if (charlieTmp == null) {
							continue;
						}
						processStart = charlieTmp.getTimestamp();
						processData(charlieTmp);
						processEnd = charlieTmp.getTimestamp();
						queue.remove(charlieTmp);
					} catch (java.util.NoSuchElementException e) {
						if (charlieTmp != null)
							charlieTmp.processError(e);
					} catch (Exception e) {
						lastExc = e;
					} catch (Throwable e) {
						lastExc = e;
					}
				}
			} catch (Throwable e) {
				lastExc = e;
			} finally {
				// isAlive = false;
			}
		}
		System.out
				.println("AlertCaptain is stopped |nasd;famsadflasmdfhq;wrxqweuiqwxrf;heqx,rqlwexr,qwexr,qeurx..  ");
		log.info(Repo.getBanner("+AlertCaptain"));
	}
	public void wait100() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	//Async
	private void processData(CheckPoint charlieTmp) {
		Threshold activist = charlieTmp.getToCheck();
		double val = charlieTmp.getValue();
		processData(val, charlieTmp.timestamp, activist);
	}
	
	//sync
	private void processData(double val, long timestampInSeconds, Threshold activist) {
 		
			AbstractAlerter abstractAlerter = (AbstractAlerter)activist;
			long start = System.currentTimeMillis();
			long startNano = System.nanoTime();
			long quote = abstractAlerter.getQuote();
			long price = abstractAlerter.getPrice();
			if (quote > price){
				abstractAlerter.performChunkAsync(timestampInSeconds, val, price);	
			} 
			long stop = System.currentTimeMillis();
			long stopNano = System.nanoTime();
			long executeTimeNano =stopNano-startNano;
			long executeTime =stop-start;
			abstractAlerter.billPrice( timestampInSeconds ,executeTime ,executeTimeNano);
 
	}

	static final AlertCaptain myself = new AlertCaptain();
	public final Threshold unregister(Threshold activist) {
		try {
			if (ToDo.indexOf(activist) > -1) {// ToDo.add(activist)
				boolean o = unpersist(activist);
				if (o){
					activist.stop();
					return activist;
				}
					
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	public final Threshold unregister(CompositeAlerter activist) {
		Threshold retval = null;
		for (Threshold actToDel :activist.chainOfAlerters){
			retval = unregister(actToDel);
		}
		return retval;
	}
	
	
	public final Collection<Threshold> list(){
		return unmodifiableCollection;
	}
	final synchronized void  syncUC() {
		unmodifiableCollection = Collections.unmodifiableCollection(  new HashSet<Threshold>( this.ToDo ));
		//System.out.println(unmodifiableCollection);
	}
	public static Properties toProperties(Threshold thTmp) throws TholdException {
		Properties retval = new Properties();
		retval.setProperty(Threshold.CLASS, thTmp.getClass().getName());
//		this.action =props.getProperty("action") ;
//		this.actionArgs =props.getProperty("actionArgs") ;
//		this.rrdName =props.getProperty("datasource") ;
//		this.dsName =props.getProperty("dsName") ;
//		this.type =props.getProperty("monitorType") ;
//		this.monitorArgs =props.getProperty("monitorArgs") ;
//		this.activationTimeoutInSeconds = Integer.parseInt( props.getProperty("spanLength" ));
//		this.baseLine = Double.parseDouble(  props.getProperty("BaseLine")); 		

		retval.setProperty(Threshold.ACTION,thTmp.getAction()) ;
		retval.setProperty(Threshold.ACTION_ARGS,thTmp.getActionArgs() ) ;
		retval.setProperty(Threshold.DATASOURCE,thTmp.getDatasource()  ) ;
		retval.setProperty(Threshold.DS_NAME,thTmp.getDsName() ) ;
		retval.setProperty(Threshold.MONITOR_TYPE,thTmp.getMonitorType() ) ;
		retval.setProperty(Threshold.MONITOR_ARGS,thTmp.getMonitorArgs() ) ;
		retval.setProperty(Threshold.SPAN_LENGTH ,""+thTmp.getSpanLength()  ) ;
		retval.setProperty(Threshold.BASE_LINE,""+thTmp.getBaseLine() ) ;	
		
		return retval;
	}
	
	public static final String cacheNS = AlertCaptain.class.getName();
	
	public final static Threshold restoreByName(String namePar) throws TholdException{
		Cache c = Manager.getCache(cacheNS);
 		Object storedProps = c.get(namePar+".properties");
		Threshold outTmp = toThreshold(storedProps) ;
		return outTmp;
	}
	
	public final static void storeToName(String namePar, Threshold toStorePar) throws TholdException{ 
		Properties properties = toStorePar.toProperties();
		String fnTmp = namePar+".properties";
		storeToName(fnTmp, properties); 		
	}
	public final static void storeToName(String namePar, Properties propsPar) throws TholdException{
		Cache c = Manager.getCache(cacheNS);
 		String fnTmp = namePar.endsWith(".properties")?namePar:namePar+".properties";
		c.put(fnTmp, propsPar); 		
	}	
	
	public final static Threshold toThreshold(Object thTmp) throws TholdException {
		try {
			if (thTmp instanceof Properties) {
				Properties p = (Properties) thTmp;
				String clazz = "" + p.getProperty(Threshold.CLASS);
				Class cl = Class.forName(clazz);

				Class[] parameterTypes = new Class[]{Properties.class};
				Constructor<Threshold>  constructor = cl.getConstructor( parameterTypes );//instance.register( theNext );
				
				Threshold retval = (Threshold) constructor.newInstance(p );
				return retval;
			} else {
				throw new TholdException("invalid Thold def:" + thTmp);
			}
		} catch (ClassNotFoundException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (InstantiationException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (IllegalAccessException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (SecurityException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (NoSuchMethodException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (IllegalArgumentException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		} catch (InvocationTargetException e) {
			throw new TholdException("invalid Thold def:" + thTmp, e);
		}
	}
	@Override
	public final void handleNotification(Notification notification, Object handback) {
		long ts = notification.getTimeStamp();
		//System.out.println(""+notification+handback);
		//log.error("#"+ts+"::"+notification+handback);
		this.tick(ts /1000);
	}
	public final static String toNICK(String namePar) { 
			String retval = "";
			String accu = "";
			String theLast = "LeaSt";
			for (String theC :namePar.replace(".", ",").split(",")){
				theLast = theC;
				accu+=theC.substring(0,1).toUpperCase();
			}
			retval = accu;
			accu = "";
//	 		Log4JActionist -> AJL4		
//			String abcTmp = "abcdefghijklmnoprstuvwxwz1234567890".toUpperCase();
//			for (int i=0;i<abcTmp .length();i++){
//				String theC = abcTmp .substring(i,i+1);
//				if(theLast.indexOf(theC)>=0){
//					accu+=theC;
//				}
//				
//			}
//			namePar+=accu;

			
	// Log4JActionist ->L4JA		
			accu = "";
			String abcTmp = "abcdefghijklmnoprstuvwxwxyz1234567890".toUpperCase();
			for (int i=0;i<theLast .length();i++){
				String theC = theLast.substring(i,i+1);
				if(abcTmp.indexOf(theC)>=0){
					accu+=theC;
				}
				
			}	
			retval +=accu;
			return retval;
		}	

}
