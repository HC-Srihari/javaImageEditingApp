import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class project {
    public static void convertToGrayscale(String str) throws IOException,SQLException {

        BufferedImage img = null;
        File f = null;

        // read image
        String pathName = "imageEditor\\src\\input\\" + str;
        // "imageEditor\src\input\img1.png"
        try {
            f = new File(pathName);
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("inside grayscale");
            System.out.println("ERROR: Entered file doesn't exist");
            System.exit(0);
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
        
        fileName = fileName + "_grayscale.png";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", byteArrayOutputStream);
        byte[] imgData = byteArrayOutputStream.toByteArray();
        storeToDB(fileName, imgData);

    }

    public static void mirrorImage(String str) throws IOException,SQLException {
        // BufferedImage for source image
        BufferedImage simg = null;

        // File object
        File f = null;
        String pathName = "imageEditor\\src\\input\\" + str;

        // Read source image file
        try {
            f = new File(
                    pathName);
            simg = ImageIO.read(f);
        }

        catch (IOException e) {
            System.out.println("inside mirror");
            System.out.println("Error: Entered file doesn't exist  \n"+  e );
            e.printStackTrace();
            System.exit(0);
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

                int p = simg.getRGB(lx, y);

                // set mirror image pixel value
                mimg.setRGB(rx, y, p);
            }
        }

        // save mirror image

        int dotIndex = str.lastIndexOf('.');
        String fileName = str.substring(0, dotIndex);
       
        fileName = fileName + "_mirror.png";
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(mimg, "png", byteArrayOutputStream);
        byte[] imgData = byteArrayOutputStream.toByteArray();
        storeToDB(fileName, imgData);

    }

    public static void compressImage(String str) throws IOException ,SQLException{
        String inputImagePath = "imageEditor\\src\\input\\" + str;
        String outputFolderPath = "imageEditor\\src\\output\\";
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
            compressAndSaveImage(originalImage,  outputFileName, compressionQuality);

            // System.out.println("Image compression successful.");
        } catch (IOException e) {
            
            System.out.println("inside compression");
            System.out.println("ERROR : Entered file doesn't exist");
        }
    }

    private static void compressAndSaveImage(BufferedImage originalImage, String fileName, float quality)
            throws IOException,SQLException {
        

        // Create a compressed image with the specified quality
        BufferedImage compressedImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        compressedImage.createGraphics().drawImage(originalImage, 0, 0, originalImage.getWidth(),
                originalImage.getHeight(), null);

        // Write the compressed image to the output file
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(compressedImage, "png", byteArrayOutputStream);
        byte[] imgData = byteArrayOutputStream.toByteArray();
        storeToDB(fileName, imgData);


    }


    public static void storeToDB(String fileName, byte[] imageData) throws SQLException{
         try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/compressedimages", "root", "manoj")) {
            String sql = "INSERT INTO compressedimages (filename, image_data) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, fileName);
                statement.setBytes(2, imageData);
                statement.executeUpdate();
            }
        }
    }

    public static void retrieveAndSaveImage(String imageName) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/compressedimages", "root","manoj")) 
        {
            String sql = "SELECT filename, image_data FROM CompressedImages WHERE filename = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, imageName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Get image details from the database
                        String fileName = resultSet.getString("filename");
                        byte[] imageData = resultSet.getBytes("image_data");

                        // Save the image data to a file
                        saveImageToFile(fileName, imageData);
                        System.out.println(fileName + "     Succesfully saved at ouput folder\n");
                    } else {
                        System.out.println("Image not found with the provided name.");
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    // saves the image into the output folder
    private static void saveImageToFile(String fileName, byte[] imageData) throws IOException {
        String outputPath = "imageEditor\\src\\output\\" + fileName;
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(imageData);
        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("-------------------------");
        System.out.println("JAVA IMAGE EDITING APP");
        System.out.println("-------------------------");
        System.out.println("ENTER THE COMPLETE NAME OF THE IMAGE (ex: image1.png) :");
        String str = sc.next();

        try {
            // Create a fixed-size thread pool with 3 threads
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            // Submit tasks to the thread pool
            executorService.submit(() -> {
                try {
                    convertToGrayscale(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            executorService.submit(() -> {
                try {
                    mirrorImage(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            executorService.submit(() -> {
                try {
                    compressImage(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Shutdown the thread pool
            executorService.shutdown();

        } catch (Exception e) {
            System.out.println(e);
        }

        while(true){
            System.out.println("1.Compress image\n2.Convert Image to GrayScale\n3.Mirror Image\n4.Exit ");
            System.out.println("Enter the action to perform : ");
            int choice = sc.nextInt();

            int dotIndex = str.lastIndexOf('.');
            String initialFileName = str.substring(0, dotIndex);

            if(choice == 1){
                String fileName = initialFileName + "_compressed.png";
                retrieveAndSaveImage(fileName);
            }else if(choice ==2 ){
                String fileName = initialFileName + "_grayscale.png";
                retrieveAndSaveImage(fileName);
            }else if(choice == 3){
                String fileName = initialFileName + "_mirror.png";
                retrieveAndSaveImage(fileName);
            }else{
                System.out.println("Program Terminated  Successfully ......");
                System.exit(0);
            }

            
        }

        

    }
}

