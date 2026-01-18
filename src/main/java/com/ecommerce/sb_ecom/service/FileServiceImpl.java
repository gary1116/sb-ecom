package com.ecommerce.sb_ecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements  FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // get File names of current/original file
        String originalFileName= file.getOriginalFilename();

        //generate a unique file name
        String randomId= UUID.randomUUID().toString();
//        if file name-> Gary.jpg -> random id -> 1234-> it will be saved as 1234.jpg
        // this will give the extension originalFileName.substring(originalFileName.lastIndexOf('.')
        // like .jpg, .jpeg and concat with randomId created above
        String fileName= randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        //pathSeparator is nothing but a forward slash "/"
        String filePath=path+ File.separator+fileName;

        //check if path exist and create
        File folder=new File(path);
        if(!folder.exists())
            folder.mkdir();

        // upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return fileName;
    }

}
