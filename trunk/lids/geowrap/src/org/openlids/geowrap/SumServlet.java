package org.openlids.geowrap;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SumServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		
		String val1 = req.getParameter("val1");
		String val2 = req.getParameter("val2");
		
		if (val1 == null || val2 == null) {
			resp.sendError(500, "requires parameter val1, val2");
		}
		
		int i1 = Integer.parseInt(val1);
		int i2 = Integer.parseInt(val2);
		
		String p = req.getPathInfo();
		p = p.substring(1);
		//out.println(req.getQueryString());
		out.println(":val <http://example.org/" + p + " " + (i1+i2) + " .");
	}
}
