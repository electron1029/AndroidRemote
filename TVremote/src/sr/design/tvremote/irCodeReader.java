package sr.design.tvremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class irCodeReader
{
	/** Reads a pre-defined file. This is for reading IR values "by-hand." 
	 * @param in_s **/
	public static String[] readFile(InputStreamReader inputStream)
	{
		String [] encodings = new String[5];
		BufferedReader reader = null;
		
		for (int j=0; j < encodings.length; j++)
		{
			encodings[j] = "";
		}
		
		reader = new BufferedReader(inputStream);
		
		int input = 0;
		int i = 0;
		while(input != -1)
		{
			//Read the next char:
			try
			{
				input = reader.read();
			} catch (IOException e)
			{
				System.out.println("Error occured while reading file. Exiting");
				return null;
			}
			Character c = (char) input;
			encodings[i] += c;
			if(c == '.') //last pair
			{
				i++;
				if(i >= encodings.length)
					return encodings;
			}
				
		}
		
		//System.out.println(currString);
		return encodings; 
	}
}
