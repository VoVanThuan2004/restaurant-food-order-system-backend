package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.DiningTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, String> {
    boolean existsDiningTableByName(String name);

    @Query("""
        select t
        from DiningTable t
        where (
                :search is null
                or t.name ilike concat('%', :search, '%')
        )
        and (:status is null or t.status = :status)
        and t.deleted = false
    """)
    Page<DiningTable> findAllDiningTables(@Param("search") String search, @Param("status") Boolean status, Pageable pageable);


    @Query("""
        select max(t.position)
        from DiningTable t
    """)
    Long findMaxPosition();

    List<DiningTable> findAllByOrderByPositionAsc();

    @Query("""
        select t.position
        from DiningTable t
        where t.diningTableId = :diningTableId
    """)
    Long findDiningTablePositionById(@Param("diningTableId") String previousTableId);
}
