package com.aavukat.patientservice.controller;

import com.aavukat.patientservice.dto.PatientRequestDTO;
import com.aavukat.patientservice.dto.PatientResponseDTO;
import com.aavukat.patientservice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);
        return ResponseEntity.ok(patientResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable String id, @Valid @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(UUID.fromString(id), patientRequestDTO);
        return ResponseEntity.ok(patientResponseDTO);
    }
}

