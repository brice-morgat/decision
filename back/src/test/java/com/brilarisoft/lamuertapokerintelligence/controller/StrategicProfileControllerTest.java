package com.brilarisoft.lamuertapokerintelligence.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileDetailDto;
import com.brilarisoft.lamuertapokerintelligence.exception.RestExceptionHandler;
import com.brilarisoft.lamuertapokerintelligence.service.StrategicProfileService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StrategicProfileControllerTest {

    @Mock
    private StrategicProfileService strategicProfileService;

    @Test
    void createProfileReturnsBadRequestWhenPayloadIsInvalid() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StrategicProfileController(strategicProfileService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "desc",
                                  "active": false,
                                  "archived": false,
                                  "versionLabel": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProfileReturnsCreatedWhenPayloadIsValid() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StrategicProfileController(strategicProfileService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        when(strategicProfileService.createProfile(any())).thenReturn(new StrategyProfileDetailDto(
                UUID.randomUUID(),
                "Cash standard",
                "desc",
                GameType.CASH,
                true,
                false,
                "v1",
                Instant.now(),
                Instant.now()
        ));

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cash standard",
                                  "description": "desc",
                                  "gameType": "CASH",
                                  "active": true,
                                  "archived": false,
                                  "versionLabel": "v1"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
