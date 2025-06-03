package com.leafree.backend.domain.event.repository;

import com.leafree.backend.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.deviceId = :deviceId AND e.occurredAt BETWEEN :start AND :end ORDER BY e.occurredAt DESC")
    List<Event> findByDeviceIdAndDateRange(@Param("deviceId") Long deviceId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);


}
