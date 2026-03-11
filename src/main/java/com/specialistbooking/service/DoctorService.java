package com.specialistbooking.service;

import com.specialistbooking.dto.request.DoctorRequest;
import com.specialistbooking.entity.Doctor;
import org.springframework.data.domain.Page;
import java.util.List;

public interface DoctorService {
    Doctor addDoctor(DoctorRequest request);
    Page<Doctor> getDoctors(int page, int size, String sortBy);
    Doctor getDoctorById(Long id);
    Doctor updateDoctor(Long id, DoctorRequest request);
    void deleteDoctor(Long id);
    Doctor addRegularPatient(Long doctorId, Long userId);
    List<Doctor> getDoctorsBySpecialty(String specialty);
    List<Doctor> getDoctorsByProvince(String province);
    List<Doctor> searchDoctors(String specialty, String province);
    List<Doctor> searchByName(String name);
}