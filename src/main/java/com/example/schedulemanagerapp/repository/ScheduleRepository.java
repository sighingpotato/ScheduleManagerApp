package com.example.schedulemanagerapp.repository;

import com.example.schedulemanagerapp.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
