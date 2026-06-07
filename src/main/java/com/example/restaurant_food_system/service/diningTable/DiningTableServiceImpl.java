package com.example.restaurant_food_system.service.diningTable;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.request.UpdateTablePositionRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.entity.DiningTable;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.DiningTableMapper;
import com.example.restaurant_food_system.repository.DiningTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {
    private final DiningTableRepository diningTableRepository;
    private final DiningTableMapper diningTableMapper;


    @Override
    public void createDiningTable(DiningTableRequest diningTableRequest) {
        if (diningTableRepository.existsDiningTableByName(diningTableRequest.getName())) {
            throw new BadRequestException("Tên bàn đã tồn tại");
        }

        // Lấy vị trí position cuối cùng
        Long maxPosition = diningTableRepository.findMaxPosition();
        maxPosition = maxPosition == null ? 1000L : maxPosition + 1000L;

        DiningTable diningTable = diningTableMapper.mapToEntity(diningTableRequest);
        diningTable.setPosition(maxPosition);
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
}
