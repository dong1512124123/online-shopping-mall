package com.shop.mall.controller;

import com.shop.mall.service.ExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/orders")
    public ResponseEntity<byte[]> downloadOrders() throws IOException {
        byte[] data = excelService.exportOrders();
        return buildResponse(data, "주문목록");
    }

    @GetMapping("/members")
    public ResponseEntity<byte[]> downloadMembers() throws IOException {
        byte[] data = excelService.exportMembers();
        return buildResponse(data, "회원목록");
    }

    @GetMapping("/products")
    public ResponseEntity<byte[]> downloadProducts() throws IOException {
        byte[] data = excelService.exportProducts();
        return buildResponse(data, "상품목록");
    }

    private ResponseEntity<byte[]> buildResponse(byte[] data, String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = prefix + "_" + date + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
