<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<div class='corner' id='angle'><a href="/corner" target="_blank"><IMG
	SRC="img/btn_track.gif">rrd4u</a></div>
<div class='cornerA' id='cornerA'><a href="/corner_a"
	target="_blank"><IMG SRC="img/btn_prev.gif">A</a></div>
<div class='cornerB' id='cornerB'><a href="/corner_b"
	target="_blank"><IMG SRC="img/btn_sharethis.gif">b</a></div>
<div class='cornerC' id='cornerC'><a href="/corner_c"
	target="_blank"><IMG SRC="img/btn_memories.gif">.C</a></div>
<div class='cornerD' id='cornerD'><a href="/corner_d"
	target="_blank">d.<IMG SRC="img/button-flag.gif"></a></div>
<html>
<head>
<title>rrd SAAS</title>
<link rel="Stylesheet" href="css/corner.css" type="text/css">
</head>
<body onLoad="return 1;">
<%@page import="org.jrobin.cmd.RrdCommander"%>
<%@page import="org.jrobin.svg.RrdGraphInfo"%>
<table>
	<tr>
		<td width="64" height="64"><a
			href="https://rrdsaas.appspot.com/rrd.jsp" title=" RRD Java impl ">asdasdas</a>
		</td>
	</tr>
</table>
<table>
	<tr>
		<td width="64" height="64"><embed src="JavaLogo.svg"
			type="image/svg+xml" height="64" width="64" scale=true
			alt="javalogo as embed.svg" /></td>
		<td width="64" height="64"><embed src="JavaLogo.svg" height="64"
			width="64" type="image/svg+xml" scale=true
			alt="javalogo as embed+plug.svg"
			pluginspage="http://www.adobe.com/svg/viewer/install/" /></td>

		<td width="64" height="64"><object data="JavaLogo.svg"
			width="300" height="100" type="image/svg+xml"
			alt="javalogo as object+plug.svg"
			codebase="http://www.adobe.com/svg/viewer/install/" /></td>

		<td width="64" height="64"><IMG src="gif.jsp" height="64"
			width="64" /></td>
		<td width="64" height="64">TCJ:</td>
		<td width="64" height="64"><IMG src="speed.gif" height="64"
			width="164" alt="tomcat/jee rrdoutput" /></td>
		<td width="64" height="64">GAE: <embed src="svg.jsp"
			type="image/svg+xml" height="100%" width="100%" alt="gae rrdoutput" /></td>
		<td width="64" height="64"><img src="JavaLogo.svg" height="64"
			width="64" alt="javalogo as img.svg" /></td>
	<tr width="100%" height="64">
	</tr>

	<td width="200">
	<form method="post">
	<%
		String testCOLOR = "FFFFFF000000CCAA";
		testCOLOR = testCOLOR
				.substring((int) (System.currentTimeMillis() % 10));
		testCOLOR = testCOLOR.substring(0, 6);
		String testCMD = " rrdtool graph speed.gif  --start 920804400 --end 920808000  DEF:myspeed=test.rrd:speed:AVERAGE  LINE"
				+ (((int) (System.currentTimeMillis() % 3) + 1))
				+ ":myspeed#" + testCOLOR;
	%> RddCommand:: <textarea name="cmd" cols="160" rows="4"
		value="<%=testCMD%>"><%=testCMD%></textarea> <<<-- if u don't know
	what to do -- just try the default action - press the button ;) <input
		type="submit" /></form>
	</td>
	</tr>

	<tr>
		<td width="100%"><iframe src="man/man.html" width="93%"
			height="133"> a lot of thanks to Alex van den Bogaerdt for
		this very kindly checked documetation. Unfortunately this page is
		dropped from original https://rrd4j.dev.java.net/tutorial.html... ...
		but __fortunately___ I backuped it! :) </iframe></td>
	</tr>
	<tr width="100%" height="333">

		<td>
		<%
			System.out.println("== Rrd4j's RRDTool commander ==");
			String cmdTmp = request.getParameter("cmd");
			Object o = null;
			if (cmdTmp != null) {
				System.out.println(cmdTmp);
				cmdTmp = cmdTmp.replace("\\", "\n");
				//RrdCommander.setRrdDbPoolUsed(false);
				o = RrdCommander.execute(cmdTmp);
				if (o instanceof org.jrobin.svg.RrdGraphInfo) {
					RrdGraphInfo oInf = (RrdGraphInfo) o;
					session.setAttribute("svg", oInf.getBytes());
				}
			}
		%>

		<form method="post">Execution result::: <textarea
			readonly="readonly" name="result" cols="60" rows="4">
 <%=o%>
</textarea></form>


		</td>
	</tr>

	<tr>
		<td></embed> <!-- 
 <%=" "+System.getProperties()+" "%>
  --></td>
	</tr>
</table>
<table height="64" width="100%">
	<tr width="64" height="64">
		<td><img alt="timestamp" src="img/Task_Scheduler.jpg"> RRD
		SAAS timestamp: <%=System.currentTimeMillis()%></td>
		<td><a href="rrdcalc.html"> <img alt="time3stamp"
			src="img/sales.jpg" /> RRDCAlculator</a></td>
	</tr>
	<tr width="1" height="1">
		<td>.</td>
	</tr>
</table>