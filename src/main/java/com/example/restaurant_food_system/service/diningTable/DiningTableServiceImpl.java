package com.example.restaurant_food_system.service.diningTable;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.request.UpdateTablePositionRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.dto.response.QrCodeResponse;
import com.example.restaurant_food_system.entity.DiningTable;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ConflictException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.DiningTableMapper;
import com.example.restaurant_food_system.repository.DiningTableRepository;
import com.example.restaurant_food_system.repository.DiningTableSessionRepository;
import com.example.restaurant_food_system.service.qr.QrCodeService;
import com.example.restaurant_food_system.utils.DiningTableSessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {
    private final DiningTableRepository diningTableRepository;
    private final DiningTableMapper diningTableMapper;
    private final QrCodeService qrCodeService;
    private final DiningTableSessionRepository diningTableSessionRepository;


    @Override
    @Transactional
    public void createDiningTable(DiningTableRequest diningTableRequest) {
        if (diningTableRepository.existsDiningTableByName(diningTableRequest.getName())) {
            throw new BadRequestException("Tên bàn đã tồn tại");
        }

        // Lấy vị trí position cuối cùng
        Long maxPosition = diningTableRepository.findMaxPosition();
        maxPosition = maxPosition == null ? 1000L : maxPosition + 1000L;

        DiningTable diningTable = diningTableMapper.mapToEntity(diningTableRequest);
        diningTable.setPosition(maxPosition);
        diningTable.setQrCode(generateUniqueQrCode());
        diningTable.setQrGeneratedAt(Instant.now());
        diningTableRepository.save(diningTable);
    }

    @Override
    public void updateDiningTable(String diningTableId, DiningTableRequest request) {
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy bàn ăn"));

        diningTable.setName(request.getName());
        diningTable.setCapacity(request.getCapacity());
        diningTableRepository.save(diningTable);

    }

    @Override
    public boolean disableDiningTable(String diningTableId) {
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));

        boolean currentStatus = diningTable.getStatus();

        diningTable.setStatus(!currentStatus);
        diningTableRepository.save(diningTable);

        return !currentStatus;
    }

    @Override
    public Page<DiningTableResponse> getAllTables(int page, int size, String search, Boolean status) {
        // Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by("position").ascending());

        // Query data
        Page<DiningTable> diningTables = diningTableRepository.findAllDiningTables(search, status, pageable);

        // Mapping data trả về
        return diningTables
                .map(diningTableMapper::mapToResponse);
    }

    @Override
    public void deleteDiningTable(String diningTableId) {
        // Kiểm tra bàn ăn có tồn tại
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));

        // Xóa mềm
        diningTable.setDeleted(true);
        diningTableRepository.save(diningTable);
    }

    @Override
    public DiningTableResponse getDiningTableDetail(String diningTableId) {
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));

        return diningTableMapper.mapToResponse(diningTable);
    }

    @Override
    public void reIndexAllTables() {
        // Lấy danh sách bàn ăn, tăng dần theo position
        List<DiningTable> diningTables = diningTableRepository.findAllByOrderByPositionAsc();

        Long position = 1000L;
        Long previousPosition = 0L;
        for (DiningTable diningTable: diningTables) {
            diningTable.setPosition(previousPosition + position);
            previousPosition = diningTable.getPosition();
        }
    }

    @Override
    public List<DiningTableResponse> updateDiningTablePosition(String diningTableId, UpdateTablePositionRequest updateTablePositionRequest) {
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));

        // TH1. Kéo vào vị trí giữa
        if (updateTablePositionRequest.getPreviousTableId() != null && updateTablePositionRequest.getNextTableId() != null) {
            // 1.1 Lấy vị trí position của previous, next table
            Long previousPosition = diningTableRepository.findDiningTablePositionById(updateTablePositionRequest.getPreviousTableId());
            Long nextPosition = diningTableRepository.findDiningTablePositionById(updateTablePositionRequest.getNextTableId());
            if (previousPosition == null || nextPosition == null) {
                throw new BadRequestException("Vị trí bàn ăn không hợp lệ");
            }

            // 2.2 Cập nhật vị trí mới
            Long newPosition = (previousPosition + nextPosition) / 2L;
            diningTable.setPosition(newPosition);

            if (nextPosition - newPosition <= 1) {
                reIndexAllTables();
            }
        }

        // TH2. Kéo vào vị trí đầu tiên
        if (updateTablePositionRequest.getPreviousTableId() == null && updateTablePositionRequest.getNextTableId() != null) {
            // 2.1 Lấy vị trí position của next table
            Long nextPosition = diningTableRepository.findDiningTablePositionById(updateTablePositionRequest.getNextTableId());
            if (nextPosition == null) {
                throw new BadRequestException("Vị trí bàn ăn không hợp lệ");
            }

            // 2.2 Cập nhật vị trí mới
            diningTable.setPosition(nextPosition - 1000L);

            if (nextPosition - diningTable.getPosition() <= 1) {
                reIndexAllTables();
            }
        }

        // TH3. Kéo vào vị trí cuối cùng
        if (updateTablePositionRequest.getNextTableId() == null && updateTablePositionRequest.getPreviousTableId() != null) {
            // 3.1 Lấy vị trí position của previous table
            Long previousPosition = diningTableRepository.findDiningTablePositionById(updateTablePositionRequest.getPreviousTableId());
            if (previousPosition == null) {
                throw new BadRequestException("Vị trí bàn ăn không hợp lệ");
            }

            // 3.2 Cập nhật vị trí mới
            diningTable.setPosition(previousPosition + 1000L);

            if (diningTable.getPosition() - previousPosition <= 1) {
                reIndexAllTables();
            }
        }

        diningTableRepository.save(diningTable);

        // Trả về danh sách bàn ăn sau khi đã sắp xếp lại
        return diningTableRepository.findAllByOrderByPositionAsc().stream()
                .map(diningTableMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public QrCodeResponse ensureQrCode(String diningTableId) {
        DiningTable diningTable = getDiningTableForQrUpdate(diningTableId);
        validateDiningTableNotDeleted(diningTable);

        if (diningTable.getQrCode() == null || diningTable.getQrCode().isBlank()) {
            diningTable.setQrCode(generateUniqueQrCode());
            diningTable.setQrGeneratedAt(Instant.now());
            diningTableRepository.save(diningTable);
        }

        return mapToQrCodeResponse(diningTable);
    }

    @Override
    @Transactional
    public QrCodeResponse regenerateQrCode(String diningTableId, boolean force) {
        DiningTable diningTable = getDiningTableForQrUpdate(diningTableId);
        validateDiningTableNotDeleted(diningTable);

        boolean hasOpenSession = diningTableSessionRepository
                .existsByDiningTable_DiningTableIdAndStatus(
                        diningTableId,
                        DiningTableSessionStatus.OPEN
                );

        if (hasOpenSession && !force) {
            throw new ConflictException("Bàn ăn đang có phiên mở, không thể tạo lại QR");
        }

        diningTable.setQrCode(generateUniqueQrCode());
        diningTable.setQrGeneratedAt(Instant.now());
        diningTableRepository.save(diningTable);

        return mapToQrCodeResponse(diningTable);
    }

    @Override
    public QrCodeResponse getQrCode(String diningTableId) {
        DiningTable diningTable = diningTableRepository.findById(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));
        validateDiningTableNotDeleted(diningTable);

        if (diningTable.getQrCode() == null || diningTable.getQrCode().isBlank()) {
            throw new ResourceNotFoundException("Bàn ăn chưa có QR");
        }

        return mapToQrCodeResponse(diningTable);
    }

    @Override
    public byte[] generateQrImage(String diningTableId) {
        QrCodeResponse qrCodeResponse = getQrCode(diningTableId);
        return qrCodeService.renderPng(qrCodeResponse.getQrUrl());
    }

    private DiningTable getDiningTableForQrUpdate(String diningTableId) {
        return diningTableRepository.findByIdForUpdate(diningTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn ăn"));
    }

    private void validateDiningTableNotDeleted(DiningTable diningTable) {
        if (Boolean.TRUE.equals(diningTable.getDeleted())) {
            throw new BadRequestException("Bàn ăn đã bị xóa");
        }
    }

    private String generateUniqueQrCode() {
        for (int i = 0; i < 5; i++) {
            String qrCode = qrCodeService.generateCode();
            if (!diningTableRepository.existsByQrCode(qrCode)) {
                return qrCode;
            }
        }

        throw new IllegalStateException("Không thể tạo mã QR, vui lòng thử lại");
    }

    private QrCodeResponse mapToQrCodeResponse(DiningTable diningTable) {
        return QrCodeResponse.builder()
                .diningTableId(diningTable.getDiningTableId())
                .tableName(diningTable.getName())
                .qrCode(diningTable.getQrCode())
                .qrUrl(qrCodeService.buildQrUrl(diningTable.getQrCode()))
                .qrGeneratedAt(diningTable.getQrGeneratedAt())
                .build();
    }
}
