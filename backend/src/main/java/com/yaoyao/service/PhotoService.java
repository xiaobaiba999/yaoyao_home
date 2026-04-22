package com.yaoyao.service;

import com.yaoyao.entity.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {

    List<Photo> list();

    Photo getById(String id);

    Photo upload(MultipartFile file, String description);

    void delete(String id);
}
