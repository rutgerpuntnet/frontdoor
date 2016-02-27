package net.rutger.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.soap.SOAPException;

import net.rutger.repository.InMemoryImageRepo;
import net.rutger.repository.InMemoryImageRepo.FrontdoorImage;
import net.rutger.service.ScheduledImageService;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.onvif.ver10.schema.Profile;
import org.onvif.ver10.schema.VideoSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.MediaDevices;

@Controller
public class FrontdoorController {
    private static final Logger LOG = Logger.getLogger(FrontdoorController.class);
    private static final String ONVIF_CAMERA_IP = "192.168.1.10:8899";
    
    @Autowired
    private ScheduledImageService imageService;
    
    @Autowired
    private InMemoryImageRepo imageRepo;
    
    @Autowired 
    private ServletContext servletContext;

    private FrontdoorImage latestImage;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss");

    @RequestMapping({"/index", "/"})
    public String frontdoor(ModelMap model) {
//        latestImage = imageRepo.getLatestImage();
//        model.addAttribute("numberOfImages", imageRepo.getAllImages().size());
//        model.addAttribute("imageDate", sdf.format(latestImage.getDate()));
//        model.addAttribute("oldImage", latestImage.getDate().before(new DateTime().minusSeconds(30).toDate()));
//        
//        List<Long> imageDates = new ArrayList<Long>(imageRepo.getAllImages().keySet());
//        Collections.reverse(imageDates);
//        // skip the last X number of the list (because of timeout)
//        model.addAttribute("imageKeys", imageDates.size() > 90 ? imageDates.subList(0, 90) : imageDates);

        return "frontdoor";
    }

    @RequestMapping(value = "history")
    public String history(ModelMap model) {
        model.addAttribute("numberOfImages", imageRepo.getAllImages().size());
        
        List<Long> imageDates = new ArrayList<Long>(imageRepo.getAllImages().keySet());
        Collections.reverse(imageDates);
        // skip the last X number of the list (because of timeout)
        model.addAttribute("imageKeys", imageDates.size() > 99 ? imageDates.subList(0, 98) : imageDates);
        
        model.addAttribute("memoryInfo", "Memory total/free/max <br/>\n" +
        Runtime.getRuntime().totalMemory() + "<br/>\n"+
        Runtime.getRuntime().freeMemory()+ "<br/>\n" +
        Runtime.getRuntime().maxMemory());
        return "history";
    }

    @RequestMapping(value = "cachedImage", params = { "index" }, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> cachedImage(@RequestParam(value = "index") long index) {
        byte[] content;
        try{
            FrontdoorImage image = imageRepo.getAllImages().get(index);
            
            content = image.getImageBytes();
            
        } catch (Exception e) {
            content = getImageDummy();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg");
        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "latestImage", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> latestImage() {

        byte[] content = imageService.getImage();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "latestCachedImage", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> latestCachedImage() {
        if (latestImage == null) {
            latestImage = imageRepo.getLatestImage();
        }

        byte[] content = latestImage.getImageBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "cachedThumbnail", params = { "index" }, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> cachedThumbnail(@RequestParam(value = "index") long index) {
        byte[] content;
        try{
            FrontdoorImage image = imageRepo.getAllImages().get(index);
            
            content = image.getThumbnailBytes();
            
        } catch (Exception e) {
            content = getImageDummy();
        }

        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg");
        
        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "latestCachedThumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> latestCachedThumbnail() {
        if (latestImage == null) {
            latestImage = imageRepo.getLatestImage();
        }
        byte[] content = latestImage.getThumbnailBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    // Test method using direct onvif library
//    @RequestMapping(value = "onvif", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @RequestMapping(value = "onvif", method = RequestMethod.GET, produces = {"text/html"})
    @ResponseBody
    public Model onvifImage(Model model) {
        byte[] content = getImageDummy();
        
        try {
            // Replace these values with your camera data!
            OnvifDevice onvifDevice = new OnvifDevice(ONVIF_CAMERA_IP);
            Date nvtDate = onvifDevice.getDevices().getDate();
            LOG.info("Onvif camera date : " + new SimpleDateFormat().format(nvtDate));

            List<Profile> profiles = onvifDevice.getDevices().getProfiles();
            LOG.info(profiles == null ? "null" : profiles.size() + " profiles available");
            
            MediaDevices mediaDevices = onvifDevice.getMedia();
            int profileNumber = 0;
            for(Profile profile : profiles) {
                LOG.debug("profile " + profileNumber);
                String profileToken = profile.getToken();
                String profileName = profile.getName();
                LOG.info("profile ["+ profileNumber +"] has token:" + profileToken + " and Name:" + profileName);
                
                LOG.info("getHTTPStreamUri: " + mediaDevices.getHTTPStreamUri(profileNumber));
                LOG.info("getRTSPStreamUri: " + mediaDevices.getRTSPStreamUri(profileNumber));
                LOG.info("getSnapshotUri: " + mediaDevices.getSnapshotUri(profileToken));
                LOG.info("getSceenshotUri: " + mediaDevices.getSceenshotUri(profileToken));
                LOG.info("getTCPStreamUri: " + mediaDevices.getTCPStreamUri(profileNumber));
                LOG.info("getUDPStreamUri: " + mediaDevices.getUDPStreamUri(profileNumber));
                LOG.info("getVideoEncoderConfigurationOptions: " + mediaDevices.getVideoEncoderConfigurationOptions(profileToken));
                profileNumber++;
            }
            
            
            // default info
            LOG.info("getDefaultSnapshotUri: " + mediaDevices.getDefaultSnapshotUri());
            LOG.info("getDefaultHTTPStreamUri: " + mediaDevices.getDefaultHTTPStreamUri());
            LOG.info("getDefaultSceenshotUri: " + mediaDevices.getDefaultSceenshotUri());
            for (VideoSource videoSource: mediaDevices.getVideoSources()) {
                LOG.info("  videoSource with token: " + videoSource.getToken());
            }
            
            return model.addAttribute("persons", "niks");
        }
        catch (ConnectException e) {
            LOG.error("Could not connect to onvif camera.", e);
        }
        catch (SOAPException e) {
            LOG.error("SOAPException on onvif connection", e);
        }
        catch (Exception e) {
            LOG.error("Exception on onvif connection", e);
        }
        return null;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "image/jpg");
//        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    private byte[] getImageDummy() {
        byte[] fileContent;
        InputStream is = servletContext.getResourceAsStream("webresources/img/failedSmall.jpg");
        try {
            fileContent = IOUtils.toByteArray(is);
        } catch (IOException e1) {
            fileContent = new byte[0];
        }
        return fileContent;
    }
}
