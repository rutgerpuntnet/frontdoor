package net.rutger.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import net.rutger.repository.InMemoryImageRepo;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Scheduled service that retrieves and stores a frontdoor image
 *
 */
@Service
public class ScheduledImageService {

    private static final Logger LOG = Logger.getLogger(ScheduledImageService.class);
    // private static final String url =
    // "http://192.168.1.168/snapshot.jpg?size=-1x-1";
    private static final int HTTP_TIMEOUT = 30000;
    private static String sid = null; // WYlH4D3T0qT02BCK8N0107
    private static final String BASE_URL = "http://192.168.1.3:5000/webapi/entry.cgi?api=SYNO.SurveillanceStation.Camera&method=GetSnapshot&version=1&cameraId=2&_sid=";
    private static final String LOGIN_URL = "http://192.168.1.3:5000/webapi/auth.cgi?api=SYNO.API.Auth&method=Login&version=2&account=snapshot&passwd=snapshot&session=SurveillanceStation";

    @PostConstruct
    public void postConstruct() {
        LOG.debug("ScheduledImageService constructed");
    }
    
    @Autowired
    InMemoryImageRepo repo;

    public byte[] getImage() {
        byte[] content = new byte[0];
        try {
            URL imageURL = new URL(getUrl());
            URLConnection con = imageURL.openConnection();
            con.setConnectTimeout(HTTP_TIMEOUT);
            con.setReadTimeout(HTTP_TIMEOUT);
            InputStream in = con.getInputStream();
            BufferedImage originalImage = ImageIO.read(in);
            if (originalImage != null) {
                LOG.debug("Image loaded");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "jpg", baos);
                content = baos.toByteArray();
            } else {
                // An empty image can mean that we were not successfully logged
                // in.
                // get a new session id and try again
                updateSid();
                con = imageURL.openConnection();
                con.setConnectTimeout(HTTP_TIMEOUT);
                con.setReadTimeout(HTTP_TIMEOUT);
                in = con.getInputStream();
                originalImage = ImageIO.read(in);
                if (originalImage != null) {
                    LOG.debug("Image loaded");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(originalImage, "jpg", baos);
                    content = baos.toByteArray();
                }
                LOG.debug("Image empty");
            }
        } catch (SocketTimeoutException e) {
            LOG.warn("SocketTimeoutException while reading image: " + e.getMessage());
        } catch (ConnectException e) {
            LOG.warn("ConnectException while reading image: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Exception while reading image", e);
        }
        return content;
    }

    // temporary turned off
    //@Scheduled(fixedDelay = 1000)
    public void getScheduledImage() {
        byte[] content = getImage();
        if(content.length>0) {
            repo.addImage(content);
        }
    }

    private String getUrl() {
        if (sid == null) {
            updateSid();
        }
        return BASE_URL + sid;
    }

    private void updateSid() {
        // read json response with
        try {
            JSONObject json = readJsonFromUrl(LOGIN_URL);
            this.sid = json.getJSONObject("data").getString("sid");
        } catch (Exception e) {
            LOG.warn("Cannot get a new SID.", e);
            this.sid = null;        
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        URLConnection con = new URL(url).openConnection();
        con.setConnectTimeout(HTTP_TIMEOUT);
        con.setReadTimeout(HTTP_TIMEOUT);
        InputStream is = con.getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
