package com.esi.mscours.API;

import com.esi.mscours.entities.Document;
import com.esi.mscours.entities.Groupe;
import com.esi.mscours.entities.Lecture;
import com.esi.mscours.repository.DocumentRepository;
import com.esi.mscours.repository.GroupeRepository;
import com.esi.mscours.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1")
public class DocumentController {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private GroupeRepository groupeRepository;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/documents/{id}")
    public List<Document> getDocuments(@PathVariable(value="id") Long idTeacher){
        List<Document> documents=documentRepository.findDocumentByIdTeacher(idTeacher);
        return documents;
    }
    /*
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/add-document")
    public Document addDocument(@RequestBody Map<String,Object> payload){
        Document document=new Document();
        document.setIdTeacher(Long.valueOf(payload.get("idTeacher").toString()));
        document.setName(payload.get("name").toString());
        document.setLink(payload.get("link").toString());
        documentRepository.save(document);
        return document;
    }
    */


    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @DeleteMapping("/documents/{id}")
    public void deleteDocument(@PathVariable(value = "id") Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        // Remove the document from each lecture if it exists
        List<Groupe> groupes = groupeRepository.findGroupesByIdTeacher(document.getIdTeacher());
        for (Groupe groupe : groupes) {
            for (Lecture lecture : groupe.getLectures()) {
                if (lecture.getDocumentList().contains(document)) {
                    lecture.getDocumentList().remove(document);
                }
            }
        }

        documentRepository.delete(document);
    }


    @PostMapping("/fileSystem")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("file") MultipartFile file,@RequestParam("idTeacher") Long idTeacher) throws IOException {
        String uploadFile = documentService.uploadImageToFileSystem(file,idTeacher);

        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadFile);
    }
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/fileSystem/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData=documentService.downloadImageFromFileSystem(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.ALL)
                .body(imageData);

    }

}
