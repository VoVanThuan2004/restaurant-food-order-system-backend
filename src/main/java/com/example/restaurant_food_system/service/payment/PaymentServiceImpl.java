package com.example.restaurant_food_system.service.payment;

import com.example.restaurant_food_system.dto.request.PaymentRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.entity.DiningTable;
import com.example.restaurant_food_system.entity.Order;
import com.example.restaurant_food_system.entity.Payment;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.repository.DiningTableRepository;
import com.example.restaurant_food_system.repository.OrderRepository;
import com.example.restaurant_food_system.repository.PaymentRepository;
import com.example.restaurant_food_system.service.SocketService;
import com.example.restaurant_food_system.utils.OrderItemStatus;
import com.example.restaurant_food_system.utils.PaymentMethod;
import com.example.restaurant_food_system.utils.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DiningTableRepository diningTableRepository;
    private final SocketService socketService;

    @Override
    @Transactional
    public void payOrder(PaymentRequest paymentRequest) {
        // 1. Kiểm tra đơn gọi món
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món không tồn tại"));

        // 2. Kiểm tra trạng thái thanh toán
        if (order.getStatus()) {
            throw new BadRequestException("Đơn gọi món đã được thanh toán");
        }

        // 3. Kiểm tra phương thức thanh toán
        if (!PaymentMethod.isValidPaymentMethod(paymentRequest.getPaymentMethod())) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ");
        }

        // 4. Kiểm tra các món ăn đã được phục vụ hết chưa
        order.getOrderItems().forEach(orderItem -> {
            if (!orderItem.getCurrentStatus().equals(OrderItemStatus.SERVED) && !orderItem.getCurrentStatus().equals(OrderItemStatus.CANCELLED)) {
                throw new BadRequestException("Món ăn trong đơn gọi món chưa phục vụ, không thể thanh toán");
            }
        });

        // 5. Thanh toán bằng tiền mặt
        if (paymentRequest.getPaymentMethod().equals(PaymentMethod.CASH)) {
            // 5.1 Kiểm tra tiền nhận của khách
            if (paymentRequest.getAmountReceived() < order.getTotalPrice()) {
                throw new BadRequestException("Tiền nhận của khách bị thiếu");
            }

            // 5.2 Tạo payment
            Payment payment = Payment.builder()
                    .amountPaid(order.getTotalPrice())
                    .amountReceived(paymentRequest.getAmountReceived())
                    .changeAmount(paymentRequest.getAmountReceived() - order.getTotalPrice())
                    .paymentMethod(PaymentMethod.CASH)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .order(order)
                    .build();
            paymentRepository.save(payment);

            // 5.3 Cập nhật order
            order.setStatus(true);
            orderRepository.save(order);

            // 5.4 Cập nhật lại trạng thái bàn ăn
            DiningTable diningTable = order.getDiningTable();
            diningTable.setStatus(true);
            diningTableRepository.save(diningTable);

            // 5.5 Gửi socket cập nhật trạng thái bàn ăn
            socketService.sendDiningTableStatus(DiningTableResponse.builder()
                            .diningTableId(diningTable.getDiningTableId())
                            .status(true)
                    .build());
        }

        // 6. Thanh toán chuyển khoản
        else {
            // 6.1 Tạo payment pending
            Payment payment = Payment.builder()
                    .amountPaid(order.getTotalPrice())
                    .amountReceived(paymentRequest.getAmountReceived())
                    .changeAmount(paymentRequest.getAmountReceived() - order.getTotalPrice())
                    .paymentMethod(PaymentMethod.TRANSFER)
                    .paymentStatus(PaymentStatus.PENDING)
                    .order(order)
                    .build();
            paymentRepository.save(payment);
        }
    }
}
