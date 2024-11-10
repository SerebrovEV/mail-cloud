package org.image.core.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.image.core.dto.ImageDto;
import org.image.core.service.ImageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(
            summary = "Загрузка изображений",
            description = "Метод для загрузки изображений.",
            tags = {"Изображения"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Массив изображений для загрузки",
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "object")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Изображения успешно загружены"),
                    @ApiResponse(responseCode = "403", description = "Некорректные данные")
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile[] images) {
        imageService.uploadImage(images);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получение изображений пользователя",
            description = "Метод для получения изображений пользователя с возможностью фильтрации и сортировки.",
            tags = {"Изображения"},
            parameters = {
                    @Parameter(name = "imageId", description = "ID изображения", required = false),
                    @Parameter(name = "date", description = "Дата создания изображения в формате dd.MM.yyyy", required = false),
                    @Parameter(name = "size", description = "Размер изоражений", required = false),
                    @Parameter(name = "sortBy", description = "Поле для сортировки", required = false, example = "id"),
                    @Parameter(name = "orderBy", description = "Порядок сортировки (asc или desc)", required = false, example = "asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список изображений успешно получен"),
                    @ApiResponse(responseCode = "403", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "500", description = "Размер файла привышен или размер всех файлов превышен")
            }
    )
    @GetMapping("/get")
    public ResponseEntity<List<ImageDto>> getUserImages(@RequestParam(value = "imageId", required = false) Long imageId,
                                                        @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date,
                                                        @RequestParam(value = "size", required = false) Long size,
                                                        @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
                                                        @RequestParam(value = "orderBy", required = false, defaultValue = "asc") String orderBy) {
        List<ImageDto> res = imageService.getUserImages(imageId, date, size, sortBy, orderBy);
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Получение изображений пользователя для модератора",
            description = "Метод для получения изображений пользователя с возможностью фильтрации и сортировки для модератора.",
            tags = {"Изображения"},
            parameters = {
                    @Parameter(name = "userId", description = "ID пользователя", required = false),
                    @Parameter(name = "imageId", description = "ID изображения", required = false),
                    @Parameter(name = "date", description = "Дата создания изображения в формате dd.MM.yyyy", required = false),
                    @Parameter(name = "size", description = "Размер изображений", required = false),
                    @Parameter(name = "sortBy", description = "Поле для сортировки", required = false, example = "id"),
                    @Parameter(name = "orderBy", description = "Порядок сортировки (asc или desc)", required = false, example = "asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список изображений успешно получен"),
                    @ApiResponse(responseCode = "403", description = "Нет прав для доступа к списку")
            }
    )
    @GetMapping("/getUserImages")
    public ResponseEntity<List<ImageDto>> getUserImagesForModerator(@RequestParam(value = "userId", required = false) Long userId,
                                                                    @RequestParam(value = "imageId", required = false) Long imageId,
                                                                    @RequestParam(value = "date", required = false) LocalDate date,
                                                                    @RequestParam(value = "size", required = false) Long size,
                                                                    @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
                                                                    @RequestParam(value = "orderBy", required = false, defaultValue = "asc") String orderBy) {
        List<ImageDto> res = imageService.getUserImagesForModerator(userId, imageId, date, size, sortBy, orderBy);
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Скачивание изображения",
            description = "Метод для скачивания изображения по его ID.",
            tags = {"Изображения"},
            parameters = {
                    @Parameter(name = "id", description = "ID изображения", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Изображение успешно скачано"),
                    @ApiResponse(responseCode = "404", description = "Изображение не найдено")
            }
    )
    @GetMapping("/download/{id}")
    public ResponseEntity<String> downloadImage(@PathVariable("id") Long imageId) {
        String res = imageService.downloadUserImage(imageId);
        return ResponseEntity.ok(res);
    }
}
