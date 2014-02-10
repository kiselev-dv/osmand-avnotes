package me.osm.avnotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class Shortlink {
	
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	public static void main(String[] args) {
		
		List<WPT> list = new ArrayList<>();
		
		File location = new File(args[0]);
		if(location.isDirectory()) {
			for(String f : location.list()) {
				File file = new File(f);
				if(f.endsWith(".wav") && !file.isDirectory()) {
					if(f.length() > 11) {
						double[] decode = decode (f.substring(0, 11));
						WPT wpt = new WPT();
						
						wpt.lon = decode[0];
						wpt.lat = decode[1];
						wpt.f = file;

						try {
							Path path = FileSystems.getDefault().getPath(location.getAbsolutePath(), file.getName());
							BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
							FileTime creationTime = attributes.creationTime();
							
							wpt.d = new Date(creationTime.toMillis());
							wpt.p = path.toString();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						list.add(wpt);
					}
				}
			}
		}
		
		Collections.sort(list, new Comparator<WPT>() {

			@Override
			public int compare(WPT o1, WPT o2) {
				return o1.d.compareTo(o2.d);
			}
			
		});
		
		PrintStream out = null;
		try {
			out = new PrintStream(FileSystems.getDefault().getPath(location.getAbsolutePath(), "avnotes.gpx").toFile());
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
			out.println("<gpx version=\"1.1\" creator=\"OsmAnd~\" " +
					"xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
					"xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
			
			for(WPT wpt : list){
				out.println("<wpt lat=\"" + wpt.lat + "\" lon=\"" + wpt.lon + "\">");
				
				out.println("<time>");
				out.println(simpleDateFormat.format(wpt.d));
				out.println("</time>");
				
				out.println("<link href=\"" + wpt.f.getName() + "\">");
				
				out.println("<type>");
				out.println("audio");
				out.println("</type>");
				
				out.println("</link>");
				
				out.println("</wpt>");
			}
			
			out.println("</gpx>");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if(out !=null)
				out.close();
		}
	}
	
	public static class WPT {
		public double lon;
		public double lat;
		
		public File f;
		public Date d;
		public String p; 
	}
	
	private static final String ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~";
	
	public static double[] decode(String str) {
	    long x = 0;
	    long y = 0;
	    int z = 0;
	    int z_offset = 0;
	
	    str = str.replace("@", "~");
	
	    for(char c : str.toCharArray())
	    {
	    	int t = ARRAY.indexOf(c);
	    	if(t < 0)
	    	{
	    		z_offset -= 1;
	    	}
	    	else
	    	{
	    		for(int i = 0; i < 3; i++)
	    		{
	    			x <<= 1; 
	    			if((t & 32) != 0)	
	    				x = x | 1; 
	    			t <<= 1;
	    			
	    			y <<= 1; 
	    			if((t & 32) != 0)	
	    				y = y | 1; 
	    			t <<= 1;
	    		}
	    		
				z += 3;
	    	}
	    }

	    //pack the coordinates out to their original 32 bits.
	    x <<= (32 - z);
	    y <<= (32 - z);

	    //project the parameters back to their coordinate ranges.
	    return new double[]{ (x * 360.0 / Math.pow(2, 32)) - 180.0, 
	            (y * 180.0 / Math.pow(2, 32) ) - 90.0, 
	            z - 8 - (z_offset % 3)
	    };
	}
}
