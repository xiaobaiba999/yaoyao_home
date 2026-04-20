package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Photo;
import com.yaoyao.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public Result<List<Photo>> list() {
        List<Photo> data = photoService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Photo> upload(
            @RequestParam("photo") MultipartFile file,
            @RequestParam(value = "description", required = false, defaultValue = "") String description) {
        Photo data = photoService.upload(file, description);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        photoService.delete(id);
        return Result.success("删除成功");
    }
}
