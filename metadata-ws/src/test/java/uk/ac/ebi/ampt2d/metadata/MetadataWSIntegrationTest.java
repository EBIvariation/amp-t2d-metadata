/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "src/test/resources/features/analysis.feature",
                "src/test/resources/features/file.feature",
                "src/test/resources/features/reference-sequence.feature",
                "src/test/resources/features/sample.feature",
                "src/test/resources/features/study.feature",
                "src/test/resources/features/taxonomy.feature",
                "src/test/resources/features/web-resource.feature"
        },
        plugin = {
                "pretty",
                "html:target/cucumber"
        },
        tags = { "not @ignore" }
)
public class MetadataWSIntegrationTest {
}
