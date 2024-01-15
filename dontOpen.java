import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class dontOpen {
    public static void convertToGrayscale(String str) throws IOException {

        BufferedImage img = null;
        File f = null;

        // read image
        String pathName = "src\\input\\" + str;
        try {
            f = new File(pathName);
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println(e);
        }

        // get image's width and height
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        // convert to grayscale
        for (int i = 0; i < pixels.length; i++) {

            // Here i denotes the index of array of pixels
            // for modifying the pixel value.
            int p = pixels[i];

            int a = (p >> 24) & 0xff;
            int r = (p >> 16) & 0xff;
            int g = (p >> 8) & 0xff;
            int b = p & 0xff;

            // calculate average
            int avg = (r + g + b) / 3;

            // replace RGB value with avg
            p = (a << 24) | (avg << 16) | (avg << 8) | avg;

            pixels[i] = p;
        }
        img.setRGB(0, 0, width, height, pixels, 0, width);
        // write image

        int dotIndex = str.lastIndexOf('.');
        String fileName = str.substring(0, dotIndex);
        String outputPathName = "src\\output\\" + fileName + "_grayscale.png";
        try {
            f = new File(
                    outputPathName);
            ImageIO.write(img, "png", f);
            System.out.println("converted to grayscale successfully");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void mirrorImage(String str) throws IOException {
        // BufferedImage for source image
        BufferedImage simg = null;

        // File object
        File f = null;
        String pathName = "src\\input\\" + str;

        // Read source image file
        try {
            f = new File(
                    pathName);
            simg = ImageIO.read(f);
        }

        catch (IOException e) {
            System.out.println("Error: " + e);
        }

        // Get source image dimension
        int width = simg.getWidth();
        int height = simg.getHeight();

        // BufferedImage for mirror image
        BufferedImage mimg = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);

        // Create mirror image pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int lx = 0, rx = width - 1; lx < width; lx++, rx--) {

                // lx starts from the left side of the image
                // rx starts from the right side of the
                // image lx is used since we are getting
                // pixel from left side rx is used to set
                // from right side get source pixel value
                int p = simg.getRGB(lx, y);

                // set mirror image pixel value
                mimg.setRGB(rx, y, p);
            }
        }

        // save mirror image

        int dotIndex = str.lastIndexOf('.');
        String fileName = str.substring(0, dotIndex);
        String outputPathName = "src\\output\\" + fileName + "_mirror.png";
        try {
            f = new File(
                    outputPathName);
            ImageIO.write(mimg, "png", f);
            System.out.println("Mirror image created succefully");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public static void compressImage(String str) throws IOException {
        String inputImagePath = "src\\input\\" + str;
        String outputFolderPath = "src\\output\\";
        int dotIndex = str.lastIndexOf('.');
        String fileName = str.substring(0, dotIndex);
        String outputFileName = fileName + "_compressed.png";
        float compressionQuality = 0.5f; // Adjust the compression quality (0.0 to 1.0)

        try {
            // Load the original image
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));

            // Create a folder if it doesn't exist
            File outputFolder = new File(outputFolderPath);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // Compress the image
            compressAndSaveImage(originalImage, outputFolderPath + outputFileName, compressionQuality);

            System.out.println("Image compression successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compressAndSaveImage(BufferedImage originalImage, String outputPath, float quality)
            throws IOException {
        // Get the output file format from the file name
        String formatName = outputPath.substring(outputPath.lastIndexOf(".") + 1);

        // Create a compressed image with the specified quality
        BufferedImage compressedImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        compressedImage.createGraphics().drawImage(originalImage, 0, 0, originalImage.getWidth(),
                originalImage.getHeight(), null);

        // Write the compressed image to the output file
        ImageIO.write(compressedImage, formatName, new File(outputPath));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("-------------------------");
        System.out.println("JAVA IMAGE EDITING APP");
        System.out.println("-------------------------");
        System.out.println("ENTER THE NAME OF THE IMAGE");
        String str = sc.next();

        try {
            // Create a fixed-size thread pool with 3 threads
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            // Submit tasks to the thread pool
            executorService.submit(() -> {
                try {
                    convertToGrayscale(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            executorService.submit(() -> {
                try {
                    mirrorImage(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            executorService.submit(() -> {
                try {
                    compressImage(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Shutdown the thread pool
            executorService.shutdown();

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            sc.close();
        }

    }
}
