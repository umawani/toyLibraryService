package com.toyLibrary.toyLibraryService.controller;

import com.toyLibrary.toyLibraryService.dto.request.ProductRequestDTO;
import com.toyLibrary.toyLibraryService.dto.response.ProductListResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.ProductResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.ResponseDTO;
import com.toyLibrary.toyLibraryService.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping("/list")
    public ResponseDTO<ProductListResponseDTO> getProductList(){
        return productService.getProductList();
    }

    @PostMapping("/create")
    public ResponseDTO<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO req){
        return productService.addProduct(req);
    }

    @PutMapping("/edit")
    public ResponseDTO<ProductResponseDTO> editProduct(@RequestBody ProductRequestDTO req){
        return productService.editProduct(req);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<String> deleteProduct(@PathVariable("id") Integer i){
        return productService.deleteProduct(i);
    }
}
