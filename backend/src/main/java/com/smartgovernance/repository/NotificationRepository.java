package com.smartgovernance.repository;

import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCitizenOrderByCreatedAtDesc(Citizen citizen);

    List<Notification> findByCitizenAndIsReadOrderByCreatedAtDesc(Citizen citizen, boolean isRead);

    long countByCitizenAndIsRead(Citizen citizen, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.citizen = :citizen AND n.isRead = false")
    int markAllAsReadForCitizen(@Param("citizen") Citizen citizen);
}
