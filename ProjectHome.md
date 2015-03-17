# rrd ws #
[RRD](http://www.mrtg.org/rrdtool/) is well-known soft/hardware monitoring-tool software...

...and now, thanks to [JRobin](http://www.jrobin.org/index.php/Main_Page), it is available for java/ j2ee , web-services, clustering, SoftwareAsAService (SaaS)

## supported protocols for Data-collecting ##

  * log-file
  * xml (DUMP, xpath)
  * Java-JMX
  * jcollectd TCP/UDP
  * SNMP (v 1., 2., 3.  )
  * typeperf
  * log4j, JUL, slf4j


## supported runtimes ##

  * [JAVA](http://java.net) - standalone application
  * [J2EE](http://download.oracle.com/javaee/) - java appserver ( tomcat, jboss, e.t.c)
  * [GAE](http://code.google.com/appengine/) - Google Application Engine

## supported output formats ##

### raster ###

  * gif
  * png
  * jpg
  * tif

### vector ###

  * svg

### composit ###

  * pdf

## reproduce original rrdtools commans ##

just follow https://code.google.com/p/rrdws/wiki/TUTORIAL_by_Alex_van_den_Bogaerdt

or simle try https://rrdsaas.appspot.com/!

## here some input/output ##
### RRD-tool commands ###
```

rrdtool create test.rrd  --start 920804400  DS:speed:COUNTER:600:U:U  RRA:AVERAGE:0.5:1:24  RRA:AVERAGE:0.5:6:10
rrdtool update test.rrd 920804700:12345 920805000:12357 920805300:12363
rrdtool update test.rrd 920805600:12363 920805900:12363 920806200:12373
rrdtool update test.rrd 920806500:12383 920806800:12393 920807100:12399
rrdtool update test.rrd 920807400:12405 920807700:12411 920808000:12415
rrdtool update test.rrd 920808300:12420 920808600:12422 920808900:12423
rrdtool fetch test.rrd AVERAGE --start 920804400 --end 920809200 >test.txt
rrdtool graph speed.gif  --start 920804400 --end 920808000  DEF:myspeed=test.rrd:speed:AVERAGE  LINE2:myspeed#FF0000
rrdtool graph speed2.gif  --start 920804400 --end 920808000  --vertical-label m/s  DEF:myspeed=test.rrd:speed:AVERAGE  CDEF:realspeed=myspeed,1000,*  LINE2:realspeed#FF0000
rrdtool graph speed3.gif  --start 920804400 --end 920808000  --vertical-label km/h  DEF:myspeed=test.rrd:speed:AVERAGE  CDEF:kmh=myspeed,3600,*  CDEF:fast=kmh,100,GT,kmh,0,IF  CDEF:good=kmh,100,GT,0,kmh,IF  HRULE:100#0000FF:"Maximum allowed"  AREA:good#00FF00:"Good speed"  AREA:fast#FF0000:"Too fast"
rrdtool graph speed4.gif  --start 920804400 --end 920808000  --vertical-label km/h  DEF:myspeed=test.rrd:speed:AVERAGE  CDEF:kmh=myspeed,3600,*  CDEF:fast=kmh,100,GT,100,0,IF  CDEF:over=kmh,100,GT,kmh,100,-,0,IF  CDEF:good=kmh,100,GT,0,kmh,IF  HRULE:100#0000FF:"Maximum allowed"  AREA:good#00FF00:"Good speed"  AREA:fast#550000:"Too fast"  STACK:over#FF0000:"Over speed"
rrdtool create all.rrd --start 978300900  DS:a:COUNTER:600:U:U  DS:b:GAUGE:600:U:U  DS:c:DERIVE:600:U:U  DS:d:ABSOLUTE:600:U:U  RRA:AVERAGE:0.5:1:10
rrdtool update all.rrd  978301200:300:1:600:300  978301500:600:3:1200:600  978301800:900:5:1800:900  978302100:1200:3:2400:1200  978302400:1500:1:2400:1500  978302700:1800:2:1800:1800  978303000:2100:4:0:2100  978303300:2400:6:600:2400  978303600:2700:4:600:2700  978303900:3000:2:1200:3000
rrdtool graph all1.gif -s 978300600 -e 978304200 -h 400  DEF:linea=all.rrd:a:AVERAGE LINE3:linea#FF0000:"Line A"  DEF:lineb=all.rrd:b:AVERAGE LINE3:lineb#00FF00:"Line B"  DEF:linec=all.rrd:c:AVERAGE LINE3:linec#0000FF:"Line C"  DEF:lined=all.rrd:d:AVERAGE LINE3:lined#000000:"Line D"
rrdtool fetch all.rrd  >all.txt
rrdtool dump all.rrd  >all.dump
rrdtool dump test.rrd  >test.dump 

```

### generated Raster-Diagramm ###
![http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-tomcat/all1.gif](http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-tomcat/all1.gif)


### Vector-Diagramm AS SVG ###
![http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-gae/all1.svg](http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-gae/all1.svg) - SVG is still not supported by Google-WIKI ?

![http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-gae/all1.svg](http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-gae/all1.svg)  - SVG is still not supported by Google-WIKI ?

just click here:::
[as SVG](http://rrdws.googlecode.com/hg/src/site/resources/!webapp/jrobin-gae/all1.svg)

### . ###

### ohloh ###

&lt;wiki:gadget url="http://www.ohloh.net/p/488601/widgets/project\_users.xml?style=rainbow" height="100" border="0"/&gt;

&lt;wiki:gadget url="http://www.ohloh.net/p/488601/widgets/project\_partner\_badge.xml" height="53" border="0"/&gt;




&lt;wiki:gadget url="http://www.google.com/ig/directory?synd=open&hl=de&url=http://www.infosniper.net/plugin/gadget-worldmap.xml" height="200" border="0" /&gt;