package com.capitalone.dashboard.service;
import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.Build2Repository;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.request.Build2Request;
import com.mysema.query.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Build2ServiceImpl implements Build2Service {

	
    private final Build2Repository buildRepository;
    private final ComponentRepository componentRepository;
    private final CollectorRepository collectorRepository;

    @Autowired
    public Build2ServiceImpl(Build2Repository buildRepository,
                            ComponentRepository componentRepository,
                            CollectorRepository collectorRepository) {
        this.buildRepository = buildRepository;
        this.componentRepository = componentRepository;
        this.collectorRepository = collectorRepository;
    }

    @Override
    public DataResponse<Iterable<Build2>> search(Build2Request request) {
        Component component = componentRepository.findOne(request.getComponentId());
        CollectorItem item = component.getCollectorItems().get(CollectorType.Build2).get(0);

        QBuild2 build = new QBuild2("build2");
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(build.collectorItemId.eq(item.getId()));

        
        Iterable<Build2> result = buildRepository.findAll(build.collectorItemId.eq(item.getId()));
        Collector collector = collectorRepository.findOne(item.getCollectorId());
        return new DataResponse<>(result, collector.getLastExecuted());
    }
}
