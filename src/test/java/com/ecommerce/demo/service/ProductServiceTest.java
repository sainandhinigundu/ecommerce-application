package com.ecommerce.demo.service;

import com.ecommerce.demo.exception.InsufficientStockException;
import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.repository.ProductRepository;
import com.ecommerce.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Wireless Mouse", "Ergonomic 2.4GHz mouse", new BigDecimal("799.00"), 50);
    }

    @Test
    @DisplayName("Creates a new product")
    void create_success() {
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.create(product);

        assertThat(result.getName()).isEqualTo("Wireless Mouse");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Returns product when found by id")
    void getById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getById(1L);

        assertThat(result.getPrice()).isEqualByComparingTo("799.00");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when product id does not exist")
    void getById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Returns all products")
    void getAll_returnsList() {
        Product second = new Product(2L, "Keyboard", "Mechanical keyboard", new BigDecimal("2499.00"), 20);
        when(productRepository.findAll()).thenReturn(Arrays.asList(product, second));

        List<Product> result = productService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Updates product fields")
    void update_success() {
        Product updated = new Product(null, "Wireless Mouse Pro", "Upgraded version", new BigDecimal("999.00"), 30);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Wireless Mouse Pro");
        assertThat(result.getStock()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deletes a product that exists")
    void delete_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete throws ResourceNotFoundException when product does not exist")
    void delete_notFound_throwsException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Reduces stock when sufficient quantity is available")
    void reduceStock_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.reduceStock(1L, 10);

        assertThat(result.getStock()).isEqualTo(40);
    }

    @Test
    @DisplayName("Throws InsufficientStockException when requested quantity exceeds stock")
    void reduceStock_insufficientStock_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.reduceStock(1L, 100))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Wireless Mouse");

        verify(productRepository, never()).save(any(Product.class));
    }
}
