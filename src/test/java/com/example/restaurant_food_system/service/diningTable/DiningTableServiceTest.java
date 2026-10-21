package com.example.restaurant_food_system.service.diningTable;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.request.UpdateTablePositionRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.entity.DiningTable;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.DiningTableMapper;
import com.example.restaurant_food_system.repository.DiningTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiningTableServiceTest {

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private DiningTableMapper diningTableMapper;

    @InjectMocks
    private DiningTableServiceImpl diningTableService;

    private DiningTableRequest buildRequest(String name, Integer capacity) {
        DiningTableRequest request = new DiningTableRequest();
        request.setName(name);
        request.setCapacity(capacity);
        return request;
    }

    private UpdateTablePositionRequest buildPositionRequest(String previousTableId, String nextTableId) {
        UpdateTablePositionRequest request = new UpdateTablePositionRequest();
        request.setPreviousTableId(previousTableId);
        request.setNextTableId(nextTableId);
        return request;
    }

    @Test
    void createDiningTable_WhenFirstTable_ShouldUseDefaultPosition() {
        DiningTableRequest request = buildRequest("Bàn 1", 4);
        DiningTable diningTable = DiningTable.builder().name("Bàn 1").capacity(4).build();

        when(diningTableRepository.existsDiningTableByName("Bàn 1")).thenReturn(false);
        when(diningTableRepository.findMaxPosition()).thenReturn(null);
        when(diningTableMapper.mapToEntity(request)).thenReturn(diningTable);

        diningTableService.createDiningTable(request);

        assertEquals(1000L, diningTable.getPosition());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void createDiningTable_WhenMaxPositionExists_ShouldAppendThousand() {
        DiningTableRequest request = buildRequest("Bàn 4", 6);
        DiningTable diningTable = DiningTable.builder().name("Bàn 4").capacity(6).build();

        when(diningTableRepository.existsDiningTableByName("Bàn 4")).thenReturn(false);
        when(diningTableRepository.findMaxPosition()).thenReturn(3000L);
        when(diningTableMapper.mapToEntity(request)).thenReturn(diningTable);

        diningTableService.createDiningTable(request);

        assertEquals(4000L, diningTable.getPosition());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void createDiningTable_WhenNameExists_ShouldThrowBadRequestException() {
        DiningTableRequest request = buildRequest("Bàn 1", 4);

        when(diningTableRepository.existsDiningTableByName("Bàn 1")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> diningTableService.createDiningTable(request));

        assertEquals("Tên bàn đã tồn tại", exception.getMessage());
        verify(diningTableMapper, never()).mapToEntity(any());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void updateDiningTable_WhenFound_ShouldUpdateNameAndCapacity() {
        DiningTableRequest request = buildRequest("Bàn VIP", 10);
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t1").name("Bàn 1").capacity(4).position(1000L).build();

        when(diningTableRepository.findById("t1")).thenReturn(Optional.of(diningTable));

        diningTableService.updateDiningTable("t1", request);

        assertEquals("Bàn VIP", diningTable.getName());
        assertEquals(10, diningTable.getCapacity());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void updateDiningTable_WhenNotFound_ShouldThrowResourceNotFoundException() {
        DiningTableRequest request = buildRequest("Bàn VIP", 10);

        when(diningTableRepository.findById("t1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> diningTableService.updateDiningTable("t1", request));

        assertEquals("Không tìm thấy bàn ăn", exception.getMessage());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void disableDiningTable_WhenActive_ShouldDisableAndReturnFalse() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t1").name("Bàn 1").status(true).build();

        when(diningTableRepository.findById("t1")).thenReturn(Optional.of(diningTable));

        boolean result = diningTableService.disableDiningTable("t1");

        assertFalse(result);
        assertFalse(diningTable.getStatus());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void disableDiningTable_WhenInactive_ShouldEnableAndReturnTrue() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t1").name("Bàn 1").status(false).build();

        when(diningTableRepository.findById("t1")).thenReturn(Optional.of(diningTable));

        boolean result = diningTableService.disableDiningTable("t1");

        assertTrue(result);
        assertTrue(diningTable.getStatus());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void disableDiningTable_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(diningTableRepository.findById("t1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> diningTableService.disableDiningTable("t1"));

        assertEquals("Không tìm thấy bàn ăn", exception.getMessage());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void getAllTables_ShouldReturnMappedPage() {
        DiningTable table1 = DiningTable.builder().diningTableId("t1").name("Bàn 1").build();
        DiningTable table2 = DiningTable.builder().diningTableId("t2").name("Bàn 2").build();
        Page<DiningTable> page = new PageImpl<>(List.of(table1, table2));

        DiningTableResponse response1 = DiningTableResponse.builder().diningTableId("t1").name("Bàn 1").build();
        DiningTableResponse response2 = DiningTableResponse.builder().diningTableId("t2").name("Bàn 2").build();

        when(diningTableRepository.findAllDiningTables(any(), any(), any(Pageable.class))).thenReturn(page);
        when(diningTableMapper.mapToResponse(table1)).thenReturn(response1);
        when(diningTableMapper.mapToResponse(table2)).thenReturn(response2);

        Page<DiningTableResponse> result = diningTableService.getAllTables(0, 10, null, null);

        assertEquals(2, result.getTotalElements());
        assertEquals("Bàn 1", result.getContent().get(0).getName());
        assertEquals("Bàn 2", result.getContent().get(1).getName());
    }

    @Test
    void deleteDiningTable_WhenFound_ShouldSoftDelete() {
        DiningTable diningTable = DiningTable.builder().diningTableId("t1").name("Bàn 1").build();

        when(diningTableRepository.findById("t1")).thenReturn(Optional.of(diningTable));

        diningTableService.deleteDiningTable("t1");

        assertTrue(diningTable.getDeleted());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void deleteDiningTable_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(diningTableRepository.findById("t1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> diningTableService.deleteDiningTable("t1"));

        assertEquals("Không tìm thấy bàn ăn", exception.getMessage());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void getDiningTableDetail_WhenFound_ShouldReturnResponse() {
        DiningTable diningTable = DiningTable.builder().diningTableId("t1").name("Bàn 1").build();
        DiningTableResponse response = DiningTableResponse.builder().diningTableId("t1").name("Bàn 1").build();

        when(diningTableRepository.findById("t1")).thenReturn(Optional.of(diningTable));
        when(diningTableMapper.mapToResponse(diningTable)).thenReturn(response);

        DiningTableResponse result = diningTableService.getDiningTableDetail("t1");

        assertEquals("t1", result.getDiningTableId());
        assertEquals("Bàn 1", result.getName());
    }

    @Test
    void getDiningTableDetail_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(diningTableRepository.findById("t1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> diningTableService.getDiningTableDetail("t1"));

        assertEquals("Không tìm thấy bàn ăn", exception.getMessage());
    }

    @Test
    void reIndexAllTables_ShouldReassignPositionsByThousand() {
        DiningTable table1 = DiningTable.builder().diningTableId("t1").name("Bàn 1").position(500L).build();
        DiningTable table2 = DiningTable.builder().diningTableId("t2").name("Bàn 2").position(900L).build();
        DiningTable table3 = DiningTable.builder().diningTableId("t3").name("Bàn 3").position(2500L).build();

        when(diningTableRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(table1, table2, table3));

        diningTableService.reIndexAllTables();

        assertEquals(1000L, table1.getPosition());
        assertEquals(2000L, table2.getPosition());
        assertEquals(3000L, table3.getPosition());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void updateDiningTablePosition_WhenBetweenTwoTables_ShouldSetMiddlePosition() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t2").name("Bàn 2").position(2000L).build();
        List<DiningTable> tables = List.of(diningTable);
        UpdateTablePositionRequest request = buildPositionRequest("t1", "t3");

        when(diningTableRepository.findById("t2")).thenReturn(Optional.of(diningTable));
        when(diningTableRepository.findDiningTablePositionById("t1")).thenReturn(1000L);
        when(diningTableRepository.findDiningTablePositionById("t3")).thenReturn(5000L);
        when(diningTableRepository.findAllByOrderByPositionAsc()).thenReturn(tables);
        when(diningTableMapper.mapToResponse(diningTable))
                .thenReturn(DiningTableResponse.builder().diningTableId("t2").build());

        List<DiningTableResponse> result = diningTableService.updateDiningTablePosition("t2", request);

        assertEquals(3000L, diningTable.getPosition());
        assertEquals(1, result.size());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void updateDiningTablePosition_WhenMovingToFirst_ShouldSetPositionBeforeNext() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t2").name("Bàn 2").position(2000L).build();
        UpdateTablePositionRequest request = buildPositionRequest(null, "t1");

        when(diningTableRepository.findById("t2")).thenReturn(Optional.of(diningTable));
        when(diningTableRepository.findDiningTablePositionById("t1")).thenReturn(5000L);
        when(diningTableRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(diningTable));
        when(diningTableMapper.mapToResponse(diningTable))
                .thenReturn(DiningTableResponse.builder().diningTableId("t2").build());

        diningTableService.updateDiningTablePosition("t2", request);

        assertEquals(4000L, diningTable.getPosition());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void updateDiningTablePosition_WhenMovingToLast_ShouldSetPositionAfterPrevious() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t2").name("Bàn 2").position(2000L).build();
        UpdateTablePositionRequest request = buildPositionRequest("t1", null);

        when(diningTableRepository.findById("t2")).thenReturn(Optional.of(diningTable));
        when(diningTableRepository.findDiningTablePositionById("t1")).thenReturn(5000L);
        when(diningTableRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(diningTable));
        when(diningTableMapper.mapToResponse(diningTable))
                .thenReturn(DiningTableResponse.builder().diningTableId("t2").build());

        diningTableService.updateDiningTablePosition("t2", request);

        assertEquals(6000L, diningTable.getPosition());
        verify(diningTableRepository).save(diningTable);
    }

    @Test
    void updateDiningTablePosition_WhenPositionsInvalid_ShouldThrowBadRequestException() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t2").name("Bàn 2").position(2000L).build();
        UpdateTablePositionRequest request = buildPositionRequest("t1", "t3");

        when(diningTableRepository.findById("t2")).thenReturn(Optional.of(diningTable));
        when(diningTableRepository.findDiningTablePositionById("t1")).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> diningTableService.updateDiningTablePosition("t2", request));

        assertEquals("Vị trí bàn ăn không hợp lệ", exception.getMessage());
        verify(diningTableRepository, never()).save(any());
    }

    @Test
    void updateDiningTablePosition_WhenTableNotFound_ShouldThrowResourceNotFoundException() {
        UpdateTablePositionRequest request = buildPositionRequest("t1", "t3");

        when(diningTableRepository.findById("t2")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> diningTableService.updateDiningTablePosition("t2", request));

        assertEquals("Không tìm thấy bàn ăn", exception.getMessage());
    }

    @Test
    void updateDiningTablePosition_WhenGapTooSmall_ShouldReIndexAllTables() {
        DiningTable diningTable = DiningTable.builder()
                .diningTableId("t2").name("Bàn 2").position(1000L).build();
        UpdateTablePositionRequest request = buildPositionRequest("t1", "t3");

        when(diningTableRepository.findById("t2")).thenReturn(Optional.of(diningTable));
        when(diningTableRepository.findDiningTablePositionById("t1")).thenReturn(1000L);
        when(diningTableRepository.findDiningTablePositionById("t3")).thenReturn(1001L);
        when(diningTableRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(diningTable));
        when(diningTableMapper.mapToResponse(diningTable))
                .thenReturn(DiningTableResponse.builder().diningTableId("t2").build());

        diningTableService.updateDiningTablePosition("t2", request);

        verify(diningTableRepository, times(2)).findAllByOrderByPositionAsc();
        verify(diningTableRepository).save(diningTable);
    }
}
