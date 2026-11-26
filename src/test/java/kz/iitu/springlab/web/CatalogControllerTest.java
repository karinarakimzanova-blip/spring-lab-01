package kz.iitu.springlab.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.NoSuchElementException;
import kz.iitu.springlab.aspect.CallCounterAspect;
import kz.iitu.springlab.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CatalogService catalogService;

    @MockitoBean
    CallCounterAspect callCounterAspect;

    @Test
    @WithMockUser(roles = "USER")
    void existingItemReturns200WithTitleField() throws Exception {
        given(catalogService.findById(1L)).willReturn("Clean Code");

        mockMvc.perform(get("/api/lab4/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void missingItemReturns404WithProblemDetail() throws Exception {
        given(catalogService.findById(99L)).willThrow(new NoSuchElementException("Item 99 not found"));

        mockMvc.perform(get("/api/lab4/item/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidRequestBodyReturns400DueToBeanValidation() throws Exception {
        mockMvc.perform(post("/api/lab4/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}