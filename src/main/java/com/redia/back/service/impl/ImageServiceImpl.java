package com.redia.back.service.impl;

import com.redia.back.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    public ImageServiceImpl(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dgvn4n9ry");
        config.put("api_key", "658511773432214");
        config.put("api_secret", "MsvQCfUmnxkQtFn3HBa2lLZ7y_c");
        cloudinary = new Cloudinary(config);
    }
    // Constructor para tests
    public ImageServiceImpl(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    @Override
    public Map upload(MultipartFile image) throws Exception {
        File file = convert(image);
        return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "app_name"));
    }

    @Override
    public Map delete(String imageId) throws Exception {
        return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(image.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(image.getBytes());
        fos.close();
        return file;
    }
}
