package net.rutger.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Servlet that will show the frontdoor image
 *
 */
public class FrontDoorServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(FrontDoorServlet.class);
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
    	try {
			byte[] content;
			content = new byte[0];
			URL imageURL = new URL(
					"http://192.168.1.168/snapshot.jpg?size=-1x-1");
			URLConnection con = imageURL.openConnection();
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			InputStream in = con.getInputStream();
        	BufferedImage originalImage = ImageIO.read(in);
        	if (originalImage != null) {
        		LOG.debug("Image loaded");
        		ByteArrayOutputStream baos=new ByteArrayOutputStream();
        		ImageIO.write(originalImage, "jpg", baos );
        		content = baos.toByteArray();
        	} else {
        		LOG.info("Couldn't load image");
        	}
              
        response.setContentType("image/jpeg");
        OutputStream out = response.getOutputStream();
        out.write(content);
        out.close();        	
        } catch (Exception e) {
        	LOG.error("Exception while reading image", e);
        	createErrorResponse(response, e.getMessage());
        }
    }

    private void createErrorResponse(HttpServletResponse response, String errorMessage) throws IOException{
        // Set response content type
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<body><html><h1>Probleem</h1>Er is iets misgegaan bij het ophalen van de afbeelding. "
        		+ "Mogelijk is de camera (tijdelijk) niet bereikbaar<br/>"
        		+ "Klik <a onClick=\"window.location.reload()\" href=\"#\">hier</a> om het nogmaals te proberen."
        		+ "<!-- " + StringEscapeUtils.escapeHtml3(errorMessage) + "--></body></html>");
    }
}
