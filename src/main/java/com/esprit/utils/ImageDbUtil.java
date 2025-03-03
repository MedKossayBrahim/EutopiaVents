package com.esprit.utils;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

public class ImageDbUtil {

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private static final String apiKey = "fa6c553a7bc9ea2ba9a748bb33b056ef";


    public static String[] uploadFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + filePath);
        }

        // Generate a unique file name
        String uniqueFileName = generateUniqueFileName(file.getName());

        // Open a connection to the ImgBB API
        URL url = new URL(IMGBB_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true)) {

            // Add API key as a form field
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"key\"").append("\r\n");
            writer.append("\r\n");
            writer.append(apiKey).append("\r\n");

            // Add file as a form field
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + uniqueFileName + "\"").append("\r\n");
            writer.append("Content-Type: " + Files.probeContentType(file.toPath())).append("\r\n");
            writer.append("\r\n");
            writer.flush();

            // Write file content
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // End of file part
            writer.append("\r\n").append("--" + boundary + "--").append("\r\n");
        }

        // Read the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse the JSON response to extract the image URL
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getBoolean("success")) {
                    String imageUrl = jsonResponse.getJSONObject("data").getString("url");
                    return new String[]{uniqueFileName, imageUrl};
                } else {
                    throw new IOException("Upload failed. API response: " + response);
                }
            }
        } else {
            throw new IOException("Failed to upload file. Response code: " + responseCode);
        }
    }


    private static String generateUniqueFileName(String originalFileName) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return timeStamp + "_" + uuid + fileExtension;
    }
}