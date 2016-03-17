package com.capitalone.dashboard.rest;

import com.capitalone.dashboard.editors.CaseInsensitiveBuildStatusEditor;
import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.BuildStatus;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.request.Build2Request;
import com.capitalone.dashboard.service.Build2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;



@RestController
public class Build2Controller {
	
	private final Build2Service buildService;

	@Autowired
	public Build2Controller(Build2Service buildService) {
		this.buildService = buildService;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(BuildStatus.class, new CaseInsensitiveBuildStatusEditor());
	}

	@RequestMapping(value = "/build2", method = GET, produces = APPLICATION_JSON_VALUE)
	public DataResponse<Iterable<Build2>> builds(@Valid Build2Request request) throws IllegalArgumentException, IllegalAccessException {
		return buildService.search(request);
	}
}
