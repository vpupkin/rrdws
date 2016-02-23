package cc.co.llabor.cache.css;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL; 
import java.util.HashSet;
import java.util.Set;

import org.vietspider.html.util.HyperLinkUtil; 
import ws.rrd.server.LServlet; 
/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  26.07.2010::11:45:51<br> 
 */
public class Item implements Serializable{ 
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -7937242057537348262L;
	private String value;

	public Item(String value) {
		this.value = value;
	}

	public String getValue() { 
			return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	Set<String> refs = new HashSet<String>();

	public void addReffer(String refPar) {
		refs.add(refPar);
		String homeLinkPar = refPar;
		URL linkPar = null;
		try {
			linkPar = new URL(refPar);
			linkPar = new URL(linkPar.getProtocol() ,linkPar.getHost(), linkPar.getPort(),  linkPar.getPath() );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e. printStackTrace();
		}
		// TODO - parce all possible DOC-location-modifications 
		String encodeLink = HyperLinkUtil.encodeLink(linkPar, homeLinkPar   );
		encodeLink = encodeLink.replace("/l/", "/F/");
		if (value.toLowerCase().indexOf(LServlet.SwapServletUrl.replace("/l/", "/F/").toLowerCase())>0){
			return;
		}
		if (value.toLowerCase().indexOf(LServlet.SwapServletUrl .toLowerCase())>0){
			return;
		}
		this.value = this.value.replace(".src=\"", "._s_R_c=\""+encodeLink );
		this.value = this.value.replace("._s_R_c=\"" , ".src=\"" );
		this.value = this.value.replace("https://", ""+LServlet.SwapServletUrl.replace("/l/", "/F/").replace("https", "hTTpS")+"h_t_t_p_s_://");
		this.value = this.value.replace("http://", ""+LServlet.SwapServletUrl.replace("/l/", "/F/").replace("http", "hTTp")+"h_t_t_p_://");
		
		
		System.out.println(refPar);
	}

	public Set<String> getRefs() {
 
			return refs;
	}

	boolean readOnly = false;
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if (!this.readOnly)
			this.readOnly = readOnly;
	}

}


 