package com.example.restaurant_food_system.service.order;

import com.example.restaurant_food_system.dto.request.OrderRequest;
import com.example.restaurant_food_system.dto.response.*;
import com.example.restaurant_food_system.entity.DiningTable;
import com.example.restaurant_food_system.entity.Order;
import com.example.restaurant_food_system.entity.OrderItem;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.DiningTableMapper;
import com.example.restaurant_food_system.mapper.OrderMapper;
import com.example.restaurant_food_system.repository.DiningTableRepository;
import com.example.restaurant_food_system.repository.OrderItemRepository;
import com.example.restaurant_food_system.repository.OrderRepository;
import com.example.restaurant_food_system.repository.UserRepository;
import com.example.restaurant_food_system.service.SocketService;
import com.example.restaurant_food_system.utils.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DiningTableRepository diningTableRepository;
    private final SocketService socketService;
    private final DiningTableMapper diningTableMapper;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;


    @Override
    @Transactional
    public String createOrder(OrderRequest orderRequest) {
        // 1. Kiểm tra thông tin người tạo
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Người tạo không hợp lệ"));

        // 2. Kiểm tra bàn ăn có hợp lệ
        DiningTable diningTable = diningTableRepository.findById(orderRequest.getDiningTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Bàn ăn không tồn tại"));

        // 3. Kiểm tra trạng thái bàn ăn phải còn trống
        if (!diningTable.getStatus()) {
            throw new BadRequestException("Bàn ăn hiện đã có khách đặt");
        }

        // 4. Tạo order
        Order order = Order.builder()
                .user(user)
                .diningTable(diningTable)
                .build();

        order = orderRepository.save(order);

        // 5. Cập nhật lại trạng thái bàn ăn
        diningTable.setStatus(false);
        diningTableRepository.save(diningTable);

        // 5. Gửi socket cập nhật trạng thái bàn ăn
        socketService.sendDiningTableStatus(diningTableMapper.mapToResponse(diningTable));

        return order.getOrderId();
    }

    @Override
    public String checkOrder(String diningTableId, String userId) {
        // 1. Kiểm tra bàn ăn
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn ăn không tồn tại"));

        // 2. Kiểm tra người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (diningTable.getStatus()) {
            throw new BadRequestException("Bàn ăn đang trống");
        }

        Order order = orderRepository.findByDiningTableIdAndUserId(diningTableId, userId);

        if (order == null) {
            throw new BadRequestException("Bàn ăn được phục vụ bởi nhân viên khác");
        }
        return order.getOrderId();
    }

    @Override
    public OrderResponse getOrderDetail(String orderId) {
        // 1. Kiểm tra order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món không tồn tại"));

        // 2. Mapping data trả về
        return orderMapper.mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByChef(int page, int size) {
        // 1. Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastItemAddedTime").descending());

        // 2. Query data lấy danh sách orders chưa thanh toán
        Page<Order> orders = orderRepository.findOrdersByStatusFalse(pageable);

        // 3. Mapping data trả về
        return orders.map(order -> OrderResponse.builder()
                .orderId(order.getOrderId())
                .diningTableName(order.getDiningTable().getName())
                .staffName(order.getUser().getFullName())
                .totalPrice(order.getTotalPrice())
                .orderItems(order.getOrderItems().stream()
                        .filter(orderItem -> !Objects.equals(orderItem.getCurrentStatus(), OrderItemStatus.NEW))
                        .map(orderItem -> OrderItemResponse.builder()
                                .orderItemId(orderItem.getOrderItemId())
                                .dishName(orderItem.getDish().getName())
                                .dishImage(orderItem.getDishImage())
                                .basePrice(orderItem.getBasePrice())
                                .quantity(orderItem.getQuantity())
                                .notes(orderItem.getNotes())
                                .currentStatus(orderItem.getCurrentStatus())
                                .totalPrice(orderItem.getTotalPrice())
                                .orderItemVariants(orderItem.getOrderItemVariants().stream()
                                        .map(orderItemVariant -> OrderItemVariantResponse.builder()
                                                .groupId(orderItemVariant.getDishVariantGroup().getGroupId())
                                                .groupName(orderItemVariant.getDishVariantGroup().getGroupName())
                                                .optionId(orderItemVariant.getDishVariantOption().getOptionId())
                                                .optionName(orderItemVariant.getDishVariantOption().getOptionName())
                                                .priceAdjustment(orderItemVariant.getPriceAdjustment())
                                                .build())
                                        .toList()
                                )
                                .build())
                        .toList())
                .build());
    }

    @Override
    @Transactional
    public void placeOrder(String orderId) {
        // 1. Kiểm tra đơn gọi món
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món không tồn tại"));

        // 2. Kiểm tra trạng thái đơn gọi món
        if (order.getStatus()) {
            throw new BadRequestException("Đơn gọi món đã được thanh toán");
        }

        // 3. Lấy danh sách orderItems
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdAndCurrentStatus(orderId, OrderItemStatus.NEW);

        // 4. Cập nhật trạng thái mới sau khi gọi món
        orderItems.forEach(orderItem -> {
            orderItem.setCurrentStatus(OrderItemStatus.PENDING);
        });

        orderItemRepository.saveAll(orderItems);

        // 5. Cập nhật lại order
        order.setLastItemAddedTime(Instant.now());
        orderRepository.save(order);

        // 6. Gửi qua socket
        socketService.sendPlaceOrder(orderId);
    }

    @Override
    public Page<OrderPaymentResponse> getOrdersByStaff(String staffId, int page, int size) {
        // 1. Kiểm tra nhân viên
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        // 1. Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size);

        // 2. Query data trả về
        Page<Order> orders = orderRepository.findOrdersByStaffAndPaid(staffId, pageable);

        // 3. Mapping data trả về
        return orders
                .map(order -> OrderPaymentResponse.builder()
                        .orderId(order.getOrderId())
                        .diningTableName(order.getDiningTable().getName())
                        .staffName(order.getUser().getFullName())
                        .totalPrice(order.getTotalPrice())
                        .amountReceived(order.getPayment().getAmountReceived())
                        .changeAmount(order.getPayment().getChangeAmount())
                        .paymentMethod(order.getPayment().getPaymentMethod())
                        .paidAt(order.getPayment().getPaidAt())
                        .build());
    }

    @Override
    public Integer getOrderTotalItems(String orderId) {
        // 1. Kiểm tra đơn gọi món
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món không tồn tại"));

        // 2. Lấy tổng số món ăn có trong đơn gọi món
        Integer totalItems = orderItemRepository.countAllByOrderId(orderId);
        return totalItems;
    }

    @Override
    public Page<OrderResponse> getOrdersByAdmin(
            int page, int size, LocalDate startDate, LocalDate endDate, String userId, Boolean status
    ) {
        // 1. Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size);

        Instant startInstant = null;
        Instant endInstant = null;
        if (startDate != null && endDate != null) {
            ZoneId zoneId = ZoneId.systemDefault();
            startInstant = startDate
                    .atStartOfDay(zoneId)
                    .toInstant();

            endInstant = endDate
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
        }


        // 2. Query trả về data orders
        Page<Order> orders;
        if (startInstant != null && endInstant != null) {
            orders = orderRepository.findOrdersForAdminWithDateFilter(startInstant, endInstant, userId, status, pageable);
        } else {
            orders = orderRepository.findOrdersForAdminNoDateFilter(userId, status, pageable);
        }

        // 3. Mapping data trả về
        return orders.map(order -> OrderResponse.builder()
                .orderId(order.getOrderId())
                .diningTableName(order.getDiningTable().getName())
                .staffName(order.getUser().getFullName())
                .totalPrice(order.getTotalPrice())
                .amountReceived(order.getAmountReceived())
                .changeAmount(order.getChangeAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPayment().getPaymentMethod())
                .paidAt(order.getPayment().getPaidAt())
                .build());
    }
}
