package org.openlids.registry;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EntityManager em = EMF.get().createEntityManager();
		
		PrintWriter pw = resp.getWriter();
		
		LidsDescription ld = new LidsDescription();
		
		try {
			ld.parse(req.getParameterMap());
			em.persist(ld);
		} catch (LidsParseException e) {
			throw new IOException(e.getMessage());
		} finally {
			em.close();
		}
		
		pw.println("successfully stored\n" + ld);
	}
}
