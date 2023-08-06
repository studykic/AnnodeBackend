package com.ikchi.annode.controller;

import com.ikchi.annode.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileUploadController {


    private final FileUploadService fileUploadService;


}
