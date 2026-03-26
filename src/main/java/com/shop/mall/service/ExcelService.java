package com.shop.mall.service;

import com.shop.mall.entity.Member;
import com.shop.mall.entity.Order;
import com.shop.mall.entity.OrderItem;
import com.shop.mall.entity.Product;
import com.shop.mall.repository.MemberRepository;
import com.shop.mall.repository.OrderRepository;
import com.shop.mall.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExcelService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ExcelService(OrderRepository orderRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    /** 주문 데이터 엑셀 */
    public byte[] exportOrders() throws IOException {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("주문목록");
            CellStyle headerStyle = createHeaderStyle(wb);

            // 헤더
            String[] headers = {"주문번호", "주문자", "주문일시", "상태", "결제방법", "상품내역", "총 금액"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getMember().getNickname());
                row.createCell(2).setCellValue(order.getCreatedAt().format(DATE_FMT));
                row.createCell(3).setCellValue(order.getStatus().getDisplayName());
                row.createCell(4).setCellValue(order.getPaymentMethod().name());

                // 상품내역: "상품A x2, 상품B x1"
                StringBuilder items = new StringBuilder();
                for (OrderItem item : order.getOrderItems()) {
                    if (items.length() > 0) items.append(", ");
                    items.append(item.getProductName()).append(" x").append(item.getQuantity());
                }
                row.createCell(5).setCellValue(items.toString());
                row.createCell(6).setCellValue(order.getTotalPrice());
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toBytes(wb);
        }
    }

    /** 회원목록 엑셀 */
    public byte[] exportMembers() throws IOException {
        List<Member> members = memberRepository.findAll();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("회원목록");
            CellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"ID", "아이디", "닉네임", "이메일", "전화번호", "상태", "가입일"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Member m : members) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getUsername());
                row.createCell(2).setCellValue(m.getNickname());
                row.createCell(3).setCellValue(m.getEmail());
                row.createCell(4).setCellValue(m.getPhone() != null ? m.getPhone() : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(m.getEnabled()) ? "활성" : "비활성");
                row.createCell(6).setCellValue(m.getCreatedAt().format(DATE_FMT));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toBytes(wb);
        }
    }

    /** 상품목록 엑셀 */
    public byte[] exportProducts() throws IOException {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("상품목록");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle lowStockStyle = createLowStockStyle(wb);

            String[] headers = {"ID", "상품명", "카테고리", "가격", "원래가격", "재고", "세일", "신상품", "조회수", "좋아요", "상태", "등록일"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getCategory().name());
                row.createCell(3).setCellValue(p.getPrice());
                row.createCell(4).setCellValue(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0);
                Cell stockCell = row.createCell(5);
                stockCell.setCellValue(p.getStock());
                if (p.getStock() <= 5) {
                    stockCell.setCellStyle(lowStockStyle);
                }
                row.createCell(6).setCellValue(p.isSale() ? "Y" : "N");
                row.createCell(7).setCellValue(p.isNew() ? "Y" : "N");
                row.createCell(8).setCellValue(p.getViewCount());
                row.createCell(9).setCellValue(p.getWishCount());
                row.createCell(10).setCellValue(p.isEnabled() ? "활성" : "비활성");
                row.createCell(11).setCellValue(p.getCreatedAt().format(DATE_FMT));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toBytes(wb);
        }
    }

    // === 유틸 ===

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createLowStockStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        return style;
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
