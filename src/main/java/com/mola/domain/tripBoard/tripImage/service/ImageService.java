package com.mola.domain.tripBoard.tripImage.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(MultipartFile file);
    void delete(String fileName);
}
