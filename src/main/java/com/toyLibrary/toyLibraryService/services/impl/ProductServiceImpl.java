package com.toyLibrary.toyLibraryService.services.impl;

import com.toyLibrary.toyLibraryService.dto.request.ProductRequestDTO;
import com.toyLibrary.toyLibraryService.dto.response.ProductListResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.ProductResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.ResponseDTO;
import com.toyLibrary.toyLibraryService.entity.Product;
import com.toyLibrary.toyLibraryService.repository.ProductRepository;
import com.toyLibrary.toyLibraryService.services.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;

    public ResponseDTO<ProductListResponseDTO> getProductList(){
        // Simply retireve all products
        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> productListResponse = products.stream().map(ProductResponseDTO::new).toList();
        ProductListResponseDTO response = new ProductListResponseDTO(productListResponse);
        return new ResponseDTO<>(response, HttpStatus.OK.value(), "Fetched Product List Successfully!");
    }

    public ResponseDTO<ProductResponseDTO> addProduct(ProductRequestDTO req){
        // Currently, only adding products by their names. Later want to add images as well
        Product p = new Product();
        p.setName(req.getName());
        p = productRepository.save(p);
        ProductResponseDTO responseDTO = new ProductResponseDTO(p);
        return new ResponseDTO<>(responseDTO, HttpStatus.OK.value(), "Product Created Successfully!");
    }

    public ResponseDTO<ProductResponseDTO> editProduct(ProductRequestDTO req){
        Optional<Product> optionalProduct = productRepository.findById(req.getId());
        if(optionalProduct.isEmpty()){
            // Product does not exist! Returning Error!
            System.out.println("Product does not exist with given ID!");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with provided ID!");
        }
        // Found product with provided ID
        Product p = optionalProduct.get();
        p.setName(req.getName());
        p = productRepository.save(p);

        return new ResponseDTO<>(new ProductResponseDTO(p), HttpStatus.OK.value(), "Product edited successfully!");
    }

    @Transactional
    public ResponseDTO<String> deleteProduct(Integer id){
        // Delete Product by ID
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isEmpty()){
            // Product does not exist! Returning Error!
            System.out.println("Product does not exist with given ID!");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with provided ID!");
        }
        // If product exists, check if it is not currently booked by someone
        Product p = optionalProduct.get();
        if(checkIfProductIsAlreadyBooked(id)){
            // Currently, booked by someone! Cannot delete!
            System.out.println("Product currently booked by a customer!");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is currently booked by a customer! Cannot delete!");
        }
        // Product is free to delete!
        productRepository.delete(p);
        return new ResponseDTO<>(HttpStatus.OK.value(), "Product Deleted Successfully!");
    }

    public boolean checkIfProductIsAlreadyBooked(Integer id){
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isEmpty()){
            // Product does not exist! Returning Error!
            System.out.println("Product does not exist with given ID!");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with provided ID!");
        }
        // If product exists, check if it is not currently booked by someone
        Product p = optionalProduct.get();
        if(Objects.nonNull(p.getBookedBy())){
            // Already booked!
            return true;
        }
        return false;
    }

}
