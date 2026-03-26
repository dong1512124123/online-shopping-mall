package com.shop.mall.config;

import com.shop.mall.entity.*;
import com.shop.mall.enums.Category;
import com.shop.mall.enums.OrderStatus;
import com.shop.mall.enums.PaymentMethod;
import com.shop.mall.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Profile("!prod")  // 운영 환경에서는 테스트 데이터 생성 안 함
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private final Random random = new Random(42);
    private int orderCounter = 0;
    private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final PaymentMethod[] PAY_METHODS = PaymentMethod.values();

    public DataInitializer(AdminRepository adminRepository,
                           ProductRepository productRepository,
                           MemberRepository memberRepository,
                           AddressRepository addressRepository,
                           OrderRepository orderRepository,
                           CartItemRepository cartItemRepository,
                           WishlistRepository wishlistRepository,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.adminRepository = adminRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 1. 관리자 계정
        if (!adminRepository.existsByUsername("admin")) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setName("관리자");
            adminRepository.save(admin);
            System.out.println("[초기화] 관리자 계정 생성 완료");
        }

        // 2. 샘플 상품
        if (productRepository.count() == 0) {
            createProducts();
            System.out.println("[초기화] 샘플 상품 " + productRepository.count() + "개 생성 완료");
        }

        // 3. 테스트 데이터 (회원, 주문, 장바구니, 위시리스트)
        if (memberRepository.count() == 0) {
            createTestData();
        }
    }

    // ================================================================
    //  테스트 데이터 생성
    // ================================================================

    private void createTestData() {
        List<Member> members = createMembers();
        createAddresses(members);
        List<Product> products = productRepository.findAll();

        create2025Orders(members, products);   // 매출 데이터용 (배송완료)
        create2026Orders(members, products);   // 올해 주문 (~60건, 다양한 상태)
        createCartItems(members, products);
        createWishlistItems(members, products);

        System.out.println("[초기화] ===== 테스트 데이터 생성 완료 =====");
    }

    // ── 회원 10명 ──

    private List<Member> createMembers() {
        String[][] data = {
            {"hong123",  "홍길동", "hong@test.com",      "010-1234-5678"},
            {"kim_yuna", "김유나", "yuna@test.com",      "010-2345-6789"},
            {"lee_jh",   "이지현", "jihyun@test.com",    "010-3456-7890"},
            {"park_ms",  "박민수", "minsu@test.com",     "010-4567-8901"},
            {"choi_ey",  "최은영", "eunyoung@test.com",  "010-5678-9012"},
            {"jung_dw",  "정대우", "daewoo@test.com",    "010-6789-0123"},
            {"kang_sh",  "강서현", "seohyun@test.com",   "010-7890-1234"},
            {"yoon_jk",  "윤준규", "junkyu@test.com",    "010-8901-2345"},
            {"shin_hj",  "신혜진", "hyejin@test.com",    "010-9012-3456"},
            {"oh_sw",    "오승원", "seungwon@test.com",  "010-0123-4567"},
        };

        LocalDateTime[] joinDates = {
            LocalDateTime.of(2024, 5, 15, 10, 30),
            LocalDateTime.of(2024, 8, 22, 14, 15),
            LocalDateTime.of(2024, 11, 3, 9, 0),
            LocalDateTime.of(2025, 1, 10, 11, 45),
            LocalDateTime.of(2025, 3, 18, 16, 20),
            LocalDateTime.of(2025, 6, 5, 13, 10),
            LocalDateTime.of(2025, 8, 20, 10, 0),
            LocalDateTime.of(2025, 10, 12, 15, 30),
            LocalDateTime.of(2025, 12, 1, 9, 45),
            LocalDateTime.of(2026, 1, 15, 11, 20),
        };

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            Member m = new Member();
            m.setUsername(data[i][0]);
            m.setPassword(passwordEncoder.encode("test1234"));
            m.setNickname(data[i][1]);
            m.setEmail(data[i][2]);
            m.setPhone(data[i][3]);
            m = memberRepository.save(m);

            jdbcTemplate.update(
                "UPDATE member SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(joinDates[i]), Timestamp.valueOf(joinDates[i]), m.getId());

            members.add(m);
        }
        System.out.println("[초기화] 테스트 회원 " + members.size() + "명 생성 완료 (비밀번호: test1234)");
        return members;
    }

    // ── 배송지 ──

    private void createAddresses(List<Member> members) {
        String[][] addrs = {
            {"홍길동", "010-1234-5678", "06141", "서울시 강남구 테헤란로 123",       "101동 501호"},
            {"김유나", "010-2345-6789", "03181", "서울시 종로구 세종대로 175",       "A동 302호"},
            {"이지현", "010-3456-7890", "48060", "부산시 해운대구 해운대로 456",     "해운대아파트 1203호"},
            {"박민수", "010-4567-8901", "41585", "대구시 북구 대학로 80",            "행복타운 405호"},
            {"최은영", "010-5678-9012", "61452", "광주시 동구 금남로 200",           "빌라 201호"},
            {"정대우", "010-6789-0123", "34126", "대전시 유성구 대학로 99",          "연구단지 B동 701호"},
            {"강서현", "010-7890-1234", "21999", "인천시 연수구 컨벤시아대로 100",   "센트럴파크 1502호"},
            {"윤준규", "010-8901-2345", "16499", "수원시 팔달구 인계로 180",         "수원타운 803호"},
            {"신혜진", "010-9012-3456", "13494", "성남시 분당구 판교역로 235",       "판교테크노 1104호"},
            {"오승원", "010-0123-4567", "10409", "고양시 일산동구 중앙로 1261",     "일산빌리지 606호"},
        };

        for (int i = 0; i < members.size(); i++) {
            Address addr = new Address();
            addr.setMember(members.get(i));
            addr.setRecipientName(addrs[i][0]);
            addr.setRecipientPhone(addrs[i][1]);
            addr.setZipcode(addrs[i][2]);
            addr.setAddress(addrs[i][3]);
            addr.setAddressDetail(addrs[i][4]);
            addr.setIsDefault(true);
            addressRepository.save(addr);
        }
        System.out.println("[초기화] 배송지 " + members.size() + "개 생성 완료");
    }

    // ── 2025년 주문 (매출 데이터용 — 전부 배송완료) ──

    private void create2025Orders(List<Member> members, List<Product> products) {
        // 월별 매출 목표 (만원): 1000만~3000만 사이
        int[] targets = {1200, 1500, 1800, 1400, 2000, 2200, 2500, 2800, 2100, 1900, 2600, 3000};
        int totalOrders = 0;

        for (int month = 1; month <= 12; month++) {
            int targetWon = targets[month - 1] * 10000;
            int revenue = 0;

            while (revenue < targetWon) {
                Member member = pickRandom(members);
                LocalDateTime date = randomDate(2025, month);
                Order order = buildAndSaveOrder(member, products, date, OrderStatus.DELIVERED);
                revenue += order.getTotalPrice();
                totalOrders++;
            }
        }
        System.out.println("[초기화] 2025년 주문 " + totalOrders + "건 생성 완료 (매출 데이터)");
    }

    // ── 2026년 주문 (~60건, 다양한 상태) ──

    private void create2026Orders(List<Member> members, List<Product> products) {
        // 1월: 오래된 주문 → 배송완료 위주
        // 2월: 중간 → 배송중/배송준비 섞임
        // 3월: 최근 → 주문접수/결제완료 위주
        int[][] monthConfig = {
            // {월, 목표매출(만원), DELIVERED, SHIPPING, PREPARING, PAID, PENDING, CANCELLED, REFUNDED}
            {1, 1800, 10, 2, 1, 0, 0, 3, 2},   // 18건
            {2, 2200,  5, 4, 3, 2, 0, 3, 2},   // 19건
            {3, 1500,  2, 3, 4, 5, 3, 4, 2},   // 23건
        };

        int totalOrders = 0;

        for (int[] c : monthConfig) {
            int month = c[0];
            int targetWon = c[1] * 10000;

            // 유효 주문 상태 배열 (취소/환불 제외 — 매출에 포함됨)
            List<OrderStatus> validStatuses = new ArrayList<>();
            for (int i = 0; i < c[2]; i++) validStatuses.add(OrderStatus.DELIVERED);
            for (int i = 0; i < c[3]; i++) validStatuses.add(OrderStatus.SHIPPING);
            for (int i = 0; i < c[4]; i++) validStatuses.add(OrderStatus.PREPARING);
            for (int i = 0; i < c[5]; i++) validStatuses.add(OrderStatus.PAID);
            for (int i = 0; i < c[6]; i++) validStatuses.add(OrderStatus.PENDING);
            Collections.shuffle(validStatuses, random);

            int revenue = 0;

            // 지정된 상태의 주문 생성
            for (OrderStatus status : validStatuses) {
                Member member = pickRandom(members);
                LocalDateTime date = randomDate(2026, month);
                Order order = buildAndSaveOrder(member, products, date, status);
                revenue += order.getTotalPrice();
                totalOrders++;
            }

            // 매출 목표 미달 시 추가 DELIVERED 주문
            while (revenue < targetWon) {
                Member member = pickRandom(members);
                LocalDateTime date = randomDate(2026, month);
                Order order = buildAndSaveOrder(member, products, date, OrderStatus.DELIVERED);
                revenue += order.getTotalPrice();
                totalOrders++;
            }

            // 취소 주문 (매출 불포함)
            for (int i = 0; i < c[7]; i++) {
                Member member = pickRandom(members);
                LocalDateTime date = randomDate(2026, month);
                buildAndSaveOrder(member, products, date, OrderStatus.CANCELLED);
                totalOrders++;
            }

            // 환불 주문 (매출 불포함)
            for (int i = 0; i < c[8]; i++) {
                Member member = pickRandom(members);
                LocalDateTime date = randomDate(2026, month);
                buildAndSaveOrder(member, products, date, OrderStatus.REFUNDED);
                totalOrders++;
            }
        }
        System.out.println("[초기화] 2026년 주문 " + totalOrders + "건 생성 완료 (다양한 상태)");
    }

    // ── 장바구니 ──

    private void createCartItems(List<Member> members, List<Product> products) {
        int count = 0;
        for (int i = 0; i < 5; i++) {
            int itemCount = 2 + random.nextInt(3);
            Set<Long> usedIds = new HashSet<>();
            for (int j = 0; j < itemCount; j++) {
                Product p = pickRandomUnique(products, usedIds);
                CartItem item = new CartItem();
                item.setMember(members.get(i));
                item.setProduct(p);
                item.setQuantity(1 + random.nextInt(3));
                cartItemRepository.save(item);
                usedIds.add(p.getId());
                count++;
            }
        }
        System.out.println("[초기화] 장바구니 " + count + "개 생성 완료");
    }

    // ── 위시리스트 ──

    private void createWishlistItems(List<Member> members, List<Product> products) {
        int count = 0;
        for (int i = 0; i < 7; i++) {
            int itemCount = 2 + random.nextInt(4);
            Set<Long> usedIds = new HashSet<>();
            for (int j = 0; j < itemCount; j++) {
                Product p = pickRandomUnique(products, usedIds);
                Wishlist w = new Wishlist();
                w.setMember(members.get(i));
                w.setProduct(p);
                wishlistRepository.save(w);
                usedIds.add(p.getId());

                jdbcTemplate.update("UPDATE product SET wish_count = wish_count + 1 WHERE id = ?", p.getId());
                count++;
            }
        }
        System.out.println("[초기화] 위시리스트 " + count + "개 생성 완료");
    }

    // ================================================================
    //  주문 생성 헬퍼
    // ================================================================

    private Order buildAndSaveOrder(Member member, List<Product> products,
                                    LocalDateTime orderDate, OrderStatus status) {
        // 배송지 조회
        Address addr = addressRepository.findByMemberAndIsDefaultTrue(member).orElse(null);

        Order order = new Order();
        order.setMember(member);
        order.setOrderNumber("ORD" + orderDate.format(ORDER_FMT) + String.format("%04d", orderCounter++));
        order.setStatus(status);
        order.setPaymentMethod(PAY_METHODS[random.nextInt(PAY_METHODS.length)]);

        if (addr != null) {
            order.setRecipientName(addr.getRecipientName());
            order.setRecipientPhone(addr.getRecipientPhone());
            order.setZipcode(addr.getZipcode());
            order.setAddress(addr.getAddress());
            order.setAddressDetail(addr.getAddressDetail());
        } else {
            order.setRecipientName(member.getNickname());
            order.setRecipientPhone(member.getPhone() != null ? member.getPhone() : "010-0000-0000");
            order.setZipcode("06141");
            order.setAddress("서울시 강남구 테헤란로 123");
            order.setAddressDetail("101동 501호");
        }

        // 주문 상품 2~4개, 수량 2~9
        int itemCount = 2 + random.nextInt(3);
        int total = 0;
        Set<Long> usedIds = new HashSet<>();

        for (int i = 0; i < itemCount; i++) {
            Product p = pickRandomUnique(products, usedIds);
            int qty = 2 + random.nextInt(8);

            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setProductName(p.getName());
            item.setPrice(p.getPrice());
            item.setQuantity(qty);
            order.addOrderItem(item);

            usedIds.add(p.getId());
            total += p.getPrice() * qty;
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        // created_at을 원하는 날짜로 변경
        jdbcTemplate.update(
            "UPDATE orders SET created_at = ?, updated_at = ? WHERE id = ?",
            Timestamp.valueOf(orderDate), Timestamp.valueOf(orderDate), saved.getId());

        return saved;
    }

    // ================================================================
    //  유틸리티
    // ================================================================

    private LocalDateTime randomDate(int year, int month) {
        int maxDay;
        if (month == 2) {
            maxDay = (year % 4 == 0) ? 29 : 28;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            maxDay = 30;
        } else {
            maxDay = 31;
        }
        // 2026년 3월은 오늘(26일)까지만
        if (year == 2026 && month == 3) maxDay = 26;

        int day = 1 + random.nextInt(maxDay);
        int hour = 8 + random.nextInt(14);
        return LocalDateTime.of(year, month, day, hour, random.nextInt(60), random.nextInt(60));
    }

    private <T> T pickRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private Product pickRandomUnique(List<Product> products, Set<Long> usedIds) {
        Product p;
        int attempts = 0;
        do {
            p = products.get(random.nextInt(products.size()));
            attempts++;
            if (attempts > 100) break; // 무한루프 방지
        } while (usedIds.contains(p.getId()));
        return p;
    }

    // ================================================================
    //  상품 생성 (기존)
    // ================================================================

    private void createProducts() {
        // ── MAN 카테고리 (10) ──
        saveProduct("오버핏 코튼 티셔츠", "부드러운 코튼 소재의 오버핏 티셔츠. 편안한 착용감.", 29000, null, Category.MAN, 50, false, true, "/images/products/man-1.svg");
        saveProduct("슬림핏 데님 팬츠", "클래식한 슬림핏 데님. 스트레치 원단으로 활동성 우수.", 59000, null, Category.MAN, 30, false, true, "/images/products/man-2.svg");
        saveProduct("린넨 블렌드 셔츠", "여름 필수 아이템. 시원한 린넨 혼방 소재.", 45000, 65000, Category.MAN, 40, true, false, "/images/products/man-3.svg");
        saveProduct("스포츠 조거 팬츠", "데일리 조거팬츠. 편한 고무밴드 허리.", 35000, null, Category.MAN, 60, false, true, "/images/products/man-4.svg");
        saveProduct("캐주얼 후드 집업", "간절기 필수템. 가벼운 후드 집업.", 55000, 79000, Category.MAN, 25, true, false, "/images/products/man-5.svg");
        saveProduct("베이직 폴로셔츠", "깔끔한 카라 디자인의 폴로셔츠.", 32000, null, Category.MAN, 45, false, true, "/images/products/man-6.svg");
        saveProduct("울 블렌드 카디건", "가을 데일리 카디건. 울 혼방 소재로 따뜻한 착용감.", 65000, null, Category.MAN, 20, false, true, "/images/products/man-7.svg");
        saveProduct("스트라이프 긴팔 티", "깔끔한 스트라이프 패턴. 데일리 롱슬리브.", 33000, null, Category.MAN, 45, false, true, "/images/products/man-8.svg");
        saveProduct("치노 반바지", "여름 필수 면 반바지. 깔끔한 치노 핏.", 39000, null, Category.MAN, 55, false, true, "/images/products/man-9.svg");
        saveProduct("테일러드 블레이저", "세미 정장 블레이저. 비즈니스 캐주얼 필수템.", 89000, 120000, Category.MAN, 15, true, false, "/images/products/man-10.svg");

        // ── WOMAN 카테고리 (10) ──
        saveProduct("플라워 패턴 원피스", "로맨틱한 플라워 프린트 미디 원피스.", 68000, null, Category.WOMAN, 35, false, true, "/images/products/woman-1.svg");
        saveProduct("하이웨스트 와이드 팬츠", "다리가 길어보이는 하이웨스트 핏.", 49000, null, Category.WOMAN, 40, false, true, "/images/products/woman-2.svg");
        saveProduct("크롭 카디건", "봄에 딱 맞는 크롭 기장 카디건.", 39000, 55000, Category.WOMAN, 30, true, false, "/images/products/woman-3.svg");
        saveProduct("A라인 미니스커트", "데일리 미니스커트. 안감 포함.", 35000, null, Category.WOMAN, 50, false, true, "/images/products/woman-4.svg");
        saveProduct("쉬폰 블라우스", "우아한 쉬폰 소재 블라우스. 오피스룩으로도 추천.", 42000, 58000, Category.WOMAN, 20, true, false, "/images/products/woman-5.svg");
        saveProduct("데님 자켓", "빈티지 워싱의 크롭 데님 자켓.", 72000, null, Category.WOMAN, 15, false, true, "/images/products/woman-6.svg");
        saveProduct("캐시미어 라운드 니트", "부드러운 캐시미어 니트. 사계절 기본템.", 58000, null, Category.WOMAN, 25, false, true, "/images/products/woman-7.svg");
        saveProduct("클래식 트렌치 코트", "봄가을 필수 트렌치. 클래식 디자인.", 128000, 180000, Category.WOMAN, 12, true, false, "/images/products/woman-8.svg");
        saveProduct("크롭 탑", "여름 데일리 크롭탑. 시원한 소재.", 22000, null, Category.WOMAN, 60, false, true, "/images/products/woman-9.svg");
        saveProduct("와이드 점프수트", "원피스처럼 편한 점프수트. 깔끔한 핏.", 78000, null, Category.WOMAN, 20, false, true, "/images/products/woman-10.svg");

        // ── SPORTS 카테고리 (8) ──
        saveProduct("드라이핏 반팔 티", "흡습속건 기능성 스포츠 티셔츠.", 25000, null, Category.SPORTS, 80, false, true, "/images/products/sports-1.svg");
        saveProduct("트레이닝 레깅스", "4-WAY 스트레치 레깅스. 운동 필수템.", 38000, null, Category.SPORTS, 60, false, true, "/images/products/sports-2.svg");
        saveProduct("경량 윈드브레이커", "초경량 바람막이. 방수 기능 포함.", 65000, 89000, Category.SPORTS, 30, true, false, "/images/products/sports-3.svg");
        saveProduct("스포츠 브라탑", "서포트력 좋은 스포츠 브라. 요가/필라테스 추천.", 28000, null, Category.SPORTS, 50, false, true, "/images/products/sports-4.svg");
        saveProduct("트레이닝 트랙 팬츠", "편안한 트랙 팬츠. 운동부터 일상까지.", 42000, null, Category.SPORTS, 50, false, true, "/images/products/sports-5.svg");
        saveProduct("러닝화 클라우드 시리즈", "초경량 러닝화. 쿠셔닝 극대화.", 89000, 120000, Category.SPORTS, 20, true, false, "/images/products/sports-6.svg");
        saveProduct("메쉬 탱크탑", "통기성 좋은 메쉬 소재. 여름 운동 필수.", 22000, null, Category.SPORTS, 70, false, true, "/images/products/sports-7.svg");
        saveProduct("기능성 반바지", "속건 스포츠 반바지. 가벼운 착용감.", 32000, null, Category.SPORTS, 55, false, true, "/images/products/sports-8.svg");

        // ── OUTLET 카테고리 (6) ──
        saveProduct("[OUTLET] 울 블렌드 코트", "지난 시즌 베스트셀러. 파격 할인!", 89000, 189000, Category.OUTLET, 10, true, false, "/images/products/outlet-1.svg");
        saveProduct("[OUTLET] 가죽 스니커즈", "프리미엄 가죽 스니커즈. 한정 수량.", 59000, 129000, Category.OUTLET, 8, true, false, "/images/products/outlet-2.svg");
        saveProduct("[OUTLET] 캐시미어 니트", "부드러운 캐시미어 혼방 니트.", 45000, 98000, Category.OUTLET, 15, true, false, "/images/products/outlet-3.svg");
        saveProduct("[OUTLET] 다운 패딩 베스트", "가벼운 경량 다운 베스트.", 49000, 110000, Category.OUTLET, 12, true, false, "/images/products/outlet-4.svg");
        saveProduct("[OUTLET] 레더 토트백", "프리미엄 소가죽 토트백. 파격 세일.", 69000, 159000, Category.OUTLET, 7, true, false, "/images/products/outlet-5.svg");
        saveProduct("[OUTLET] 캐시미어 머플러", "부드러운 캐시미어 머플러. 시즌 오프 특가.", 35000, 89000, Category.OUTLET, 10, true, false, "/images/products/outlet-6.svg");

        // ── KIDS 카테고리 (6) ──
        saveProduct("키즈 공룡 프린트 티", "아이들이 좋아하는 공룡 그래픽 티.", 19000, null, Category.KIDS, 70, false, true, "/images/products/kids-1.svg");
        saveProduct("키즈 멜빵 반바지", "귀여운 멜빵 디자인. 면 100%.", 25000, null, Category.KIDS, 50, false, true, "/images/products/kids-2.svg");
        saveProduct("키즈 레인부츠", "비 오는 날 필수! 컬러풀 장화.", 22000, 32000, Category.KIDS, 40, true, false, "/images/products/kids-3.svg");
        saveProduct("키즈 스쿨 백팩", "가벼운 초등학생용 책가방.", 35000, null, Category.KIDS, 30, false, true, "/images/products/kids-4.svg");
        saveProduct("키즈 곰돌이 후드", "귀여운 곰 캐릭터 후디. 아이들 인기템.", 28000, null, Category.KIDS, 40, false, true, "/images/products/kids-5.svg");
        saveProduct("키즈 파자마 세트", "면 100% 잠옷 세트. 편안한 수면.", 25000, null, Category.KIDS, 35, false, true, "/images/products/kids-6.svg");
    }

    private void saveProduct(String name, String description, int price, Integer originalPrice,
                             Category category, int stock, boolean isSale, boolean isNew, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setCategory(category);
        product.setStock(stock);
        product.setSale(isSale);
        product.setNew(isNew);
        product.setImageUrl(imageUrl);
        product.setViewCount((int)(Math.random() * 100));
        productRepository.save(product);
    }
}
