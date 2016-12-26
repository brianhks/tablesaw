/*
 * Copyright (c) 2004, Brian Hawkins
 * Permission is granted to use this code without restriction as long
 * as this copyright notice appears in all source files.
 */
 
package tablesaw;
 
import java.util.*;


class Printf
	{
	private static String s_knownFlags = "dfxXs"; //Flags are in no particular order
	
	public static void main(String[] commandline)
		{
		Object[] args = new Object[1];
		
		args[0] = new Double(1234.1234);
		System.out.println(Printf.print("double = %2.2f", args));
		}
		
	public static String print(String pattern, Object[] args)
		{
		StringBuffer result = new StringBuffer();
		String[] parts = split(pattern);
		int paramCount = 0;
		
		for (int I = 0; I < parts.length; I++)
			{
			if (parts[I].startsWith("%"))
				{
				result.append(processArg(parts[I], args[paramCount]));
				paramCount++;
				}
			else
				result.append(parts[I]);
			}
		
		return (result.toString());
		}

//-------------------------------------------------------------------		
	private static String processArg(String tag, Object arg)
		{
		char[] ctag = tag.toCharArray();
		String ret = null;
		
		switch (ctag[ctag.length-1])
			{
			case 'd':
			case 'f':
			case 'l':
				ret = arg.toString();
				if (ctag.length > 2)
					ret = formatNumber(ret, new String(ctag, 1, ctag.length-2));
				break;
			case 'x':
				ret = Long.toHexString(((Number)arg).longValue()).toLowerCase();
				if (ctag.length > 2)
					ret = formatNumber(ret, new String(ctag, 1, ctag.length-2));
				break;
			case 'X':
				ret = Long.toHexString(((Number)arg).longValue()).toUpperCase();
				if (ctag.length > 2)
					ret = formatNumber(ret, new String(ctag, 1, ctag.length-2));
				break;
			case 's':
				ret = arg.toString();
				break;
			default:
				ret = arg.toString();
			}
			
		return (ret);
		}
		
//-------------------------------------------------------------------
	private static String formatNumber(String number, String format)
		{
		boolean leftJustify = false;
		boolean zeroPad = false;
		int startPos = 0;
		int width = 0;
		int numberWidth = 0;
		int precision = 0;
		boolean setPrecision = false;
		int precisionIndex = 0;
		
		
		if (format.startsWith("-"))
			{
			leftJustify = true;
			startPos = 1;
			}
		else if (format.startsWith("0"))
			{
			zeroPad = true;
			startPos = 1;
			}
			
		if ((precisionIndex = format.indexOf('.')) != -1)
			{
			setPrecision = true;
			precision = Integer.parseInt(format.substring(precisionIndex+1, format.length()));
			}
		else
			precisionIndex = format.length();
			
		if (precisionIndex != startPos)
			width = Integer.parseInt(format.substring(startPos, precisionIndex));
		
		if (setPrecision)
			{
			int pind;
			
			if ((pind = number.indexOf('.')) != -1)
				{
				number = String.valueOf(round(Double.parseDouble(number), precision));
				pind = number.indexOf('.');
				}
			else
				pind = number.length();
			
			char[] newnum;
			if ((pind + precision + 1) > number.length())
				newnum = new char[pind + precision + 1];
			else
				newnum = new char[number.length()];
			Arrays.fill(newnum, '0');
			newnum[pind] = '.';
			number = new String(copyArray(newnum, number.toCharArray()));
			}
		
		if (number.length() < width)
			{
			char[] newnum = new char[width];
			
			if (zeroPad)
				Arrays.fill(newnum, '0');
			else
				Arrays.fill(newnum, ' ');
				
			if (leftJustify)
				number = new String(copyArray(newnum, number.toCharArray()));
			else
				number = new String(copyArray(newnum, number.toCharArray(), width - number.length()));
			}
		
		return (number);
		}
		
//-------------------------------------------------------------------
	private static char[] copyArray(char[] dest, char[] src)
		{
		return (copyArray(dest, src, 0));
		}

//-------------------------------------------------------------------
	private static char[] copyArray(char[] dest, char[] src, int startOffset)
		{
		if (dest.length < src.length)
			{
			System.out.println("Source size = " + src.length);
			System.out.println("Dest size = " + dest.length);
			System.exit(1);
			}
			
		for (int I = 0; I < src.length; I++)
			dest[I+startOffset] = src[I];
		
		return (dest);
		}
		
//-------------------------------------------------------------------
	private static double round(double num, int precision)
		{
		long round;
		
		round = Math.round(num * Math.pow(10, precision));
		return (round / Math.pow(10, precision));
		}
		
//-------------------------------------------------------------------
	private static String[] split(String pattern)
		{
		char[] pat = pattern.toCharArray();
		ArrayList ret = new ArrayList();
		int lastIndex = 0;
		
		//There will never be a single '%' at the end so -1
		for (int I = 0; I < (pat.length-1); I++)
			{
			if (pat[I] == '%')
				{
				if (pat[I+1] != '%')
					{
					ret.add(new String(pat, lastIndex, (I - lastIndex)));
					lastIndex = I;
					for (int J = I; J < pat.length; J++)
						{
						if (s_knownFlags.indexOf(pat[J]) != -1)
							{
							ret.add(new String(pat, lastIndex, (J - lastIndex)+1));
							lastIndex = J+1;
							I = J;
							break;
							}
						}
					}
				else
					{
					I++;
					ret.add(new String(pat, lastIndex, (I - lastIndex)));
					lastIndex = I+1;
					}
				}
			}
		
		if (lastIndex < pat.length)
			ret.add(new String(pat, lastIndex, (pat.length - lastIndex)));
			
		return ((String[])ret.toArray(new String[1]));
		}
		
	}
	
