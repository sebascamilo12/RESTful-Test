package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("GET /widgets success")
    void testGetWidgetsSuccess() throws Exception {
        // Setup our mocked service
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
        doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

        // Execute the GET request
        mockMvc.perform(get("/rest/widgets"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))

                // Validate the returned fields
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }


    @Test
    @DisplayName("GET /rest/widget/{id}")
    void testGetWidgetById() throws Exception {
        Widget widget = new Widget(1l, "Widget 1 Name", "Description 1", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
        doReturn(Optional.of(widget)).when(service).findById(1l);


        mockMvc.perform(get("/rest/widget/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Widget 1 Name")))
                .andExpect(jsonPath("$.description", is("Description 1")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception {
        // Setup our mocked service
        doReturn(Optional.empty()).when(service).findById(1l);

        // Execute the GET request
        mockMvc.perform(get("/rest/widget/{id}", 1L))
                // Validate the response code
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /rest/widget/{id} - Not Found")
    void testPutWidgetByIdNotFound() throws Exception {
        Widget widgetUpdate = new Widget(1L, "New Widget", "This is my widget", 1);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(httpHeaders.IF_MATCH, "");
        doReturn(Optional.empty()).when(service).findById(1l);
        mockMvc.perform(put("/rest/widget/{id}", 1L)
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetUpdate)))

                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /rest/widget/{id}")
    void testPutWidgetById() throws Exception {
        Widget widgetToUpdate = new Widget(1L, "New Widget", "This is my widget", 1);
        Widget widgetUpdate = new Widget(1L, "New Widget", "This is my widget2", 4);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(httpHeaders.IF_MATCH, "1");
        doReturn(Optional.of(widgetToUpdate)).when(service).findById(1L);
        doReturn(widgetUpdate).when(service).save(any());


        mockMvc.perform(put("/rest/widget/{id}", 1L)
                        .headers(httpHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetToUpdate)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))


                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget2")))
                .andExpect(jsonPath("$.version", is(4)));
    }

    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception {
        // Setup our mocked service
        Widget widgetToPost = new Widget("New Widget", "This is my widget");
        Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
        doReturn(widgetToReturn).when(service).save(any());

        // Execute the POST request
        mockMvc.perform(post("/rest/widget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetToPost)))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget")))
                .andExpect(jsonPath("$.version", is(1)));
    }


    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}