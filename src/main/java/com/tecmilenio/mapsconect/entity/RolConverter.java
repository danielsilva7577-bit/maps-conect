package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte el enum Rol (mayúsculas en Java) a minúsculas en la BD,
 * para coincidir con el enum de MySQL: ('estudiante','profesor','administrador').
 */
@Converter
public class RolConverter implements AttributeConverter<Usuario.Rol, String> {

    @Override
    public String convertToDatabaseColumn(Usuario.Rol rol) {
        return rol == null ? null : rol.name().toLowerCase();
    }

    @Override
    public Usuario.Rol convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return Usuario.Rol.valueOf(dbValue.toUpperCase());
    }
}
