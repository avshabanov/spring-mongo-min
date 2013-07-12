package org.springframework.mongo.mappable.object;

/**
 * Marker interface that designates mappable data object which in its turn "tells" underlying data
 * provider that it supports simple field mapping.
 * </p>
 * The class that implements this interface SHALL have non-final fields and public parameterless constructor.
 * Each field of the implementing class shall be of String or Number or List or Map or another MappableDataObject type.
 *
 * @author Alexander Shabanov
 */
public interface MappableDataObject {
}
