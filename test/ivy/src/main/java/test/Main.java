package test;

import org.apache.commons.codec.binary.Base64;

public class Main
	{
	public static void main(String[] args)
		{
		System.out.println("Hello encoded: "+Base64.encodeBase64String("Hello".getBytes()));
		}
	}
