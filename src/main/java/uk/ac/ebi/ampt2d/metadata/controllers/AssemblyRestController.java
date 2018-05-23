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
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;

@RestController
@Api(tags = "Assembly Entity")
public class AssemblyRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private AssemblyRepository assemblyRepository;

    @ApiOperation(value="Get the list of assemblies or by query filter")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "Assembly's name", dataType = "string", paramType = "query", example = "grch38"),
            @ApiImplicitParam(name = "patch", value = "Assembly's patch number", dataType = "string", paramType = "query", example = "p2"),
            @ApiImplicitParam(name = "accessions", value = "Assembly's accession", dataType = "string", paramType = "query", example = "GCA_000001405.3")
    })
    @RequestMapping(method = RequestMethod.GET, value = "/assemblies/search")
    @ResponseBody
    public Iterable<Assembly> filter(@QuerydslPredicate(root = Assembly.class) Predicate predicate) {
        return assemblyRepository.findAll(predicate);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(AssemblyRestController.class).slash("/assemblies/search").withRel("assemblies"));
        return resource;
    }

}