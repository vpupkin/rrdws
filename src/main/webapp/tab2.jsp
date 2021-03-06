<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<link rel="shortcut icon" type="image/ico" href="favicon.ico" />
		
		<title>DataTables example</title>
		<style type="text/css" title="currentStyle">
			@import "table/demo_page.css";
			@import "table/demo_table.css";
		</style>
		<script type="text/javascript" language="javascript" src="table/jquery.js"></script>
		<script type="text/javascript" language="javascript" src="table/jquery.dataTables.js"></script>  
		<script type="text/javascript" charset="utf-8">
			var oTable;
			var selectCounter = 0;
			var giRedraw = false;
			$(document).ready(function() {
				/* Add a click handler to the rows - this could be used as a callback */
				$("#example tbody").click(function(event) {
					var oTmp = $(event.target.parentNode);
					if (oTmp.hasClass('row_selected') ){ 
						delRrd(oTmp); 
					} else	{ 
						addRrd(oTmp);
					}
					updateRdd( );
				}); 
				/* Add a click handler for the delete row */
				$('#delete').click( function() {
					var anSelected = fnGetSelected( oTable );
					// remove backwards!!
					for ( var i=anSelected.length ; i>0 ; i-- ){
						oTable.fnDeleteRow( anSelected[i-1] );
					}
				} ); 
				/* Init the table */			
				oTable = $('#example').dataTable( {
					"oLanguage": {
						"sUrl": "table/de_DE.txt"
					}				, 
					"bProcessing": true,
					"bServerSide": true,
					"sPaginationType": "full_numbers",
					"sAjaxSource": 'listPP.jsp' 				
				} );
 
				
			} );
			function toColor(counter){
				var retval = "" ;
				var hexStr =new Array( 'F','7', '0' , '4', '9', 'A', '1' , 'F');
				retval += hexStr[counter  %4];
				retval += hexStr[counter  %6];
				retval += hexStr[(counter+2)  %3];
				retval += hexStr[counter  %5];
				retval += hexStr[(counter+1)  %5];
				retval += hexStr[counter  %7];
				return retval;			
			}
			
			function addRrd(rowObj){
				rowObj.addClass('row_selected');												
				selectCounter=selectCounter+1;
				var counter = selectCounter;
				// DB-name
				var dbTmp = rowObj[0].children[2].valueOf().textContent;
				//alert(dbTmp);
				var newTextBoxDiv = $(document.createElement('div')).attr("id", 'TextBoxDiv' + counter);
				var htmlTmp = 'Textbox #'+ counter + ' : </label>' +
					  '<input type="text" name="textbox' + counter + 
					  '" id="textbox' + counter + '" value="'+dbTmp+'" >';
				htmlTmp += 	'<input type="text" name="dbField' + counter + 
					  '" id="dbField' + counter + '" value="data:AVERAGE" enable="false" size="3">';
				htmlTmp += 	'<input type="text" name="dbAlias' + counter + 
					  '" id="dbAlias' + counter + '" value="my'+counter+'" enable="false" size="3">';
				htmlTmp += 	'<input type="text" name="dbColor' + counter + 
					  '" id="dbColor' + counter + '" value="#'+toColor(counter)+'" enable="false">';
				htmlTmp += 	':<input type="text" name="dbNote' + counter + 
					  '" id="dbNote' + counter + '" value=".'+counter+'.n" enable="false">';
  
				newTextBoxDiv.html(htmlTmp);
				newTextBoxDiv.appendTo("#TextBoxesGroup");			
			}
			function delRrd(rowObj){
				rowObj.removeClass('row_selected');		
				var counter = selectCounter;
				var bNname = "#TextBoxDiv" + counter;
				var oDiv = $(bNname);
				oDiv.remove();			
				selectCounter=selectCounter-1;
			}
			
			function updateRdd( ){
				 
				var txtTmp = "list["+selectCounter+"]==";				
				var prefix = "";
				var dbTmp = "";
				var dbFieldTmp = "";
				var dbAliasTmp = "";
				var dbColorTmp = "";
				var dbColorTmp = "";
				var dbNoteTmp = "";
				var defTmp = "";
				var visTmp = "";
				
				var defAccu = "";
				var visAccu = "";
				
				for ( var i=selectCounter ; i>0 ; i-- ){
						txtTmp =txtTmp+ prefix + i  ;
						prefix = ",";
						//dbTmp = oTable._fnGetCellData(anSelected[i-1],2); 
						//document.getElementById("textbox"+i).setAttribute("value", dbTmp);
						dbTmp = document.getElementById("textbox"+i).getAttribute("value");
						var ind = i ;
						//dbFieldTmp = document.getElementById("dbField"+ind).getAttribute("value");
						dbFieldTmp = "data:AVERAGE"; 
						//document.getElementById("dbAlias"+ind).getValue();
						dbAliasTmp = "my"+ind;
						dbColorTmp = "#"+toColor(ind);
						dbNoteTmp = "not"+ind;
						//DEF:myspeed2=X840983877.rrd:data:AVERAGE 
						defTmp = ' DEF:' + dbAliasTmp + '=' + dbTmp +':'+dbFieldTmp;
						defAccu += defTmp;
						// LINE2:myspeed3#00FF88:asdfasdf 
						visTmp = ' LINE2:' +dbAliasTmp +''+ dbColorTmp +':' + dbNoteTmp;
						visAccu += visTmp;
						
				} 
				// jquery
				//$('#textbox1').val ( txtTmp +"," + counter );
				// js
				document.getElementById("rrdlist").setAttribute("value", txtTmp);
				
				// rrdIMG
				var cmdPrefix = "gifgen.jsp?cmd=rrdtool+graph+speed.gif+";
				var cmd  = cmdPrefix ;
				// -v 'vip'  -t 'XXXX!'
				cmd = cmd + "-v '" +document.getElementById("_vtitle").value +"' ";
				cmd = cmd + "-t '" +document.getElementById("_htitle").value+ "' ";
				cmd = cmd +document.getElementById("textareaRrd").value;
				cmd +=defAccu;
				cmd +=visAccu;
				
				document.getElementById("textDebug").value =  addslashes( cmd );
				document.getElementById("rrdimg").setAttribute("src",  addslashes( cmd ));
				
			}	
			//http://stackoverflow.com/questions/770523/escaping-strings-in-javascript
			// http://www.w3schools.com/jsref/jsref_replace.asp
 			function addslashes( str ) {
				return (str+'').replace(/#/g,'%23').replace(/([\\"'])/g, "\\$1");
			}

			/*
			 * I don't actually use this here, but it is provided as it might be useful and demonstrates
			 * getting the TR nodes from DataTables
			 */
			function fnGetSelected( oTableLocal )
			{
				var aReturn = new Array();
				var aTrs = oTableLocal.fnGetNodes();
				
				for ( var i=0 ; i<aTrs.length ; i++ )
				{
					if ( $(aTrs[i]).hasClass('row_selected') )
					{
						aReturn.push( i );
					}
				}
				return aReturn;
			}
			
		</script>
	</head>
	<body id="dt_example">
		<div id="container"> 
	 
			<div id="dynamic">
<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
	<thead>
		<tr>
			<th width="1%">C</th>
			<th width="1%">#</th>
			<th width="1%">DB-name</th>

			<th width="95%">xpath</th>
			<th width="1%">rrd</th>
		</tr>
	</thead>
	<tbody>
		
	</tbody>
	<tfoot>
		<tr>
			<th>Rendering engine</th>
			<th>Browser</th>
			<th>Platform(s)</th>
			<th>Engine version</th>
			<th>CSS grade</th>
		</tr>

	</tfoot>
</table>
			</div>
			
			<div id="selecterRRDs">
			<p><a href="javascript:void(0)" id="delete">Delete selected row</a></p>
			</div>
			 
<table> <tbody><tr><td>
<div id='TextBoxesGroup'>
	<label>RRDs to visualize : </label><input type='text' id='rrdlist' value='-' size='77'>
</div>		
<div id='RRDGroup'>
	<label>h-title: </label><input type='text' id='_htitle' value='h-Title' />
	<label>v-title: </label><input type='text' id='_vtitle' value='v-Title' />

	<div id="rrdDiv1">
		<label>cmd : </label>
		<textarea type='textarea' name="textareaRrd"  cols="60" rows="4" id='textareaRrd' >     --start now-1week
		</textarea>
	</div>
</div>			
</td><td>
	<div id="rrdIMAGE">
		<img  id="rrdimg" src="speed.gif"/>
	</div>

</td></tr><tbody></table> 
<input type='button' value='Add Button' id='addButton'>
<input type='button' value='Remove Button' id='removeButton'>
<input type='button' value='Get TextBox Value' id='getButtonValue'>
 <textarea type='textarea' name="textDebug"  cols="160" rows="4" id='textDebug' >  ddddddddd </textarea>
	
	</body>
</html>