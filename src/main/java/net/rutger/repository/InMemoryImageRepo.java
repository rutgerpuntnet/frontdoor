package net.rutger.repository;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryImageRepo {

    private static final Logger LOG = Logger.getLogger(InMemoryImageRepo.class);

    private static final int MAX_NUMBER_OF_IMAGES = 100;

    private BoundedSortedMap<Long,FrontdoorImage> imageMap = new BoundedSortedMap<Long,FrontdoorImage>(MAX_NUMBER_OF_IMAGES);

    public void addImage(byte[] newImage) {
        FrontdoorImage image = new FrontdoorImage(newImage);
        imageMap.put(image.getDate().getTime(),image);
    }

    public FrontdoorImage getLatestImage() {
        return imageMap.isEmpty() ? null : imageMap.lastEntry().getValue();
    }

    public BoundedSortedMap<Long,FrontdoorImage> getAllImages() {
        return imageMap;
    }

    public class FrontdoorImage {
        private byte[] imageBytes;
        private byte[] thumbnailBytes;
        private Date date;

        public FrontdoorImage(final byte[] newImage) {
            this.imageBytes = newImage;
            date = new Date();

            try {
                thumbnailBytes = createThumbnail(newImage);
            } catch (IOException e) {
                LOG.error("Exception while creating thumbnail", e);
            }
        }

        private byte[] createThumbnail(byte[] newImage) throws IOException {
            Image image = ImageIO.read(new ByteArrayInputStream(newImage));

            Image scaledImage = image.getScaledInstance(192, 108, Image.SCALE_FAST);

            BufferedImage bufferedImage = new BufferedImage(192, 108, BufferedImage.TYPE_INT_RGB);
            Graphics gc = bufferedImage.createGraphics();
            gc.drawImage(scaledImage, 0, 0, null);
            gc.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", bos);

            return bos.toByteArray();

        }

        public byte[] getImageBytes() {
            return imageBytes;
        }

        public void setImageBytes(byte[] imageBytes) {
            this.imageBytes = imageBytes;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public byte[] getThumbnailBytes() {
            return thumbnailBytes;
        }

        public void setThumbnailBytes(byte[] thumbnailBytes) {
            this.thumbnailBytes = thumbnailBytes;
        }

    }
}
