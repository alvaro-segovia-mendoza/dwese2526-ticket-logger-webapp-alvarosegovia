package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.controllers;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto.*;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.DuplicateResourceException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.exeptions.ResourceNotFoundException;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services.ProvinceService;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services.RegionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvinceControllerUnitTest {

    @Mock
    private ProvinceService provinceService;

    @Mock
    private RegionService regionService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProvinceController controller;

    private final Locale locale = new Locale("es");

    // =========================
    // GET /provinces/new
    // =========================

    @Test
    void showNewForm_ok() {
        Model model = new ExtendedModelMap();

        when(regionService.list(Pageable.unpaged()))
                .thenReturn(Page.empty());

        String view = controller.showNewForm(model, locale);

        assertEquals("views/province/province-form", view);
        assertTrue(model.containsAttribute("province"));
        assertTrue(model.containsAttribute("listRegions"));
        verify(regionService).list(Pageable.unpaged());
    }

    // =========================
    // GET /provinces/edit
    // =========================

    @Test
    void showEditForm_ok() {
        Model model = new ExtendedModelMap();

        when(provinceService.getForEdit(1L))
                .thenReturn(new ProvinceUpdateDTO());
        when(regionService.list(Pageable.unpaged()))
                .thenReturn(Page.empty());

        String view = controller.showEditForm(1L, model, locale);

        assertEquals("views/province/province-form", view);
        assertTrue(model.containsAttribute("province"));
        assertTrue(model.containsAttribute("listRegions"));
        verify(provinceService).getForEdit(1L);
    }

    // =========================
    // GET /provinces/detail
    // =========================

    @Test
    void showDetail_ok() {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(provinceService.getDetail(1L))
                .thenReturn(new ProvinceDetailDTO());

        String view = controller.showDetail(1L, model, redirect, locale);

        assertEquals("views/province/province-detail", view);
        assertTrue(model.containsAttribute("province"));
    }

    @Test
    void showDetail_notFound_redirect() {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(provinceService.getDetail(1L))
                .thenThrow(new ResourceNotFoundException("province", "id", 1L));
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("error");

        String view = controller.showDetail(1L, model, redirect, locale);

        assertEquals("redirect:/provinces", view);
    }

    // =========================
    // GET /provinces
    // =========================

    @Test
    void listProvinces_ok() {
        Model model = new ExtendedModelMap();
        Pageable pageable = PageRequest.of(0, 10);

        when(provinceService.list(pageable))
                .thenReturn(Page.empty(pageable));

        String view = controller.listProvinces(pageable, model, locale);

        assertEquals("views/province/province-list", view);
        assertTrue(model.containsAttribute("page"));
        assertTrue(model.containsAttribute("sortParam"));
    }

    // =========================
    // POST /provinces/insert
    // =========================

    @Test
    void insertProvince_ok() {
        ProvinceCreateDTO dto = new ProvinceCreateDTO();
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        when(result.hasErrors()).thenReturn(false);

        String view = controller.insertProvince(dto, result, redirect, model, locale);

        assertEquals("redirect:/provinces", view);
        verify(provinceService).create(dto);
    }

    @Test
    void insertProvince_duplicateCode() {
        ProvinceCreateDTO dto = new ProvinceCreateDTO();
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        when(result.hasErrors()).thenReturn(false);
        doThrow(new DuplicateResourceException("province", "code", "SE"))
                .when(provinceService).create(dto);
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("error");

        String view = controller.insertProvince(dto, result, redirect, model, locale);

        assertEquals("redirect:/provinces/new", view);
    }

    @Test
    @DisplayName("insertProvince validation error -> vuelve al formulario")
    void insertProvince_validationError() {
        ProvinceCreateDTO dto = new ProvinceCreateDTO();

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        RedirectAttributes redirect = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();

        String view = controller.insertProvince(dto, result, redirect, model, locale);

        assertEquals("views/province/province-form", view);

        verify(provinceService, never()).create(any());
    }


    // =========================
    // POST /provinces/update
    // =========================

    @Test
    void updateProvince_ok() {
        ProvinceUpdateDTO dto = new ProvinceUpdateDTO();
        dto.setId(1L);
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(result.hasErrors()).thenReturn(false);

        String view = controller.updateProvince(dto, result, redirect, locale);

        assertEquals("redirect:/provinces", view);
        verify(provinceService).update(dto);
    }

    @Test
    void updateProvince_notFound() {
        ProvinceUpdateDTO dto = new ProvinceUpdateDTO();
        dto.setId(1L);
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(result.hasErrors()).thenReturn(false);
        doThrow(new ResourceNotFoundException("province", "id", 1L))
                .when(provinceService).update(dto);

        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("error");

        String view = controller.updateProvince(dto, result, redirect, locale);

        assertEquals("redirect:/provinces", view);
    }

    @Test
    @DisplayName("updateProvince validation error -> vuelve al formulario")
    void updateProvince_validationError() {
        ProvinceUpdateDTO dto = new ProvinceUpdateDTO();
        dto.setId(1L);

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.updateProvince(dto, result, redirect, locale);

        assertEquals("views/province/province-form", view);

        verify(provinceService, never()).update(any());
    }


    // =========================
    // POST /provinces/delete
    // =========================

    @Test
    void deleteProvince_ok() {
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.deleteProvince(1L, redirect, locale);

        assertEquals("redirect:/provinces", view);
        verify(provinceService).delete(1L);
    }

    @Test
    void deleteProvince_notFound() {
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doThrow(new ResourceNotFoundException("province", "id", 1L))
                .when(provinceService).delete(1L);
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("error");

        String view = controller.deleteProvince(1L, redirect, locale);

        assertEquals("redirect:/provinces", view);
    }
}

