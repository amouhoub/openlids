package edu.kit.aifb.lids;

import junit.framework.TestCase;

public class RegexTest extends TestCase {
	public void testRegex() {
		String s = "type\":\"city\",\"attributes\":{},\"name\":\"Karlsruhe\",\"attributes\":{\"162834:id\":\"89366\"},";
		
		s = s.replaceAll("\"attributes\":\\{.*?\\},", "");
		
		System.out.println(s);
	}
}
