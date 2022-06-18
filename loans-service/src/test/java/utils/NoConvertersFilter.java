package utils;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class NoConvertersFilter extends TypeExcludeFilter {

    private static final String CONVERTER_INTERFACE_NAME = Converter.class.getName();

    @Override
    public boolean match(@NonNull final MetadataReader metadataReader, @NonNull final MetadataReaderFactory metadataReaderFactory) {

        return Arrays.asList(metadataReader.getClassMetadata().getInterfaceNames()).contains(CONVERTER_INTERFACE_NAME);
    }
}
