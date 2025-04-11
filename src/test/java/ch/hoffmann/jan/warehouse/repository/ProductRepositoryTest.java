package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Category;
import ch.hoffmann.jan.warehouse.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testFindAll() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("Powerful laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setCategory(category);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Smartphone");
        product2.setDescription("Latest smartphone");
        product2.setPrice(new BigDecimal("599.99"));
        product2.setCategory(category);
        entityManager.persist(product2);

        entityManager.flush();

        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "Smartphone");
    }

    @Test
    public void testFindById() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Powerful laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);

        Long id = (Long) entityManager.persistAndGetId(product);
        entityManager.flush();

        // When
        Optional<Product> foundProduct = productRepository.findById(id);

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
        assertThat(foundProduct.get().getDescription()).isEqualTo("Powerful laptop");
        assertThat(foundProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(foundProduct.get().getCategory().getName()).isEqualTo("Electronics");
    }

    @Test
    public void testFindByCategory() {
        // Given
        Category category1 = new Category();
        category1.setName("Electronics");
        entityManager.persist(category1);

        Category category2 = new Category();
        category2.setName("Books");
        entityManager.persist(category2);

        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("Powerful laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setCategory(category1);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Smartphone");
        product2.setDescription("Latest smartphone");
        product2.setPrice(new BigDecimal("599.99"));
        product2.setCategory(category1);
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("Java Programming");
        product3.setDescription("Programming book");
        product3.setPrice(new BigDecimal("49.99"));
        product3.setCategory(category2);
        entityManager.persist(product3);

        entityManager.flush();

        // When
        List<Product> electronicsProducts = productRepository.findByCategory(category1);
        List<Product> bookProducts = productRepository.findByCategory(category2);

        // Then
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "Smartphone");

        assertThat(bookProducts).hasSize(1);
        assertThat(bookProducts).extracting(Product::getName).containsExactly("Java Programming");
    }

    @Test
    public void testExistsByName() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Powerful laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);
        entityManager.persist(product);

        entityManager.flush();

        // When & Then
        assertThat(productRepository.existsByName("Laptop")).isTrue();
        assertThat(productRepository.existsByName("Smartphone")).isFalse();
    }

    @Test
    public void testSave() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Powerful laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);

        // When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();

        // Then
        assertThat(savedProduct.getId()).isNotNull();

        Product foundProduct = entityManager.find(Product.class, savedProduct.getId());
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Laptop");
        assertThat(foundProduct.getDescription()).isEqualTo("Powerful laptop");
        assertThat(foundProduct.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(foundProduct.getCategory().getName()).isEqualTo("Electronics");
    }

    @Test
    public void testUpdate() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Powerful laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);

        Long id = (Long) entityManager.persistAndGetId(product);
        entityManager.flush();

        // When
        Product productToUpdate = entityManager.find(Product.class, id);
        productToUpdate.setName("Updated Laptop");
        productToUpdate.setDescription("Updated description");
        productToUpdate.setPrice(new BigDecimal("1099.99"));

        Product updatedProduct = productRepository.save(productToUpdate);
        entityManager.flush();

        // Then
        Product foundProduct = entityManager.find(Product.class, id);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Updated Laptop");
        assertThat(foundProduct.getDescription()).isEqualTo("Updated description");
        assertThat(foundProduct.getPrice()).isEqualByComparingTo(new BigDecimal("1099.99"));
    }

    @Test
    public void testDelete() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("Powerful laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);

        Long id = (Long) entityManager.persistAndGetId(product);
        entityManager.flush();

        // When
        productRepository.deleteById(id);
        entityManager.flush();

        // Then
        Product foundProduct = entityManager.find(Product.class, id);
        assertThat(foundProduct).isNull();
    }
}