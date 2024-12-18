package org.openapitools.service;

import org.openapitools.model.Administrador;
import org.openapitools.entity.AdministradorDB;
import org.openapitools.repository.AdministradorDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdministradorDBService {
    private final AdministradorDBRepository adminDBRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdministradorDBService(AdministradorDBRepository adminDBRepository, PasswordEncoder passwordEncoder) {
        this.adminDBRepository = adminDBRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Administrador convertToAdministrador(AdministradorDB adminDB) {
        return new Administrador(adminDB);
    }

    public boolean postAdministrador(Administrador admin) {
        try {
            AdministradorDB adminDB = new AdministradorDB(admin);
            String encodedPassword = passwordEncoder.encode(admin.getPassword());

            adminDB.setPassword(encodedPassword);
            adminDBRepository.save(adminDB);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAdministrador(Integer id, Administrador admin) {
        Optional<AdministradorDB> existingAdmin = adminDBRepository.findById(id);
        if (existingAdmin.isPresent()) {
            AdministradorDB updatedAdminDB = existingAdmin.get();
            updatedAdminDB.setNombre(admin.getNombre());
            updatedAdminDB.setApellidos(admin.getApellidos());
            updatedAdminDB.setFechaDeNacimiento(admin.getFechaDeNacimiento());
            updatedAdminDB.setEmail(admin.getEmail());
            updatedAdminDB.setPassword(admin.getPassword());

            adminDBRepository.save(updatedAdminDB);
            return true;
        }
        return false;
    }

    public Optional<Administrador> getAdministradorById(Integer id) {
        Optional<AdministradorDB> adminDBOptional = adminDBRepository.findById(id);
        return adminDBOptional.map(this::convertToAdministrador);
    }

    public List<Administrador> getAllAdministradores() {
        List<AdministradorDB> listaAdminsDB = (List<AdministradorDB>) adminDBRepository.findAll();
        return listaAdminsDB.stream()
                .map(this::convertToAdministrador)
                .collect(Collectors.toList());
    }

    public boolean deleteAdministradorById(Integer id) {
        Optional<AdministradorDB> admin = adminDBRepository.findById(id);
        if (admin.isPresent()) {
            adminDBRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void deleteAdministrador(AdministradorDB admin) {
        adminDBRepository.delete(admin);
    }
}
