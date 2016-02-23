package cc.co.llabor.system;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  04.04.2011::10:40:50<br> 
 */
public class ServerLauncher implements Runnable {
 
		private String[] arg0;
		
		ServerLauncher(String[] arg0){
			this.arg0 = arg0;
		}

		public void run(){
			
			try {
				// Listen on the default IPv4 multicast group and allow remote JMX connections to RMI/TCP port 25826:
				// java -Dcom.sun.management.jmxremote.port=25826 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar collectd.jar

				org.collectd.mx.MBeanReceiver.main(arg0);
			} catch (Exception e1) {
				//e1. printStackTrace();
			} catch (Throwable e1) {
				// TODO Auto-generated catch block
				//e1. printStackTrace();
			}
		}

		public void destroyServer() {
			//org.collectd.mx.MBeanReceiver.getInstance().kill();
			org.collectd.mx.MBeanReceiver.getInstance().shutdown();
		}
	 

}


 