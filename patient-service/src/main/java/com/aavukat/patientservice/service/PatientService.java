package com.aavukat.patientservice.service;

import com.aavukat.patientservice.dto.PatientRequestDTO;
import com.aavukat.patientservice.dto.PatientResponseDTO;
import com.aavukat.patientservice.exception.EmailAlreadyExistsException;
import com.aavukat.patientservice.exception.PatientNotFoundException;
import com.aavukat.patientservice.grpc.BillingServiceGrpcClient;
import com.aavukat.patientservice.kafka.KafkaProducer;
import com.aavukat.patientservice.mapper.PatientMapper;
import com.aavukat.patientservice.model.Patient;
import com.aavukat.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email " + patientRequestDTO.getEmail() + " already exists");
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email " + patientRequestDTO.getEmail() + " already exists");
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        patientRepository.delete(patient);
    }

}
