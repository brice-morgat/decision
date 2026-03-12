package com.brilarisoft.lamuertapokerintelligence.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ReviewIssue;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.exception.RestExceptionHandler;
import com.brilarisoft.lamuertapokerintelligence.service.HandReviewService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HandReviewControllerTest {

    @Mock
    private HandReviewService handReviewService;

    @Test
    void listReviewsReturnsOk() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HandReviewController(handReviewService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        when(handReviewService.listRecentReviews()).thenReturn(List.of(new HandReviewSummaryDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cash profile",
                "AKO",
                "Ah,Kd,7s",
                DecisionStatus.SUCCESS,
                ActionType.CALL,
                ActionType.FOLD,
                BigDecimal.valueOf(-5),
                ReviewIssue.LOSS,
                "Sample",
                Instant.now()
        )));

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk());
    }

    @Test
    void createReviewRejectsInvalidPayload() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HandReviewController(handReviewService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        mockMvc.perform(post("/history")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "strategyProfileId": null,
                                  "heroHandCode": "",
                                  "recommendedAction": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReviewReturnsOk() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HandReviewController(handReviewService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        when(handReviewService.createReview(any())).thenReturn(new HandReviewDetailDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cash profile",
                "AKO",
                "Ah,Kd,7s",
                DecisionStatus.SUCCESS,
                ActionType.CALL,
                ActionType.FOLD,
                BigDecimal.valueOf(-5),
                ReviewIssue.LOSS,
                "Engine explanation",
                "PREFLOP BTN vs BB",
                "Review note",
                Instant.now(),
                Instant.now()
        ));

        mockMvc.perform(post("/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "strategyProfileId": "%s",
                                  "heroHandCode": "AKO",
                                  "boardCards": "Ah,Kd,7s",
                                  "decisionStatus": "SUCCESS",
                                  "recommendedAction": "CALL",
                                  "actualAction": "FOLD",
                                  "netResultInBigBlinds": -5,
                                  "issue": "LOSS",
                                  "situationSummary": "PREFLOP BTN vs BB",
                                  "engineExplanation": "Engine explanation",
                                  "reviewNote": "Review note"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }
}
