package com.esi.mscours.service;

import com.esi.mscours.entities.Document;
import com.esi.mscours.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;


@Service
public class DocumentService {

    @Autowired

    private DocumentRepository documentRepository;

    private final String FOLDER_PATH="E:\\Projects\\darassni\\code\\ms-course\\src\\main\\java\\com\\esi\\mscours\\service\\MyFIles\\";


    public String uploadImageToFileSystem(MultipartFile file,Long idTeacher) throws IOException {
        String filePath=FOLDER_PATH+file.getOriginalFilename();

        Document document=Document.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .link(filePath).build();
        document.setIdTeacher(idTeacher);
        documentRepository.save(document);

        file.transferTo(new File(filePath));

        if (document != null) {
            return "file uploaded successfully : " + filePath;
        }
        return null;
    }

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<Document> Document = documentRepository.findByName(fileName);
        String filePath=Document.get().getLink();
        byte[] files = Files.readAllBytes(new File(filePath).toPath());
        return files;
    }

}
