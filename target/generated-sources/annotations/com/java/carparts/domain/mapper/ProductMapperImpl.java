package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.ProductCreateRequest;
import com.java.carparts.domain.dto.ProductResponse;
import com.java.carparts.domain.dto.ProductUpdateRequest;
import com.java.carparts.domain.model.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-09T01:04:10+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setId( product.getId() );
        productResponse.setSku( product.getSku() );
        productResponse.setName( product.getName() );
        productResponse.setCategory( product.getCategory() );
        productResponse.setManufacturer( product.getManufacturer() );
        productResponse.setCompatibleModel( product.getCompatibleModel() );
        productResponse.setPrice( product.getPrice() );
        productResponse.setStock( product.getStock() );

        return productResponse;
    }

    @Override
    public Product toEntity(ProductCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        product.setSku( request.getSku() );
        product.setName( request.getName() );
        product.setCategory( request.getCategory() );
        product.setManufacturer( request.getManufacturer() );
        product.setCompatibleModel( request.getCompatibleModel() );
        product.setPrice( request.getPrice() );
        product.setStock( request.getStock() );

        return product;
    }

    @Override
    public void updateEntityFromRequest(ProductUpdateRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getCategory() != null ) {
            product.setCategory( request.getCategory() );
        }
        if ( request.getManufacturer() != null ) {
            product.setManufacturer( request.getManufacturer() );
        }
        if ( request.getCompatibleModel() != null ) {
            product.setCompatibleModel( request.getCompatibleModel() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        product.setStock( request.getStock() );
    }
}
