package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.category.CategoriesListResponse;
import com.tbank.ttravels_backend.dto.category.CategoryResponse;
import com.tbank.ttravels_backend.dto.category.CreateCategoryRequest;
import com.tbank.ttravels_backend.dto.category.EditCategoryRequest;
import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.exception.CategoryNotFoundException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.repository.CategoryRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private CategoryService categoryService;


    @Test
    void createCategory() {

        // === Given ===
        long travelId = 1;
        long newCategoryId = 2;
        CreateCategoryRequest createCategoryRequest = TestDataFactory.createCategoryRequest("category name");
        Travel travel = TestDataFactory.travel(travelId);


        // === Mocking ===
        doReturn(Optional.of(travel)).when(travelRepository).findById(travelId);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category c = invocation.getArgument(0);
                    c.setId(newCategoryId);
                    return c;
                });

        // === When ===
        CategoryResponse actual = categoryService.create(travelId, createCategoryRequest);

        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getId()).isEqualTo(newCategoryId),
                () -> assertThat(actual.getTravelId()).isEqualTo(travelId),
                () -> assertThat(actual.getName()).isEqualTo(createCategoryRequest.getName())
        );

        // === VERIFY ===
        verify(travelRepository).findById(travelId);
        verify(categoryRepository).save(any(Category.class));
    }


    @Test
    void throwTravelNotFoundException() {
        // === Given ===
        long travelId = 1;
        CreateCategoryRequest createCategoryRequest = TestDataFactory.createCategoryRequest("category name");

        // === Mocking ===
        doReturn(Optional.empty()).when(travelRepository).findById(travelId);

        // === When & Then===
        assertThrows(TravelNotFoundException.class, () -> categoryService.create(travelId, createCategoryRequest));

        // === VERIFY ===
        verifyNoInteractions(categoryRepository);
    }


    @Test
    void editCategory() {

        // === Given ===
        long travelId = 1;
        long categoryId = 2;
        EditCategoryRequest editCategoryRequest = TestDataFactory.editCategoryRequest("new category name");
        Category category = TestDataFactory.category(categoryId, TestDataFactory.travel(travelId));


        // === Mocking ===
        doReturn(Optional.of(category)).when(categoryRepository).findByIdAndTravel_Id(categoryId, travelId);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // === When ===
        CategoryResponse actual = categoryService.edit(travelId, categoryId, editCategoryRequest);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getTravelId()).isEqualTo(travelId),
                () -> assertThat(actual.getName()).isEqualTo(editCategoryRequest.getName())
        );


        // === VERIFY ===
        verify(categoryRepository).findByIdAndTravel_Id(categoryId, travelId);
        verify(categoryRepository).save(any(Category.class));
    }


    @Test
    void throwCategoryNotFoundException() {

        // === Given ===
        long travelId = 1;
        long categoryId = 2;
        EditCategoryRequest editCategoryRequest = TestDataFactory.editCategoryRequest("new category name");

        // === Mocking ===
        doReturn(Optional.empty()).when(categoryRepository).findByIdAndTravel_Id(categoryId, travelId);

        // === When & Then===
        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.edit(travelId, categoryId, editCategoryRequest));

        // === VERIFY ===
        verify(categoryRepository, never()).save(any());
    }


    @Test
    void getByTravel() {

        // === Given ===
        long travelId = 1;
        List<Category> categories = List.of(TestDataFactory.category(1L, TestDataFactory.travel(travelId)),
                TestDataFactory.category(2L, TestDataFactory.travel(travelId)),
                TestDataFactory.category(3L, TestDataFactory.travel(travelId)));

        // === Mocking ===
        doReturn(categories).when(categoryRepository).findAllByTravel_Id(travelId);


        // === When ===
        CategoriesListResponse actual = categoryService.getByTravel(travelId);

        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getItems()).hasSize(categories.size()),
                () -> assertCategoryDTOsMatchEntities(actual.getItems(), categories)
        );


        // === Verify ===
        verify(categoryRepository).findAllByTravel_Id(travelId);
    }

    @Test
    void findAllCategoryInTravel() {


        // === Given ===
        long travelId = 1;
        List<Category> categories = List.of(
                TestDataFactory.category(1L),
                TestDataFactory.category(2L)
        );

        // === Mocking ===
        doReturn(categories).when(categoryRepository).findAllByTravel_Id(travelId);

        // === When ===
        List<Category> actual = categoryService.findAllCategoryInTravel(travelId);

        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual).containsExactlyElementsOf(categories)
        );

        // === Verify ===
        verify(categoryRepository).findAllByTravel_Id(travelId);
    }

    @Test
    void findCategory_Success() {

        // === Given ===
        long categoryId = 1;
        Category category = TestDataFactory.category(categoryId);

        // === Mocking ===
        doReturn(Optional.of(category)).when(categoryRepository).findById(categoryId);

        // === When ===
        Category actual = categoryService.findCategory(categoryId);

        // === Then ===
        assertThat(actual).isSameAs(category);

        // === Verify ===
        verify(categoryRepository).findById(categoryId);
    }


    @Test
    void findCategory_NotFound() {

        // === Given ===
        long categoryId = 1;

        // === Mocking ===
        doReturn(Optional.empty()).when(categoryRepository).findById(categoryId);

        // === When & Then ===
        assertThrows(CategoryNotFoundException.class, () -> categoryService.findCategory(categoryId));

        // === Verify ===
        verify(categoryRepository).findById(categoryId);
    }


    private void assertCategoryDTOsMatchEntities(List<CategoryResponse> dtos, List<Category> categories) {
        for (CategoryResponse dto : dtos) {
            Category matchingCategory = categories.stream()
                    .filter(c -> c.getId().equals(dto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Entity id = " +
                            dto.getId() + " not found"));

            assertThat(dto.getTravelId()).isEqualTo(matchingCategory.getTravel().getId());
            assertThat(dto.getName()).isEqualTo(matchingCategory.getName());
        }
    }
}
