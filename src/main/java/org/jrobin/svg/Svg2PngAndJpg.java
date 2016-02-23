package org.jrobin.svg;
import java.io.*;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;
import org.xml.sax.XMLReader;
/**
 * http://www.torsten-horn.de/techdocs/java-img.htm
 * @author vpupkin
 *
 */
public class Svg2PngAndJpg
{
  public static void main( String[] args )
  {
    if( 1 > args.length || 5 > args[0].length() || 0 > args[0].indexOf( '.' ) ) {
      System.out.println( "Error: Parameter for .svg input file missing.");
      System.exit( 1 );
    }
    try {
      //Svg2Png( args[0] );
      //Svg2Jpg( args[0] );
      System.exit( 0 );
    } catch( Exception ex ) {
      // ex. printStackTrace();
      System.exit( 2 );
    }
  }

  public static void Svg2Png(InputStream svgIn,   OutputStream ostream )
  throws TranscoderException, IOException
  {
    PNGTranscoder t = new PNGTranscoder();
    
    TranscoderInput input = new TranscoderInput( svgIn );
    
    
    TranscoderOutput output = new TranscoderOutput( ostream );
    t.transcode( input, output );
    ostream.flush();
    ostream.close();
  }

  public static void Svg2Jpg( InputStream svgIn, OutputStream ostream)
  throws TranscoderException, IOException
  {
    JPEGTranscoder t = new JPEGTranscoder();
    t.addTranscodingHint( JPEGTranscoder.KEY_QUALITY, new Float( .8 ) );
     
    TranscoderInput input = new TranscoderInput( svgIn );
     
     
    TranscoderOutput output = new TranscoderOutput( ostream );
    t.transcode( input, output );
    ostream.flush();
    ostream.close();
  }
}


 