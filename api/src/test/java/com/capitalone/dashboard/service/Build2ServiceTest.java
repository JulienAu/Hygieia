package com.capitalone.dashboard.service;

import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.repository.Build2Repository;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.request.Build2Request;
import com.mysema.query.types.Predicate;
import org.bson.types.ObjectId;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Build2ServiceTest {

    @Mock private Build2Repository buildRepository;
    @Mock private ComponentRepository componentRepository;
    @Mock private CollectorRepository collectorRepository;
    @InjectMocks private Build2ServiceImpl buildService;

    @Test
    public void search() {
        ObjectId componentId = ObjectId.get();
        ObjectId collectorItemId = ObjectId.get();
        ObjectId collectorId = ObjectId.get();

        Build2Request request = new Build2Request();
        request.setComponentId(componentId);

        when(componentRepository.findOne(request.getComponentId())).thenReturn(makeComponent(collectorItemId, collectorId));
        when(collectorRepository.findOne(collectorId)).thenReturn(new Collector());

        buildService.search(request);

        verify(buildRepository, times(1)).findAll(argThat(hasPredicate("build2.collectorItemId = " + collectorItemId.toString())));
    }

    

    private Component makeComponent(ObjectId collectorItemId, ObjectId collectorId) {
        CollectorItem item = new CollectorItem();
        item.setId(collectorItemId);
        item.setCollectorId(collectorId);
        Component c = new Component();
        c.getCollectorItems().put(CollectorType.Build2, Arrays.asList(item));
        return c;
    }

    private Matcher<Predicate> hasPredicate(final String value) {
        return new TypeSafeMatcher<Predicate>() {
            @Override
            protected boolean matchesSafely(Predicate predicate) {
                return predicate.toString().equalsIgnoreCase(value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Predicate equal to " + value);
            }
        };
    }
}
