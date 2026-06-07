package com.example.restaurant_food_system.service.orderItem;

import com.example.restaurant_food_system.dto.request.OrderItemRequest;
import com.example.restaurant_food_system.dto.request.OrderItemVariantRequest;
import com.example.restaurant_food_system.dto.response.OrderItemHistoryResponse;
import com.example.restaurant_food_system.entity.*;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.repository.*;
import com.example.restaurant_food_system.service.SocketService;
import com.example.restaurant_food_system.utils.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final DishVariantGroupRepository dishVariantGroupRepository;
    private final DishVariantOptionRepository dishVariantOptionRepository;
    private final OrderItemVariantRepository orderItemVariantRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;
    private final SocketService socketService;

    @Override
    @Transactional
    public void addOrderItem(OrderItemRequest orderItemRequest) {
        // 1. Kiểm tra đơn gọi món
        Order order = orderRepository.findById(orderItemRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món không tồn tại"));

        // 2. Kiểm tra món ăn
        Dish dish = dishRepository.findById(orderItemRequest.getDishId())
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        // 3. Kiểm tra trạng thái món ăn
        if (!dish.isStatus()) {
            throw new BadRequestException("Món ăn hiện đang ngừng phục vụ");
        }

        // =====================================================
        // 3. LOAD REQUIRED GROUPS
        // =====================================================
        List<DishVariantGroup> variantGroups = dishVariantGroupRepository.findAllByDish_DishIdAndDeletedFalse(dish.getDishId());

        Map<String, DishVariantGroup> groupMaps = variantGroups.stream()
                .collect(Collectors.toMap(
                        DishVariantGroup::getGroupId,
                        Function.identity()
                ));

        // =====================================================
        // 4. LOAD OPTIONS
        // =====================================================
        List<String> optionIds = orderItemRequest.getVariants()
                .stream()
                .map(OrderItemVariantRequest::getOptionId)
                .toList();

        List<DishVariantOption> options = dishVariantOptionRepository.findAllById(optionIds);

        if (optionIds.size() != options.size()) {
            throw new ResourceNotFoundException("Lựa chọn biến thể không tồn tại");
        }


        // =====================================================
        // 5. VALIDATE REQUIRED GROUP
        // =====================================================
        Set<String> selectedGroupIds = new HashSet<>();
        for (DishVariantOption option : options) {
            DishVariantGroup group = option.getDishVariantGroup();
            if (!group.getDish().getDishId().equals(dish.getDishId())) {
                throw new BadRequestException("Biến thể không thuộc món ăn");
            }
            selectedGroupIds.add(group.getGroupId());
        }

        for (DishVariantGroup group: variantGroups) {
            if (group.isRequired() && !selectedGroupIds.contains(group.getGroupId())) {
                throw new BadRequestException("Vui lòng chọn: " + group.getGroupName());
            }
        }


        // =====================================================
        // 6. VALIDATE MULTIPLE
        // =====================================================
        Map<String, Integer> countGroupSelections =
                new HashMap<>();

        for (DishVariantOption option : options) {

            String groupId =
                    option.getDishVariantGroup()
                            .getGroupId();

            countGroupSelections.put(
                    groupId,
                    countGroupSelections.getOrDefault(
                            groupId,
                            0
                    ) + 1
            );
        }

        for (Map.Entry<String, Integer> entry :
                countGroupSelections.entrySet()) {

            DishVariantGroup group =
                    groupMaps.get(entry.getKey());

            if (!group.isMultiple()
                    && entry.getValue() > 1) {

                throw new BadRequestException(
                        "Nhóm " + group.getGroupName()
                                + " chỉ được chọn 1 lựa chọn");
            }
        }

        // =====================================================
        // 7. TÍNH GIÁ
        // =====================================================
        Double variantPrice = options.stream()
                .mapToDouble(DishVariantOption::getPriceAdjustment)
                .sum();

        Double totalPrice = (dish.getBasePrice() + variantPrice) * orderItemRequest.getQuantity();

        // =====================================================
        // 8. TẠO ORDER ITEM
        // =====================================================
        OrderItem orderItem = OrderItem.builder()
                .basePrice(dish.getBasePrice())
                .quantity(orderItemRequest.getQuantity())
                .dishImage(dish.getImage())
                .notes(orderItemRequest.getNotes())
                .currentStatus(OrderItemStatus.NEW)
                .dish(dish)
                .order(order)
                .totalPrice(totalPrice)
                .build();

        OrderItem finalOrderItem = orderItemRepository.save(orderItem);

        // =====================================================
        // 9. TẠO ORDER ITEM VARIANTS
        // =====================================================
        List<OrderItemVariant> orderItemVariants = options.stream()
                .map(option -> OrderItemVariant.builder()
                        .orderItem(finalOrderItem)
                        .dishVariantGroup(option.getDishVariantGroup())
                        .dishVariantOption(option)
                        .priceAdjustment(option.getPriceAdjustment())
                        .build())
                .toList();
        orderItemVariantRepository.saveAll(orderItemVariants);

        // =====================================================
        // 10. TẠO HISTORY
        // =====================================================
        OrderItemHistory history =
                OrderItemHistory.builder()
                        .orderItem(orderItem)
                        .status(OrderItemStatus.NEW)
                        .build();

        // =====================================================
        // 11. UPDATE TOTAL ORDER
        // =====================================================
        Double orderTotal =
                orderItemRepository.sumTotalPriceByOrderId(
                        order.getOrderId()
                );

        order.setTotalPrice(orderTotal);
//        order.setLastItemAddedTime(Instant.now());
        orderRepository.save(order);

        orderItemHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void deleteOrderItem(String orderItemId) {
        // 1. Kiểm tra order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn trong đơn gọi món không tồn tại"));

        Double dishTotalPrice = orderItem.getTotalPrice();

        // 2. Cập nhật lại tổng tiền đơn gọi món
        Order order = orderItem.getOrder();
        order.setTotalPrice(order.getTotalPrice() - dishTotalPrice);
        orderRepository.save(order);

        // 3. Xóa order item
        orderItemRepository.delete(orderItem);
    }

    @Override
    public void updateOrderItemQuantity(String orderItemId, Integer newQuantity) {
        // 1. Kiểm tra order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn trong đơn gọi món không tồn tại"));

        if (!orderItem.getCurrentStatus().equals(OrderItemStatus.NEW)) {
            throw new BadRequestException("Món ăn đã được gửi xuống bếp, không thể chỉnh sửa");
        }

        // 2. Kiểm tra số lượng
        if (newQuantity == null) {
            throw new BadRequestException("Vui lòng chọn số lượng");
        }

        if (newQuantity >= 50) {
            throw new BadRequestException("Số lượng không được vượt quá 50");
        }

        // 3. Lấy tổng tiền biến thể
        Double totalPriceAdjustment = orderItem.getOrderItemVariants().stream()
                .mapToDouble(OrderItemVariant::getPriceAdjustment)
                .sum();

        Double newTotalPrice = (orderItem.getBasePrice() + totalPriceAdjustment) * newQuantity;

        // 4. Cập nhật order item
        orderItem.setTotalPrice(newTotalPrice);
        orderItem.setQuantity(newQuantity);
        orderItemRepository.save(orderItem);

        // 5. Cập nhật order
        Order order = orderItem.getOrder();
        order.setTotalPrice(order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum()
        );
        orderRepository.save(order);
    }

    @Override
    public void updateOrderItemNotes(String orderItemId, String notes) {
        // 1. Kiểm tra order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn trong đơn gọi món không tồn tại"));

        if (!orderItem.getCurrentStatus().equals(OrderItemStatus.NEW)) {
            throw new BadRequestException("Món ăn đã được gửi xuống bếp, không thể chỉnh sửa");
        }

        // 2. Kiểm tra ghi chú
        if (notes == null) {
            throw new BadRequestException("Vui lòng nhập ghi chú món ăn");
        }

        orderItem.setNotes(notes);
        orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public void confirmStatusItem(String orderItemId, String status) {
        // Kiểm tra status hợp lệ
        if (!OrderItemStatus.isValid(status)) {
            throw new BadRequestException("Trạng thái món ăn không hợp lệ");
        }

        // 1. Kiểm tra order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn trong đơn gọi món không tồn tại"));

        // 2. Kiểm tra trạng thái
        // 2.1 Nếu hủy món
        if (status.equals(OrderItemStatus.CANCELLED)) {
            orderItem.setCurrentStatus(OrderItemStatus.CANCELLED);
            orderItemRepository.save(orderItem);

            OrderItemHistory orderItemHistory = OrderItemHistory.builder()
                    .orderItem(orderItem)
                    .status(OrderItemStatus.CANCELLED)
                    .build();
            orderItemHistoryRepository.save(orderItemHistory);

            // Cập nhật lại tổng tiền đơn gọi món
            Order order = orderItem.getOrder();
            order.setTotalPrice(order.getTotalPrice() - orderItem.getTotalPrice());
            orderRepository.save(order);
        }

        else {
            orderItem.setCurrentStatus(status);
            orderItemRepository.save(orderItem);

            OrderItemHistory orderItemHistory = OrderItemHistory.builder()
                    .orderItem(orderItem)
                    .status(status)
                    .build();
            orderItemHistoryRepository.save(orderItemHistory);
        }

        // Gọi web socket cập nhật trạng thái món ăn
        socketService.sendConfirmStatusItem(orderItem.getOrder().getOrderId(), orderItemId, status);
    }

    @Override
    @Transactional
    public List<OrderItemHistoryResponse> getOrderItemHistory(String orderItemId) {
        // 1. Kiểm tra order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn trong đơn gọi món không tồn tại"));

        // 2. Mapping data trả về
        return orderItem.getOrderItemHistories().stream()
                .map(orderItemHistory -> OrderItemHistoryResponse.builder()
                        .status(orderItemHistory.getStatus())
                        .createdAt(orderItemHistory.getCreatedAt())
                        .build())
                .toList();
    }
}
