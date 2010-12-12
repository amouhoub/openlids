package org.openlids.registry;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class ListServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EntityManager em = EMF.get().createEntityManager();
		
		PrintWriter pw = resp.getWriter();

		ServletContext ctx = getServletContext();

		StringBuilder sb = new StringBuilder();
		
		sb.append("<list>\n");

		try {
			Query q = em.createQuery("select from " + LidsDescription.class.getName());

			List<LidsDescription> li = (List<LidsDescription>)q.getResultList();
			
			for (LidsDescription d : li) {
				sb.append("<lids>");
				sb.append(d.toXml());
				sb.append("</lids>");
			}
		} finally {
			em.close();
		}
		
		sb.append("</list>\n");
		
		//pw.println(sb);
		
		resp.setContentType("text/html");
		
		Transformer t = (Transformer)ctx.getAttribute(Listener.T);
	
		try {
			StreamSource ssource = new StreamSource(new StringReader(sb.toString()));
			StreamResult sresult = new StreamResult(pw);
			t.transform(ssource, sresult);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
}
