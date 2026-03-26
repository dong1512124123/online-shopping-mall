package com.shop.mall.service;

import com.shop.mall.entity.*;
import com.shop.mall.enums.OrderStatus;
import com.shop.mall.enums.PaymentMethod;
import com.shop.mall.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrderFromCart(String username, PaymentMethod paymentMethod,
                                     String recipientName, String recipientPhone,
                                     String zipcode, String address,
                                     String addressDetail, String memo) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<CartItem> cartItems = cartItemRepository.findByMemberOrderByCreatedAtDesc(member);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setMember(member);
        order.setPaymentMethod(paymentMethod);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setZipcode(zipcode);
        order.setAddress(address);
        order.setAddressDetail(addressDetail);
        order.setMemo(memo);
        order.setStatus(OrderStatus.PAID);

        int totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "'" + product.getName() + "' 상품의 재고가 부족합니다. (재고: " + product.getStock() + "개)");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.addOrderItem(orderItem);

            totalPrice += orderItem.getTotalPrice();

            product.setStock(product.getStock() - cartItem.getQuantity());
        }

        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return order;
    }

    public List<Order> getMyOrders(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return orderRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    public Order getOrderDetail(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (!order.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        return order;
    }

    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (!order.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("취소할 수 없는 주문 상태입니다.");
        }

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    // --- Admin methods ---

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        order.setStatus(status);
    }

    public long getOrderCount() {
        return orderRepository.count();
    }

    public long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public List<Map<String, Object>> getBestSellers(int limit) {
        List<Object[]> results = orderItemRepository.findBestSellingProducts();
        List<Map<String, Object>> bestSellers = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", row[0]);
            item.put("productName", row[1]);
            item.put("totalQuantity", row[2]);
            item.put("totalRevenue", row[3]);
            bestSellers.add(item);
            count++;
        }
        return bestSellers;
    }

    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + dateStr + random;
    }
}
