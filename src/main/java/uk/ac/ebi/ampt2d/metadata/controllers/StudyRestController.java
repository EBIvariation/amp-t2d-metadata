/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.metadata.controllers;

import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(tags = "Study Entity")
@RequestMapping(path = "studies")
public class StudyRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private StudyRepository studyRepository;

    @ApiOperation(value = "Get a filtered list of studies based on filtering criteria")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyses.assembly.name", value = "Assembly's name", dataType = "string", paramType = "query", example = "grch37"),
            @ApiImplicitParam(name = "analyses.assembly.patch", value = "Assembly's patch number", dataType = "string", paramType = "query", example = "p2"),
            @ApiImplicitParam(name = "analyses.type", value = "Analysis's type", dataType = "string", paramType = "query", example = "CASE_CONTROL")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    public Resources<Study> search(@QuerydslPredicate(root = Study.class) Predicate predicate) {
        return new Resources<>(studyRepository.findAll(predicate));
    }

    @ApiOperation(value = "studySearch")
    @RequestMapping(method = RequestMethod.GET, path = "/search/text")
    public Resources<Study> getStudies(@RequestParam("searchString") String searchString) {
        QStudy study = QStudy.study;
        Predicate predicate = study.name.containsIgnoreCase(searchString).
                or(study.description.containsIgnoreCase(searchString));
        Iterable<Study> studyIterable = studyRepository.findAll(predicate);
        return new Resources(studyIterable, ((List<Study>) studyIterable).stream().
                map(studyObj -> ControllerLinkBuilder.linkTo(Study.class).slash("studies").slash(studyObj.getId())
                        .withRel("search")).collect(Collectors.toList()));
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/text").withRel("studies"));
        return resource;
    }

}
