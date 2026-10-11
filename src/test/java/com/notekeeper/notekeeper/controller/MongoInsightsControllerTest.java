package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.document.DeliveryLog;
import com.notekeeper.notekeeper.document.PageRevision;
import com.notekeeper.notekeeper.repository.mongo.DeliveryLogRepository;
import com.notekeeper.notekeeper.repository.mongo.PageRevisionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoInsightsControllerTest {

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    @Test
    void reportsUnavailableInsteadOfFailingWhenMongoIsNotConnected() {
        MongoInsightsController controller = new MongoInsightsController(provider(null), provider(null));

        assertFalse((Boolean) controller.status().get("available"));
        Map<String, Object> revisions = controller.pageRevisions(50, null).getBody();
        assertFalse((Boolean) revisions.get("available"));
        assertTrue(((List<?>) revisions.get("items")).isEmpty());
        assertFalse((Boolean) controller.deliveryLogs(50, null).getBody().get("available"));
    }

    @Test
    void statusCountsBothCollections() {
        PageRevisionRepository revisionRepo = mock(PageRevisionRepository.class);
        DeliveryLogRepository logRepo = mock(DeliveryLogRepository.class);
        when(revisionRepo.count()).thenReturn(3L);
        when(logRepo.count()).thenReturn(7L);

        Map<String, Object> status = new MongoInsightsController(provider(revisionRepo), provider(logRepo)).status();

        assertTrue((Boolean) status.get("available"));
        assertEquals(3L, status.get("pageRevisions"));
        assertEquals(7L, status.get("deliveryLogs"));
    }

    @Test
    void revisionListShowsAShortPreviewWithoutHtmlTags() {
        PageRevisionRepository revisionRepo = mock(PageRevisionRepository.class);
        PageRevision revision = new PageRevision("page-1", "Plan", "<p>Hello <b>world</b></p>", "ws-1", "user-1");
        when(revisionRepo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(revision)));

        Map<String, Object> body = new MongoInsightsController(provider(revisionRepo), provider(mock(DeliveryLogRepository.class)))
                .pageRevisions(50, null).getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        assertEquals(1, items.size());
        assertEquals("Hello world", items.get(0).get("preview"));
        assertEquals("page-1", items.get(0).get("pageId"));
        assertFalse(items.get(0).containsKey("content"), "the list never sends the full content");
    }

    @Test
    void channelFilterIsPassedToTheQuery() {
        DeliveryLogRepository logRepo = mock(DeliveryLogRepository.class);
        when(logRepo.findByChannel(eq("sms"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new DeliveryLog("sms", "+250700000000", "hi"))));

        Map<String, Object> body = new MongoInsightsController(provider(mock(PageRevisionRepository.class)), provider(logRepo))
                .deliveryLogs(50, "SMS").getBody();

        verify(logRepo).findByChannel(eq("sms"), any(Pageable.class));
        assertEquals(1L, body.get("total"));
    }
}
